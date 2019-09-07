package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.organization.StudentService
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.service.DataAccessService
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class FreeListenPublicService {
    FreeListenFormService freeListenFormService
    DataAccessService dataAccessService
    SecurityService securityService
    StudentService studentService

    def getFormForShow(String userId, Long id) {
        def form = freeListenFormService.getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!canView(userId, form)) {
            throw new ForbiddenException()
        }

        def termId = form.term as Integer
        def studentId = form.studentId as String
        def formId = form.id as Integer
        def settings = FreeListenSettings.get(termId)
        def studentSchedules = freeListenFormService.getStudentSchedules(termId, studentId)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(formId)
        return [
                form               : form,
                studentSchedules   : studentSchedules,
                departmentSchedules: departmentSchedules,
                settings           : settings,
        ]
    }

    private canView(String userId, Map form) {
        String studentId = form.studentId
        if (userId == studentId) {
            return true
        }

        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            if (securityService.departmentId == studentService.getDepartment(studentId).id) {
                return true
            }
        }

        if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR')) {
            if (userId == studentService.getCounsellor(studentId)?.id) {
                return true
            }
        }

        if (securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            if (userId == studentService.getSupervisor(studentId)?.id) {
                return true
            }
        }

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
''', [teacherId: userId, id: form.id]
        return count > 0
    }

    /**
     * 查找与排课相关的免听记录
     * @param taskScheduleIds 排课Id列表
     * @return 免听列表
     */
    def listByWeekAndTaskSchedules(List<UUID> taskScheduleIds) {
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
  and form.term = courseClass.term
  and (item.taskSchedule = taskSchedule
   or item.taskSchedule = taskSchedule.root.id)
  and form.student = taskStudent.student
  and taskSchedule.id in (:taskScheduleIds)
''', [taskScheduleIds: taskScheduleIds]
    }


    def getSettings(Term term) {
        FreeListenSettings.get(term.id)
    }
}
