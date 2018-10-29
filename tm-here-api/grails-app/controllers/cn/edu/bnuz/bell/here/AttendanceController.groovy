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
@PreAuthorize('hasAnyAuthority("PERM_EXAM_DISQUAL_DEPT_ADMIN", "PERM_ATTENDANCE_DEPT_ADMIN", "PERM_ATTENDANCE_CLASS_ADMIN")')
class AttendanceController implements ServiceExceptionHandler {
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    StudentService studentService
    AttendanceTermService attendanceTermService

    /**
     * 管理人员查看考勤详情
     * @param id 学号
     */
    def show(String id, Integer termId) {
        if (securityService.hasRole('ROLE_STUDENT') && securityService.userId == id ||
                securityService.hasRole('ROLE_ACADEMIC_SECRETARY') && securityService.departmentId == studentService.getDepartment(id)?.id ||
                securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN') && securityService.departmentId == studentService.getDepartment(id)?.id ||
                securityService.hasRole('ROLE_STUDENT_COUNSELLOR') && securityService.userId == studentService.getCounsellor(id)?.id ||
                securityService.hasRole('ROLE_CLASS_SUPERVISOR') && securityService.userId == studentService.getSupervisor(id)?.id) {
            renderJson attendanceService.getStudentAttendances(termId, id)
        } else {
            throw new ForbiddenException()
        }
    }

    /**
     * 获取考勤学期
     */
    def terms() {
        renderJson attendanceTermService.getTerms()
    }
}
