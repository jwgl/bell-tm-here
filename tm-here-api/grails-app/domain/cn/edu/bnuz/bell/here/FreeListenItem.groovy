package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskSchedule
import org.apache.commons.lang.builder.HashCodeBuilder

class FreeListenItem implements Serializable {
    private static final long serialVersionUID = 1

    /**
     * 教学安排
     */
    TaskSchedule taskSchedule

    static belongsTo = [form : FreeListenForm]

    static mapping = {
        comment      '免听项'
        id           composite: ['form', 'taskSchedule'], comment: '免听项ID'
        form         comment: '免听申请'
        taskSchedule comment: '教学安排'
    }

    boolean equals(other) {
        if (!(other instanceof FreeListenItem)) {
            return false
        }

        other.form?.id == form?.id && other.taskSchedule?.id == taskSchedule?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (form)
            builder.append(form.id)
        if (taskSchedule)
            builder.append(taskSchedule.id)
        builder.toHashCode()
    }
}
