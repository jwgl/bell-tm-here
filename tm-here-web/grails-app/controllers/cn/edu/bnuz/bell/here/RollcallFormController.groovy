package cn.edu.bnuz.bell.here

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_ROLLCALL_WRITE")')
class RollcallFormController {
    def index() { }
}
