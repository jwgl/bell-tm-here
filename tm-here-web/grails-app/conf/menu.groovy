menuGroup 'main', {
    process 20, {
        rollcall          10, 'PERM_ROLLCALL_WRITE',      '/web/here/teachers/${userId}/rollcalls'
        studentLeaveForm  20, 'PERM_STUDENT_LEAVE_WRITE', '/web/here/students/${userId}/leaves'
        studentLeaveAdmin 21, 'PERM_STUDENT_LEAVE_CHECK', '/web/here/leaves'
    }
}