package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_COURSE_CLASS_READ")')
class CourseClassStudentController implements ServiceExceptionHandler {
    CourseClassStudentService courseClassStudentService

    @PreAuthorize('hasAnyAuthority("PERM_EXAM_DISQUAL_WRITE", "PERM_EXAM_DISQUAL_DEPT_ADMIN")')
    def patch(String courseClassId, String id, String op) {
        switch (op) {
            case 'DISQUALIFY':
                courseClassStudentService.disqualify(UUID.fromString(courseClassId), id)
                break
            case 'QUALIFY':
                courseClassStudentService.qualify(UUID.fromString(courseClassId), id)
                break
            default:
                throw new BadRequestException()
        }
        renderOk()
    }

    def attendances(String courseClassId, String courseClassStudentId) {
        renderJson courseClassStudentService.show(UUID.fromString(courseClassId), courseClassStudentId)
    }
}
