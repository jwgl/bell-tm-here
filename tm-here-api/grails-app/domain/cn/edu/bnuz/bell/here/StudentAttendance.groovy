package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher

/**
 * 学生出勤情况。
 * 如果类型为旷课、迟到、早退和迟到+早退，且存在对应的请假或免听表单，
 * 则考勤数据无效，不计入统计。
 */
class StudentAttendance {
    /**
     * 学期ID
     */
    Term term

    /**
     * 学生
     */
    Student student

    /**
     * 周次
     */
    Integer week

    /**
     * 安排
     */
    TaskSchedule taskSchedule

    /**
     * 类别：1-旷课；2-迟到；3-早退；4-请假；5-迟到+早退；
     */
    Integer type

    /**
     * 请假
     */
    StudentLeaveForm studentLeaveForm

    /**
     * 免听
     */
    FreeListenForm freeListenForm

    /**
     * 是否有效
     */
    Boolean valid

    /**
     * 考勤或批假教师
     */
    Teacher teacher

    static mapping = {
        table 'dv_student_attendance'
    }

    static constraints = {
        studentLeaveForm nullable: true
        freeListenForm   nullable: true
    }
}
