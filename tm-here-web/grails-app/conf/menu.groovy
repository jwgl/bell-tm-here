menuGroup 'main', {
    process 20, {
        rollcall             10, 'PERM_ROLLCALL_WRITE',        '/web/here/teachers/${userId}/rollcalls'
        studentLeaveForm     20, 'PERM_STUDENT_LEAVE_WRITE',   '/web/here/students/${userId}/leaves'
        studentLeaveApproval 21, 'PERM_STUDENT_LEAVE_APPROVE', '/web/here/approvers/${userId}/leaves'
        freeListenForm       31, 'PERM_FREE_LISTEN_WRITE',     '/web/here/students/${userId}/freeListens'
        freeListenCheck      31, 'PERM_FREE_LISTEN_CHECK',     '/web/here/teachers/${userId}/freeListens'
        freeListenApproval   32, 'PERM_FREE_LISTEN_APPROVE',   '/web/here/approvers/${userId}/freeListens'
    }
}