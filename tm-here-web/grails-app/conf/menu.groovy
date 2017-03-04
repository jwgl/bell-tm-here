menuGroup 'main', {
    process 20, {
        rollcallForm         10, 'PERM_ROLLCALL_WRITE',        '/web/here/teachers/${userId}/rollcalls'
        attendanceList       11, 'PERM_ATTENDANCE_LIST',       '/web/here/attendances'
        attendanceItem       12, 'PERM_ATTENDANCE_ITEM',       '/web/here/students/${userId}/attendances'
        studentLeaveForm     20, 'PERM_STUDENT_LEAVE_WRITE',   '/web/here/students/${userId}/leaves'
        studentLeaveApproval 21, 'PERM_STUDENT_LEAVE_APPROVE', '/web/here/approvers/${userId}/leaves'
        freeListenForm       31, 'PERM_FREE_LISTEN_WRITE',     '/web/here/students/${userId}/freeListens'
        freeListenCheck      31, 'PERM_FREE_LISTEN_CHECK',     '/web/here/teachers/${userId}/freeListens'
        freeListenApproval   32, 'PERM_FREE_LISTEN_APPROVE',   '/web/here/approvers/${userId}/freeListens'
    }
}