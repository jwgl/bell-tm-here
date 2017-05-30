package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.organization.StudentService
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 按部门/班主任/辅导员查询考勤情况。
 */
@PreAuthorize('hasAuthority("PERM_ATTENDANCE_LIST")')
class AttendanceController implements ServiceExceptionHandler {
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    StudentService studentService

    /**
     * 管理人员查看考勤统计
     * @return
     */
    def index() {
        def termId = termService.activeTerm.id
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            renderJson attendanceService.studentStatsByDepartment(termId, securityService.departmentId)
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') || securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            renderJson attendanceService.studentStatsByAdministrator(termId, securityService.userId)
        } else {
            throw new ForbiddenException()
        }
    }

    /**
     * 管理人员查看考勤详情
     * @param id 学号
     */
    def show(String id) {
        def termId = termService.activeTerm.id
        def userId = securityService.userId
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN') &&
            securityService.departmentId == studentService.getDepartment(id)?.id ||
            securityService.hasRole('ROLE_STUDENT_COUNSELLOR') &&
            userId == studentService.getCounsellor(id)?.id ||
            securityService.hasRole('ROLE_CLASS_SUPERVISOR') &&
            userId == studentService.getSupervisor(id)?.id) {
            renderJson attendanceService.getStudentAttendances(termId, id)
        } else {
            throw new ForbiddenException()
        }
    }

    /**
     * 获取教学班学生考勤统计
     * @param adminClassId 教学班ID
     */
    def adminClass(Long adminClassId) {
        def termId = termService.activeTerm.id
        renderJson attendanceService.studentStatsByAdminClass(termId, adminClassId)
    }

    /**
     * 获取教学班统计
     */
    def adminClasses() {
        def termId = termService.activeTerm.id
        def adminClasses
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            adminClasses = attendanceService.adminClassesByDepartment(termId, securityService.departmentId)
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') || securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            adminClasses = attendanceService.adminClassesByAdministrator(termId, securityService.userId)
        } else {
            throw new ForbiddenException()
        }

        renderJson([termId: termId, adminClasses: adminClasses])
    }

    /**
     * 学生本人查看
     * @param studentId 学号
     */
    @PreAuthorize('hasAuthority("PERM_ATTENDANCE_ITEM")')
    def student(String studentId) {
        def termId = termService.activeTerm.id
        if (studentId == securityService.userId) {
            renderJson attendanceService.getStudentAttendances(termId, studentId)
        } else {
            throw new ForbiddenException()
        }
    }
}
