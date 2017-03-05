package cn.edu.bnuz.bell.here

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_ATTENDANCE_LIST")')
class AttendanceController {
    def index() { }

    def show() { }

    @PreAuthorize('hasAuthority("PERM_ATTENDANCE_ITEM")')
    def student() { }
}
