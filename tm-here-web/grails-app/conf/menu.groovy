menuGroup 'main', {
    process 20, {
        rollcallForm           20, 'PERM_ROLLCALL_WRITE',          '/web/here/teachers/${userId}/rollcalls'
        courseClassAttendances 21, 'PERM_ROLLCALL_WRITE',          '/web/here/teachers/${userId}/courseClasses'
        attendanceList         22, 'PERM_ATTENDANCE_LIST',         '/web/here/attendances'
        attendanceItem         23, 'PERM_ATTENDANCE_ITEM',         '/web/here/students/${userId}/attendances'
        studentLeaveForm       30, 'PERM_STUDENT_LEAVE_WRITE',     '/web/here/students/${userId}/leaves'
        studentLeaveApproval   31, 'PERM_STUDENT_LEAVE_APPROVE',   '/web/here/approvers/${userId}/leaves'
        freeListenForm         41, 'PERM_FREE_LISTEN_WRITE',       '/web/here/students/${userId}/freeListens'
        freeListenCheck        42, 'PERM_FREE_LISTEN_CHECK',       '/web/here/teachers/${userId}/freeListens'
        freeListenApproval     43, 'PERM_FREE_LISTEN_APPROVE',     '/web/here/approvers/${userId}/freeListens'
        departmentExamDisqual  51, 'PERM_EXAM_DISQUAL_DEPT_ADMIN', '/web/here/departments/${departmentId}/disquals'
    }
}