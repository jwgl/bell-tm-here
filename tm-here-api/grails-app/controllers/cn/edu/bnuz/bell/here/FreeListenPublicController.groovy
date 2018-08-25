package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.system.SystemConfigService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAnyAuthority("PERM_FREE_LISTEN_READ", "PERM_FREE_LISTEN_WRITE")')
class FreeListenPublicController implements ServiceExceptionHandler {
    FreeListenPublicService freeListenPublicService
    SecurityService securityService
    TermService termService
    SystemConfigService systemConfigService

    def show(Long id) {
        renderJson freeListenPublicService.getFormForShow(securityService.userId, id)
    }

    def settings() {
        renderJson freeListenPublicService.getSettings(termService.activeTerm)
    }

    def notice() {
        renderJson([notice: systemConfigService.get(FreeListenForm.CONFIG_NOTICE, '')])
    }
}
