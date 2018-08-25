package cn.edu.bnuz.bell.here


import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_ATTENDANCE_DEPT_ADMIN")')
class DepartmentAdminClassController {
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    ReportClientService reportClientService

    def index(Integer termId) {
        renderJson attendanceService.adminClassesByDepartment(termId, securityService.departmentId)
    }

    def attendances(String departmentId, Long departmentAdminClassId, Integer termId) {
        renderJson attendanceService.studentStatsByAdminClass(termId, departmentAdminClassId)
    }

    def allAttendances(String departmentId, Integer termId) {
        renderJson attendanceService.studentStatsByDepartment(termId, departmentId)
    }

    def statisReport(String departmentId, Integer termId) {
        def reportRequest = new ReportRequest(
                reportId: "${termId}-${new Date().format('yyyyMMdd-hhmmss')}",
                reportName: 'attendance-statis-by-department',
                parameters: [termId: termId, departmentId: departmentId],
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    def detailReport(String departmentId, Integer termId) {
        def reportRequest = new ReportRequest(
                reportId: "${termId}-${new Date().format('yyyyMMdd-hhmmss')}",
                reportName: 'attendance-detail-by-department',
                parameters: [termId: termId, departmentId: departmentId],
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    def disqualReport(String departmentId, Integer termId) {
        def reportRequest = new ReportRequest(
                reportId: "${termId}-${new Date().format('yyyyMMdd-hhmmss')}",
                reportName: 'exam-disqual-by-student-department',
                parameters: [termId: termId, departmentId: departmentId],
        )
        reportClientService.runAndRender(reportRequest, response)
    }
}
