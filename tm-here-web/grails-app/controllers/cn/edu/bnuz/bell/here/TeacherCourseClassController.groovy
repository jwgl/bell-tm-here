package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.report.ReportResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAnyAuthority("PERM_COURSE_CLASS_EXECUTE", "PERM_EXAM_DISQUAL_DEPT_ADMIN")')
class TeacherCourseClassController {
    ReportClientService reportClientService

    def index() { }

    def report(String teacherId, String teacherCourseClassId) {
        def courseClassCode = reportClientService.restTemplate.getForObject(
                'http://tm-here-api/teachers/{teacherId}/courseClasses/{courseClassId}/code',
                String,
                [
                        teacherId    : teacherId,
                        courseClassId: teacherCourseClassId,
                ]
        )

        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'attendance-statis-by-course-class',
                format: 'xlsx',
                parameters: [
                        courseClassId  : teacherCourseClassId,
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
