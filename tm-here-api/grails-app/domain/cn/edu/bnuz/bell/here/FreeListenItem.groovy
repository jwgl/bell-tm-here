package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskSchedule

class FreeListenItem {
    /**
     * 教学安排
     */
    TaskSchedule taskSchedule

    static belongsTo = [form : FreeListenForm]

    static mapping = {
        comment      '免听项'
        id           generator: 'identity', comment: '免听项ID'
        form         comment: '免听申请'
        taskSchedule comment: '教学安排'
    }
}
