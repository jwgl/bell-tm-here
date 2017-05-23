package cn.edu.bnuz.bell.here.dto

import grails.gorm.hibernate.HibernateEntity

/**
 * 学生考勤详情
 */
class StudentAttendance implements HibernateEntity<StudentAttendance> {
    /**
     * 记录ID
     */
    Long id

    /**
     * 周次
     */
    Integer week

    /**
     * 星期几
     */
    Integer dayOfWeek

    /**
     * 开始节
     */
    Integer startSection

    /**
     * 上课长度
     */
    Integer totalSection

    /**
     * 考勤类型/请假类型
     */
    Integer type

    /**
     * 课程名称
     */
    String course

    /**
     * 课程项名称
     */
    String courseItem

    /**
     * 考勤教师/批假教师
     */
    String teacher

    /**
     * 请假单号
     */
    Long studentLeaveFormId

    /**
     * 免听单号
     */
    Long freeListenFormId

    static mapping = {
        table 'dv_dumb'
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param termId 学期
     * @param studentId 学生ID
     * @return 考勤情况
     */
    static findRollcalls(Integer termId, String studentId) {
        findAllWithSql("select * from sp_get_rollcall_details_by_student($termId, $studentId)")
    }

    /**
     * 获取学生指定教学班的考勤情况
     * @param courseClassId 教学班ID
     * @param studentId 学生ID
     * @return 考勤情况
     */
    static findRollcalls(UUID courseClassId, String studentId) {
        findAllWithSql("select * from sp_get_rollcall_details_by_course_class_student($courseClassId, $studentId)")
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param termId 学期
     * @param studentId 学生ID
     * @return 考勤情况
     */
    static findLeaves(Integer termId, String studentId) {
        findAllWithSql("select * from sp_get_student_leave_details_by_student($termId, $studentId)")
    }

    /**
     * 获取学生指定教学班的考勤情况
     * @param courseClassId 教学班ID
     * @param studentId 学生ID
     * @return 考勤情况
     */
    static findLeaves(UUID courseClassId, String studentId) {
        findAllWithSql("select * from sp_get_student_leave_details_by_course_class_student($courseClassId, $studentId)")
    }
}
