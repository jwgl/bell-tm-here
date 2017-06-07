package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler

class CourseClassStudentController implements ServiceExceptionHandler {
    CourseClassStudentService courseClassStudentService

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
