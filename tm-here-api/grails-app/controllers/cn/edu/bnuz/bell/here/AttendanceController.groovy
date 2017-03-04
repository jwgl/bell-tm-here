package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.tm.common.master.TermService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_ATTENDANCE_LIST")')
class AttendanceController implements ServiceExceptionHandler{
    AttendanceService attendanceService
    TermService termService

    def index() {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 20
        renderJson attendanceService.getAll(termService.activeTerm.id, offset, max)
    }

    def department(String departmentId) {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 20
        renderJson attendanceService.getByDepartment(termService.activeTerm.id, departmentId, offset, max)
    }


    def adminClass(Long adminClassId) {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 20
        renderJson attendanceService.getByAdminClass(termService.activeTerm.id, adminClassId, offset, max)
    }

    @PreAuthorize('hasAnyAuthority("PERM_ATTENDANCE_LIST", "PERM_ATTENDANCE_ITEM")')
    def student(String studentId) {
        renderJson attendanceService.getStudentAttendances(studentId, termService.activeTerm.id)
    }
}
