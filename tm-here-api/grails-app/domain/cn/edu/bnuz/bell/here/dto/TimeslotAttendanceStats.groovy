package cn.edu.bnuz.bell.here.dto

import cn.edu.bnuz.bell.here.Rollcall
import cn.edu.bnuz.bell.here.TeacherTimeslotCommand
import grails.gorm.hibernate.HibernateEntity

/**
 * 时间段考勤统计，单位：次数
 */
class TimeslotAttendanceStats implements HibernateEntity<TimeslotAttendanceStats> {
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
    Long late

    /**
     * 早退
     */
    Long early

    /**
     * 请假
     */
    Long leave

    static mapping = {
        table 'dv_dumb'
    }

    /**
     * 按时段命令统计考勤次数，按教学班汇总
     * @param cmd 时段
     */
    static statsByTimeslot(TeacherTimeslotCommand cmd) {
        findAllWithSql("select * from sp_get_student_attendance_stats_by_timeslot($cmd.termId, $cmd.teacherId, $cmd.week, $cmd.dayOfWeek, $cmd.startSection, $cmd.totalSection)")
    }

    /**
     * 按安排统计指定学生的教学班考勤次数统计
     * @param rollcall 考勤
     */
    static statsByRollcall(Rollcall rollcall) {
        findWithSql("select * from sp_get_student_attendance_stats_by_task_schedule_student($rollcall.taskScheduleId, $rollcall.student.id)")
    }
}
