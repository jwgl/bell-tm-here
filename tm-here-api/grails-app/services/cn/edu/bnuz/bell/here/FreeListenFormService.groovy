package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.system.SystemConfigService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import grails.gorm.transactions.Transactional

import javax.annotation.Resource

@Transactional
class FreeListenFormService {
    @Resource(name='freeListenFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    def list(String studentId, Integer offset, Integer max) {
        FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  form.term.id as term,
  form.reason as reason,
  form.dateCreated as dateCreated,
  form.status as status
)
from FreeListenForm form
where form.student.id = :studentId
order by form.dateCreated desc
''', [studentId: studentId], [offset: offset, max: max]
    }

    def listCount(String studentId) {
        FreeListenForm.countByStudent(Student.load(studentId))
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

        form.existedItems = findExistedFreeListenItems(form.term, form.studentId, form.id)

        return form
    }

    List findExistedFreeListenItems(Integer termId, String studentId, Long excludeFormId) {
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

        def termId = form.term as Integer
        def formId = form.id as Integer
        def settings = FreeListenSettings.get(termId)
        if (settings.betweenCheckDateRange()) {
            form.editable = domainStateMachineHandler.canUpdate(form)
        } else {
            form.editable = false
        }

        def studentSchedules = getStudentSchedules(termId, studentId)
        def departmentSchedules = findDepartmentOtherSchedules(formId)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
                settings: settings,
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

        def termId = form.term as Integer
        return [
                form: form,
                schedules: getStudentSchedules(termId, studentId),
                settings : FreeListenSettings.get(termId),
        ]
    }

    List getStudentSchedules(Integer termId, String studentId) {
        TaskSchedule.executeQuery '''
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
  place.name as place,
  taskStudent.repeatType as repeatType,
  schedule.root.id as rootId
)
from CourseClass courseClass
join courseClass.course course
join courseClass.tasks task
join task.schedules schedule
join task.students taskStudent
join courseClass.teacher courseTeacher
join schedule.teacher scheduleTeacher
left join task.courseItem courseItem
left join schedule.place place
where taskStudent.student.id = :studentId
  and courseClass.term.id = :termId
''', [termId: termId, studentId: studentId]
    }

     /**
     * 查找免听项之外同一开课单位其它开课情况
     * @param formId 免听ID
     */
    def findDepartmentOtherSchedules(Long formId) {
        TaskSchedule.executeQuery '''
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
where (courseClass.term.id, courseClass.department.id, course.id) in (
    select courseClass2.term.id, courseClass2.department.id, courseClass2.course.id
    from FreeListenForm form
    join form.items item
    join item.taskSchedule schedule
    join schedule.task task
    join task.courseClass courseClass2
    where form.id = :formId
    and courseClass2.id <> courseClass.id
) and not exists (
    select 1
    from TaskSchedule ts
    join ts.task task
    join task.students taskStudent
    join task.courseClass courseClass
    where (courseClass.term, taskStudent.student) = (
        select term, student from FreeListenForm where id = :formId
    )
    and ts.dayOfWeek = schedule.dayOfWeek
    and exists (
        select 1
        from SerialNumber s1
        join SerialNumber s2 on s1.id = s2.id
        where s1.id between ts.startSection and ts.startSection + ts.totalSection -1
          and s2.id between schedule.startSection and schedule.startSection + schedule.totalSection -1
    )
    and exists (
        select 1
        from SerialNumber s1
        join SerialNumber s2 on s1.id = s2.id
        where s1.id between ts.startWeek and ts.endWeek
          and (ts.oddEven = 0 or s1.oddEven = ts.oddEven)
          and s2.id between schedule.startWeek and schedule.endWeek
          and (schedule.oddEven = 0 or s2.oddEven = schedule.oddEven)
    )
)''', [formId: formId]
    }

    def getFormForCreate(Term term, String studentId) {
        def config = FreeListenSettings.findByTerm(term)
        if (!config.betweenApplyDateRange()) {
            throw new BadRequestException()
        }

        def schedules = getStudentSchedules(term.id, studentId)
        def student = Student.get(studentId)
        return [
                form     : [
                        term        : term.id,
                        studentId   : student.id,
                        studentName : student.name,
                        atSchool    : student.atSchool,
                        items       : [],
                        existedItems: findExistedFreeListenItems(term.id, studentId, 0),
                ],
                schedules: schedules,
                settings : FreeListenSettings.get(term.id),
        ]
    }

    FreeListenForm create(Term term, String studentId, FreeListenFormCommand cmd) {
        def config = FreeListenSettings.findByTerm(term)
        if (!config.betweenApplyDateRange()) {
            throw new BadRequestException()
        }

        def now = new Date()
        FreeListenForm form = new FreeListenForm(
                student: Student.load(studentId),
                term: term,
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
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!FreeListenSettings.get(form.termId).betweenCheckDateRange()) {
            throw new BadRequestException()
        }

        if (form.studentId != studentId) {
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

        if (form.studentId != studentId) {
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
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!FreeListenSettings.get(form.termId).betweenCheckDateRange()) {
            throw new BadRequestException("Exceed check dates.")
        }

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canSubmit(form)) {
            throw new BadRequestException("Can not submit.")
        }

        domainStateMachineHandler.submit(form, studentId, cmd.to, cmd.comment, cmd.title)

        form.dateSubmitted = new Date()
        form.save()
    }
}
