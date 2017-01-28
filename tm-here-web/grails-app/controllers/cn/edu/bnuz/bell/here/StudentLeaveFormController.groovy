package cn.edu.bnuz.bell.here

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_STUDENT_LEAVE_WRITE")')
class StudentLeaveFormController {
    def index() { }
}
