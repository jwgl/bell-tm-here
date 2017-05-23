package cn.edu.bnuz.bell.here.dto

import grails.gorm.hibernate.HibernateEntity

/**
 * 行政班考勤人数
 */
class AdminClassStats implements HibernateEntity<AdminClassStats> {
    /**
     * 行政班ID
     */
    Long id

    /**
     * 姓名
     */
    String name

    /**
     * 存在考勤数据的学生数
     */
    Long count

    static mapping = {
        table 'dv_dumb'
    }

    /**
     * 按学院统计教学班学生数量
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 教学班学生数
     */
    static statsByDepartment(Integer termId, String departmentId) {
        findAllWithSql("select * from sp_get_admin_class_attendance_stats_by_department($termId, $departmentId)")
    }

    /**
     * 按班主任或辅导员统计教学班学生数量
     * @param termId 学期
     * @param userId 用户ID
     * @return 教学班学生数
     */
    static statsByAdministrator(Integer termId, String userId) {
        findAllWithSql("select * from sp_get_admin_class_attendance_stats_by_administrator($termId, $userId)")
    }
}
