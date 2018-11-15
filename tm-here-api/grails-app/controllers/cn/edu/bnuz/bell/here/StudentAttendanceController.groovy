package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.organization.StudentService
import cn.edu.bnuz.bell.security.SecurityService

class StudentAttendanceController implements ServiceExceptionHandler {
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    StudentService studentService

    def index(String studentId) {
        def termId = termService.activeTerm.id
        if (securityService.hasRole('ROLE_STUDENT') && securityService.userId == studentId ||
                securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN') && securityService.departmentId == studentService.getDepartment(studentId)?.id ||
                securityService.hasRole('ROLE_STUDENT_COUNSELLOR') && securityService.userId == studentService.getCounsellor(studentId)?.id ||
                securityService.hasRole('ROLE_CLASS_SUPERVISOR') &&  securityService.userId == studentService.getSupervisor(studentId)?.id) {
            renderJson attendanceService.getStudentAttendances(termId, studentId)
        } else {
            throw new ForbiddenException()
        }
    }
}
