package cn.edu.bnuz.bell.here


import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

import java.text.SimpleDateFormat

@PreAuthorize('hasAuthority("PERM_ATTENDANCE_CLASS_ADMIN")')
class TeacherAdminClassController {
    AttendanceService attendanceService
    TermService termService
    SecurityService securityService
    ReportClientService reportClientService

    def index(String teacherId, Integer termId) {
        renderJson attendanceService.adminClassesByAdministrator(termId, teacherId)
    }

    def attendances(String teacherId, Long teacherAdminClassId, Integer termId) {
        renderJson attendanceService.studentStatsByAdminClass(termId, teacherAdminClassId)
    }

    def allAttendances(String teacherId, Integer termId) {
        renderJson attendanceService.studentStatsByAdministrator(termId, teacherId)
    }

    def statisReport(String teacherId, Integer termId) {
        def reportRequest = new ReportRequest(
                reportId: "$termId-$dateSerialNo",
                reportName: 'attendance-statis-by-administrator',
                parameters: [termId: termId, userId: teacherId],
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    def detailReport(String teacherId, Integer termId) {
        def reportRequest = new ReportRequest(
                reportId: "$termId-$dateSerialNo",
                reportName: 'attendance-detail-by-administrator',
                parameters: [termId: termId, userId: teacherId],
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    private String getDateSerialNo() {
        new SimpleDateFormat('yyyyMMdd-hhmmss').format(new Date())
    }
}
