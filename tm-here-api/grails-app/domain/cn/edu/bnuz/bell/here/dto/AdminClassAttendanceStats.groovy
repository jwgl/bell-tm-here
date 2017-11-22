package cn.edu.bnuz.bell.here.dto

import grails.gorm.hibernate.HibernateEntity

/**
 * 行政班学生考勤统计，单位：节次
 */
class AdminClassAttendanceStats implements HibernateEntity<AdminClassAttendanceStats> {
    /**
     * 学号
     */
    String id

    /**
     * 姓名
     */
    String name

    /**
     * 班级
     */
    String adminClass

    /**
     * 旷课
     */
    BigDecimal absent

    /**
     * 迟到
     */
    BigDecimal late

    /**
     * 早退
     */
    BigDecimal early

    /**
     * 折合
     */
    BigDecimal total

    /**
     * 请假
     */
    BigDecimal leave

    static mapping = {
        table 'dv_dumb'
    }

    /**
     * 按学院统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 考勤统计
     */
    static statsByDepartment(Integer termId, String departmentId) {
        findAllWithSql("select * from sp_get_student_attendance_stats_by_department($termId, $departmentId)")
    }

    /**
     * 按班主任或辅导员统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 考勤统计
     */
    static statsByAdministrator(Integer termId, String userId) {
        findAllWithSql("select * from sp_get_student_attendance_stats_by_administrator($termId, $userId)")
    }

    /**
     * 按行政班统计学生考勤
     * @param termId 学期
     * @param adminClassId 学院ID
     * @return 考勤统计
     */
    static statsByAdminClass(Integer termId, Long adminClassId) {
        findAllWithSql("select * from sp_get_student_attendance_stats_by_admin_class($termId, $adminClassId)")
    }
}
