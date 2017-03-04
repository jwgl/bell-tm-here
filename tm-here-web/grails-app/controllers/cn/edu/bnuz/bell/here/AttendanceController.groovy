package cn.edu.bnuz.bell.here

import org.springframework.security.access.prepost.PreAuthorize

class AttendanceController {
    @PreAuthorize('hasAuthority("PERM_ATTENDANCE_LIST")')
    def index() { }

    @PreAuthorize('hasAnyAuthority("PERM_ATTENDANCE_LIST", "PERM_ATTENDANCE_ITEM")')
    def show() { }
}
