package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.report.ReportResponse
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class AttendanceReportController {
    SecurityService securityService
    ReportClientService reportClientService

    def statis() {
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            report(new ReportRequest(
                    reportService: 'tm-report',
                    reportName: 'attendance-statis-by-department',
                    format: 'xlsx',
                    parameters: [termId: params.termId, departmentId: securityService.departmentId]
            ))
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') ||
                   securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            report(new ReportRequest(
                    reportService: 'tm-report',
                    reportName: 'attendance-statis-by-administrator',
                    format: 'xlsx',
                    parameters: [termId: params.termId, userId: securityService.userId]
            ))
        } else {
            throw new ForbiddenException()
        }
    }

    def detail() {
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            report(new ReportRequest(
                    reportService: 'tm-report',
                    reportName: 'attendance-detail-by-department',
                    format: 'xlsx',
                    parameters: [termId: params.termId, departmentId: securityService.departmentId]
            ))
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') ||
                securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            report(new ReportRequest(
                    reportService: 'tm-report',
                    reportName: 'attendance-detail-by-administrator',
                    format: 'xlsx',
                    parameters: [termId: params.termId, userId: securityService.userId]
            ))
        } else {
            throw new ForbiddenException()
        }
    }

    private report(ReportRequest reportRequest) {
        ReportResponse reportResponse = reportClientService.runAndRender(reportRequest)

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }
}
