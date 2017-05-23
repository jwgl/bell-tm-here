package cn.edu.bnuz.bell.here.dto

import grails.gorm.hibernate.HibernateEntity

/**
 * 教学班学生考勤统计，单位：节次
 */
class CourseClassAttendanceStats implements HibernateEntity<CourseClassAttendanceStats> {
    /**
     * 学号
     */
    String id

    /**
     * 旷课
     */
    Long absent

    /**
     * 迟到
     */
    BigDecimal late

    /**
     * 早退
     */
    Long early

    /**
     * 折合
     */
    BigDecimal total

    /**
     * 请假
     */
    Long leave

    static mapping = {
        table 'dv_dumb'
    }

    /**
     * 按教学班统计考勤节数
     * @param courseClassId 教学班统计
     * @return 考勤统计
     */
    static statsByCourseClass(UUID courseClassId) {
        findAllWithSql("select * from sp_get_student_attendance_stats_by_course_class($courseClassId)")
    }
}
