package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class StudentLeaveFormInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.studentId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
