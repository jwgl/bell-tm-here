package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService
import grails.transaction.Transactional

@Transactional
class FreeListenPublicService {
    FreeListenFormService freeListenFormService
    ScheduleService scheduleService
    DataAccessService dataAccessService

    def getFormForShow(String userId, Long id) {
        def form = freeListenFormService.getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!canView(userId, id)) {
            throw new ForbiddenException()
        }

        def studentSchedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        def checkerSchedules = freeListenFormService.findCheckerOtherSchedules(form.id)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                checkerSchedules: checkerSchedules,
                departmentSchedules: departmentSchedules,
        ]
    }

    private canView(String teacherId, Long id) {
        def count = dataAccessService.getInteger '''
select count(distinct form.id)
From FreeListenForm form
join form.items item,
     CourseClass courseClass
join courseClass.tasks task
join task.schedules taskSchedule
join task.students taskStudent
where form.status = 'APPROVED'
  and form.term = courseClass.term
  and item.taskSchedule = taskSchedule
  and form.student = taskStudent.student
  and taskSchedule.teacher.id = :teacherId
  and form.id = :id
''', [teacherId: teacherId, id: id]
        return count > 0
    }

    def getRollcallFreeListens(Term term, String teacherId, Integer week, Integer dayOfWeek, Integer startSection) {
        FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  form.student.id as studentId
)
from FreeListenForm form
join form.items item,
     CourseClass courseClass
join courseClass.tasks task
join task.schedules taskSchedule
join task.students taskStudent
where form.status = 'APPROVED'
  and form.term = :term
  and item.taskSchedule = taskSchedule
  and form.student = taskStudent.student
  and form.term = courseClass.term
  and :week between taskSchedule.startWeek and taskSchedule.endWeek
  and (
    taskSchedule.oddEven = 0 or
    taskSchedule.oddEven = 1 and :week % 2 = 1 or
    taskSchedule.oddEven = 2 and :week % 2 = 0
  )
  and taskSchedule.dayOfWeek = :dayOfWeek
  and taskSchedule.startSection = :startSection
  and taskSchedule.teacher.id = :teacherId
''', [term: term, teacherId: teacherId, week: week, dayOfWeek: dayOfWeek, startSection: startSection]
    }
}
