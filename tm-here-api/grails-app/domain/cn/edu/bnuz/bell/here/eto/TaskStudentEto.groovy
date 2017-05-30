package cn.edu.bnuz.bell.here.eto

/**
 * 外部学生选课表，只有对examFlag的更新权限
 */
class TaskStudentEto {
    String taskCode
    String studentId
    String examFlag

    static mapping = {
        table name: 'et_task_student'
    }
}
