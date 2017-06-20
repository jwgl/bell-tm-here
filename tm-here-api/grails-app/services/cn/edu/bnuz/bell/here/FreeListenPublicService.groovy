package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
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

        def studentSchedules = freeListenFormService.getStudentSchedules(form.term, form.studentId)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
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
     * 查找与考勤命令相关的免听记录
     * @param cmd
     * @return 免听列表
     */
    def listByTimeslot(TeacherTimeslotCommand cmd) {
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
  and form.term.id = :termId
  and (item.taskSchedule = taskSchedule
   or item.taskSchedule = taskSchedule.root.id)
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
  and taskSchedule.totalSection = :totalSection
  and taskSchedule.teacher.id = :teacherId
''', cmd as Map
    }
}
