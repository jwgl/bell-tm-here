package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler

class TeacherSettingController implements ServiceExceptionHandler {
    TeacherSettingService teacherSettingService

    def update(String teacherId, String id) {
        switch (id) {
            case 'rollcall':
                switch (params.type) {
                    case 'settings':
                        teacherSettingService.setRollcallSettings(teacherId, request.JSON as Map)
                        break
                    case 'view':
                        teacherSettingService.setRollcallView(teacherId, request.JSON.view as String)
                        break
                }
                return renderOk()
            default:
                throw new BadRequestException()
        }
    }
}
