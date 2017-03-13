package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.operation.ScheduleService
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.system.SystemConfigService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import grails.transaction.Transactional

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class FreeListenFormService {
    TermService termService
    ScheduleService scheduleService
    SystemConfigService systemConfigService

    @Resource(name='freeListenFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    def list(String studentId, Integer offset, Integer max) {
        def forms = FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  form.reason as reason,
  form.dateCreated as dateCreated,
  form.status as status
)
from FreeListenForm form
where form.student.id = :studentId
order by form.dateCreated desc
''', [studentId: studentId], [offset: offset, max: max]

        return [
                forms: forms,
                count: FreeListenForm.countByStudent(Student.load(studentId)),
                config: getDateConfig(),
                notice: systemConfigService.get(FreeListenForm.CONFIG_NOTICE, ''),
        ]
    }

    def getDateConfig() {
        def today = LocalDate.now()
        [
                startDate: systemConfigService.get(FreeListenForm.CONFIG_START_DATE, today),
                endDate: systemConfigService.get(FreeListenForm.CONFIG_END_DATE, today),
                today: today,
        ]
    }

    Map getFormInfo(Long id) {
        def results = FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  form.term.id as term,
  student.id as studentId,
  student.name as studentName,
  student.atSchool as atSchool,
  adminClass.name as adminClass,
  major.grade as grade,
  subject.name as subject,
  form.reason as reason,
  checker.id as checkerId,
  checker.name as checkerName,
  form.dateCreated as dateCreated,
  form.dateChecked as dateChecked,
  form.dateModified as dateModified,
  approver.name as approver,
  form.dateApproved as dateApproved,
  form.status as status,
  form.workflowInstance.id as workflowInstanceId
)
from FreeListenForm form
join form.student student
join student.adminClass adminClass
join student.major major
join major.subject subject
join form.checker checker
left join form.approver approver
where form.id = :id
''', [id: id]
        if (!results) {
            return null
        }

        def form = results[0]

        form.items = FreeListenItem.executeQuery '''
select new map(
  item.id as id,
  item.taskSchedule.id as scheduleId
)
from FreeListenItem item
where item.form.id = :formId
''', [formId: id]

        form.existedItems = findExistedFreeListenItems(form.studentId, form.term, form.id)

        return form
    }

    List findExistedFreeListenItems(String studentId, Integer termId, Long excludeFormId) {
        FreeListenForm.executeQuery '''
select new map(
  item.taskSchedule.id as taskScheduleId,
  form.status as status
)
from FreeListenItem item
join item.form form
where form.student.id = :studentId
  and form.term.id = :termId
  and form.id != :excludeFormId
''', [studentId: studentId, termId: termId, excludeFormId: excludeFormId]
    }

    def getFormForShow(String studentId, Long id) {
        def form = getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        def config = getDateConfig()
        if (config.today >= config.startDate && config.today <= config.endDate) {
            form.editable = domainStateMachineHandler.canUpdate(form)
        } else {
            form.editable = false
        }

        def studentSchedules = scheduleService.getStudentSchedules(studentId, form.term)
        def departmentSchedules = findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
        ]
    }

    def getFormForEdit(String studentId, Long id) {
        def form = getFormInfo(id)

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        def schedules = scheduleService.getStudentSchedules(studentId, form.term)

        return [
                form: form,
                schedules: schedules,
        ]
    }

     /**
     * 查找免听项之外同一开课单位其它开课情况
     * @param formId 免听ID
     */
    def findDepartmentOtherSchedules(Long formId) {
        List schedules = TaskSchedule.executeQuery '''
select new map(
  schedule.id as id,
  task.id as taskId,
  courseClass.id as courseClassId,
  courseClass.name as courseClassName,
  courseTeacher.id as courseTeacherId,
  courseTeacher.name as courseTeacherName,
  scheduleTeacher.id as teacherId,
  scheduleTeacher.name as teacherName,
  schedule.startWeek as startWeek,
  schedule.endWeek as endWeek,
  schedule.oddEven as oddEven,
  schedule.dayOfWeek as dayOfWeek,
  schedule.startSection as startSection,
  schedule.totalSection as totalSection,
  course.name as course,
  courseItem.name as courseItem,
  place.name as place
)
from TaskSchedule schedule
join schedule.task task
join task.courseClass courseClass
join courseClass.course course
join courseClass.teacher courseTeacher
join schedule.teacher scheduleTeacher
left join task.courseItem courseItem
left join schedule.place place
where (courseClass.term.id,
       courseClass.department.id,
       course.id,
       coalesce(courseItem.id, '0')) in (
    select courseClass.term.id,
        courseClass.department.id,
        courseClass.course.id,
        coalesce(courseItem.id, '0')
    from FreeListenForm form
    join form.items item
    join item.taskSchedule schedule
    join schedule.task task
    join task.courseClass courseClass
    left join task.courseItem courseItem
    where form.id = :formId
) and courseClass.id not in (
    select task.courseClass.id
    from FreeListenForm form
    join form.items item
    join item.taskSchedule schedule
    join schedule.task task
    where form.id = :formId     
) and (schedule.dayOfWeek, schedule.startSection) not in (
    select schedule.dayOfWeek, schedule.startSection
    from TaskSchedule schedule
    join schedule.task task
    join task.students taskStudent
    where taskStudent.student = (
        select student from FreeListenForm where id = :formId
    )
)''', [formId: formId]

        Map<Integer, List> groups = schedules.groupBy {s ->
            s.dayOfWeek * 100 + s.startSection
        }

        // 公共课（如英语）同一课号的教学班很多，这里进行限制显示的数量
        def maxLength = 3
        groups.forEach { key, group ->
            while (group.size() > maxLength) {
                def schedule = group.pop()
                schedules.remove(schedule)
                schedules.findAll {it.courseClassId == schedule.courseClassId}.forEach { other ->
                    def otherGroup = groups[other.dayOfWeek * 100 + other.startSection]
                    group.remove(other)
                    schedules.remove(other)
                }
            }
        }

        return schedules
    }

    def getFormForCreate(String studentId) {
        checkOpeningDate()

        def term = termService.activeTerm
        def schedules = scheduleService.getStudentSchedules(studentId, term.id)
        def student = Student.get(studentId)
        return [
                term: [
                        startWeek: term.startWeek,
                        endWeek: term.endWeek,
                        currentWeek: term.currentWeek,
                ],
                form: [
                        term: term.id,
                        studentId: student.id,
                        studentName: student.name,
                        atSchool: student.atSchool,
                        items: [],
                        existedItems: findExistedFreeListenItems(studentId, term.id, 0),
                ],
                schedules: schedules,
        ]
    }

    FreeListenForm create(String studentId, FreeListenFormCommand cmd) {
        checkOpeningDate()

        def now = new Date()

        FreeListenForm form = new FreeListenForm(
                student: Student.load(studentId),
                term: termService.activeTerm,
                reason: cmd.reason,
                checker: Teacher.load(cmd.checkerId),
                dateCreated: now,
                dateModified: now,
                status: domainStateMachineHandler.initialState
        )

        cmd.addedItems.each { item ->
            form.addToItems(new FreeListenItem(
                   taskSchedule: TaskSchedule.load(item)
            ))
        }

        form.save()

        domainStateMachineHandler.create(form, studentId)

        return form
    }

    FreeListenForm update(String studentId, FreeListenFormCommand cmd) {
        checkOpeningDate()

        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        form.reason = cmd.reason
        form.checker = Teacher.load(cmd.checkerId)
        form.dateModified = new Date()

        cmd.addedItems.each { taskScheduleId ->
            form.addToItems(new FreeListenItem(
                    taskSchedule: TaskSchedule.load(taskScheduleId)
            ))
        }

        cmd.removedItems.each { id ->
            def freeItem = FreeListenItem.load(id)
            form.removeFromItems(freeItem)
            freeItem.delete()
        }

        domainStateMachineHandler.update(form, studentId)

        form.save()
    }

    void delete(String studentId, Long id) {
        FreeListenForm form = FreeListenForm.get(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        if (form.workflowInstance) {
            form.workflowInstance.delete()
        }

        form.delete()
    }

    def submit(String studentId, SubmitCommand cmd) {
        checkOpeningDate()

        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canSubmit(form)) {
            throw new BadRequestException()
        }

        domainStateMachineHandler.submit(form, studentId, cmd.to, cmd.comment, cmd.title)

        form.dateSubmitted = new Date()
        form.save()
    }

    def checkOpeningDate() {
        def config = getDateConfig()
        if (config.today < config.startDate || config.today > config.endDate) {
            throw new BadRequestException()
        }
    }
}
