package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.organization.StudentService
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_ATTENDANCE_LIST")')
class AttendanceController implements ServiceExceptionHandler{
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    StudentService studentService

    def index() {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 20
        def termId = termService.activeTerm.id
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            renderJson([
                    termId      : termId,
                    adminClasses: attendanceService.adminClassesByDepartment(termId, securityService.departmentId),
                    students    : attendanceService.studentStatsByDepartment(termId, securityService.departmentId, offset, max)
            ])
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') || securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            renderJson([
                    termId      : termId,
                    adminClasses: attendanceService.adminClassesByAdministrator(termId, securityService.userId),
                    students    : attendanceService.studentStatsByAdministrator(termId, securityService.userId, offset, max),
            ])
        } else {
            throw new ForbiddenException()
        }
    }

    /**
     * 管理人员查看
     * @param id 学号
     */
    def show(String id) {
        def termId = termService.activeTerm.id
        def userId = securityService.userId
        def studentId = id
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN') && securityService.departmentId == studentService.getDepartment(studentId)?.id ||
            securityService.hasRole('ROLE_STUDENT_COUNSELLOR') && userId == studentService.getCounsellor(studentId)?.id ||
            securityService.hasRole('ROLE_CLASS_SUPERVISOR') && userId == studentService.getSupervisor(studentId)?.id) {
            renderJson([
                    list :attendanceService.getStudentAttendances(studentId, termId),
                    student: studentService.getStudentInfo(studentId)
            ])
        } else {
            throw new ForbiddenException()
        }
    }

    def adminClass(Long adminClassId) {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 20
        def termId = termService.activeTerm.id
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            renderJson([
                    termId      : termId,
                    adminClasses: attendanceService.adminClassesByDepartment(termId, securityService.departmentId),
                    students    : attendanceService.studentStatsByAdminClass(termService.activeTerm.id, adminClassId, offset, max)
            ])
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') || securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            renderJson([
                    termId      : termId,
                    adminClasses: attendanceService.adminClassesByAdministrator(termId, securityService.userId),
                    students    : attendanceService.studentStatsByAdminClass(termService.activeTerm.id, adminClassId, offset, max)
            ])
        } else {
            throw new ForbiddenException()
        }
    }

    /**
     * 学生本人查看
     * @param studentId 学号
     */
    @PreAuthorize('hasAuthority("PERM_ATTENDANCE_ITEM")')
    def student(String studentId) {
        if (studentId == securityService.userId) {
            renderJson([
                    list: attendanceService.getStudentAttendances(studentId, termService.activeTerm.id),
            ])
        } else {
            throw new ForbiddenException()
        }
    }
}
