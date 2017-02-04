package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_STUDENT_LEAVE_READ")')
class StudentLeavePublicController implements ServiceExceptionHandler {
    StudentLeavePublicService studentLeavePublicService
    SecurityService securityService

    def show(Long id) {
        renderJson studentLeavePublicService.getFormForShow(securityService.userId, id)
    }
}
