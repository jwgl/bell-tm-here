package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_READ")')
class FreeListenPublicController {
    FreeListenPublicService freeListenPublicService
    SecurityService securityService

    def show(Long id) {
        renderJson freeListenPublicService.getFormForShow(securityService.userId, id)
    }
}
