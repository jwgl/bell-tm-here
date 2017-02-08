package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService
import cn.edu.bnuz.bell.workflow.State
import grails.transaction.Transactional

@Transactional
class FreeListenFormService {
    TermService termService
    ScheduleService scheduleService

    def list(String studentId, Integer offset, Integer max) {
        FreeListenForm.executeQuery '''
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
    }

    def formCount(String studentId) {
        FreeListenForm.countByStudent(Student.load(studentId))
    }

    def getFormInfo(Long id) {
        def results = FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  form.term.id as term,
  student.id as studentId,
  student.name as studentName,
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
  item.taskSchedule.id as taskScheduleId
)
from FreeListenItem item
where item.form.id = :formId
''', [formId: id]

        return form
    }

    def getFormForShow(String studentId, Long id) {
        def form = getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        form.editable = true // domainStateMachineHandler.canUpdate(form)

        def studentSchedules = scheduleService.getStudentSchedules(studentId, form.term)
        def checkerSchedules = findCheckerOtherSchedules(form.id)
        def departmentSchedules = findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                checkerSchedules: checkerSchedules,
                departmentSchedules: departmentSchedules,
        ]
    }

    def getFormForEdit(String studentId, Long id) {
        def form = getFormInfo(id)

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

//        if (!domainStateMachineHandler.canUpdate(form)) {
//            throw new BadRequestException()
//        }

        def schedules = scheduleService.getStudentSchedules(studentId, form.term)

        return [
                form: form,
                schedules: schedules,
        ]
    }

    /**
     * 查找免听项之外同一教师其它开课情况
     * @param formId 免听ID
     */
    def findCheckerOtherSchedules(Long formId) {
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
where (courseClass.term.id,
       courseClass.department.id,
       courseTeacher.id,
       course.id,
       coalesce(courseItem.id, '0')) in (
    select courseClass.term.id,
        courseClass.department.id,
        courseClass.teacher.id,
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
)''', [formId: formId]
    }

    /**
     * 查找免听项之外同一开课单位其它教师其它开课情况
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
) and courseTeacher.id not in (
    select courseClass.teacher.id
    from FreeListenForm form
    join form.items item
    join item.taskSchedule schedule
    join schedule.task task
    join task.courseClass courseClass
    where form.id = :formId         
)''', [formId: formId]
    }

    def getFormForCreate(String studentId) {
        def term = termService.activeTerm
        def schedules = scheduleService.getStudentSchedules(studentId, term.id)

        return [
                term: [
                        startWeek: term.startWeek,
                        endWeek: term.endWeek,
                        currentWeek: term.currentWeek,
                ],
                form: [
                        items: [],
                ],
                schedules: schedules,
                existedItems: findExistedFreeListenItems(studentId, term, 0),
        ]
    }

    List findExistedFreeListenItems(String studentId, Term term, Long excludeFormId) {
        FreeListenForm.executeQuery '''
select new map (
  item.id as id,
  item.taskSchedule.id as taskScheduleId
) 
from FreeListenItem item
join item.form form
where form.student.id = :studentId
  and form.term = :term
  and form.status != :excludeStatus
  and form.id != :excludeFormId
''', [studentId: studentId, term: term, excludeStatus: State.REJECTED, excludeFormId: excludeFormId]
    }

    FreeListenForm create(String studentId, FreeListenFormCommand cmd) {
        def now = new Date()

        FreeListenForm form = new FreeListenForm(
                student: Student.load(studentId),
                term: termService.activeTerm,
                reason: cmd.reason,
                checker: Teacher.load(cmd.checkerId),
                dateCreated: now,
                dateModified: now,
                status: State.CREATED, // domainStateMachineHandler.initialState
        )

        cmd.addedItems.each { item ->
            form.addToItems(new FreeListenItem(
                   taskSchedule: TaskSchedule.load(item.taskScheduleId)
            ))
        }

        form.save()

        // domainStateMachineHandler.create(form, studentId)

        return form
    }

    FreeListenForm update(String studentId, FreeListenFormCommand cmd) {
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

//        if (!domainStateMachineHandler.canUpdate(form)) {
//            throw new BadRequestException()
//        }

        form.reason = cmd.reason
        form.checker = Teacher.load(cmd.checkerId)
        form.dateModified = new Date()

        cmd.addedItems.each { item ->
            form.addToItems(new FreeListenItem(
                    taskSchedule: TaskSchedule.load(item.taskScheduleId)
            ))
        }

        cmd.removedItems.each {
            def freeItem = FreeListenItem.load(it)
            form.removeFromItems(freeItem)
            freeItem.delete()
        }

        // domainStateMachineHandler.update(form, studentId)

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

//        if (!domainStateMachineHandler.canUpdate(form)) {
//            throw new BadRequestException()
//        }

        if (form.workflowInstance) {
            form.workflowInstance.delete()
        }

        form.delete()
    }
}
