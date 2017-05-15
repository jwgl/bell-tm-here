package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.report.ReportResponse
import org.springframework.http.HttpStatus

class CourseClassController {
    ReportClientService reportClientService

    def index() { }

    def report(String teacherId, String courseClassId) {
        def courseClassCode = reportClientService.restTemplate.getForObject(
                'http://tm-here-api/teachers/{teacherId}/courseClasses/{courseClassId}/code',
                String,
                [
                        teacherId    : teacherId,
                        courseClassId: courseClassId,
                ]
        )

        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'attendance-statis-by-course-class',
                format: 'xlsx',
                parameters: [
                        courseClassId  : courseClassId,
                        courseClassCode: courseClassCode,
                        idKey          : 'courseClassCode'
                ]
        ))

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }
}
