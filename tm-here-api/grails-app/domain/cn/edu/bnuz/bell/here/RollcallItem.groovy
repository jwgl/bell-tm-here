package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher

class RollcallItem {
    /**
     * 考勤教师
     */
    Teacher teacher

    /**
     * 学生
     */
    Student student

    /**
     * 安排
     */
    TaskSchedule taskSchedule

    /**
     * 周次
     */
    Integer week

    /**
     * 类型
     */
    Integer type

    /**
     * 请假项，如果不为空，表示取消此次考勤
     */
    LeaveItem leaveItem

    static mapping = {
        id comment: '考勤项ID'
        teacher comment: '考勤教师'
        student comment: '学生'
        taskSchedule comment: '安排'
        week comment: '周次'
        type comment: '类型'
        leaveItem comment: '请假项'
    }

    static constraints = {
        leaveItem nullable: true
    }
}
