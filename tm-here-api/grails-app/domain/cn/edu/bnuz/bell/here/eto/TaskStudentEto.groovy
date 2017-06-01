package cn.edu.bnuz.bell.here.eto

/**
 * 外部学生选课表，只有对examFlag的更新权限
 */
class TaskStudentEto {
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
    Boolean locked

    static mapping = {
        table name: 'et_task_student'
    }
}
