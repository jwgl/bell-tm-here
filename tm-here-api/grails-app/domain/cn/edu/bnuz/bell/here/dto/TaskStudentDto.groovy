package cn.edu.bnuz.bell.here.dto

import org.codehaus.groovy.util.HashCodeHelper

/**
 * 外部学生选课表，用于查询学生选课状态
 */
class TaskStudentDto implements Serializable {
    /**
     * 选课课号
     */
    String taskCode

    /**
     * 学号
     */
    String studentId

    /**
     * 考试标记
     */
    String examFlag

    /**
     * 已安排考试
     */
    Boolean testScheduled

    /**
     * 已锁定，成绩已录入
     */
    Boolean scoreCommitted

    static mapping = {
        table name: 'dv_task_student'
        id    composite: ['taskCode', 'studentId']
    }


    boolean equals(other) {
        if (!(other instanceof TaskStudentDto)) {
            return false
        }

        other.taskCode == taskCode && other.studentId == studentId
    }

    int hashCode() {
        int hash = HashCodeHelper.initHash()
        hash = HashCodeHelper.updateHash(hash, taskCode)
        hash = HashCodeHelper.updateHash(hash, studentId)
        hash
    }
}
