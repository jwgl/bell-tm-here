package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class TeacherSettingInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.teacherId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
