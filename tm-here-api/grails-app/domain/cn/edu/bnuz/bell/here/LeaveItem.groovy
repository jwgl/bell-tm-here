package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskSchedule

/**
 * 假条项。可以按周、天、安排请假。
 * <ul>
 *   <li>如果按周请假，则week不为空，day和arrangement为空</li>
 *   <li>如果按天请假，则week和dayOfWeek不为空，arrangement为空</li>
 *   <li>如果按安排请假，则week和arrangement不为空，dayOfWeek为空</li>
 * </ul>
 */
class LeaveItem implements Comparable<LeaveItem> {

    /**
     * 周次
     */
    Integer week

    /**
     * 按天请假。
     */
    Integer dayOfWeek

    /**
     * 按安排请假
     */
    TaskSchedule taskSchedule

    static belongsTo = [leaveRequest : LeaveRequest]

    static mapping = {
        id comment: '请假项ID'
        week comment: '周次'
        dayOfWeek comment: '星期几'
        taskSchedule comment: '安排'
    }

    static constraints = {
        dayOfWeek nullable: true
        taskSchedule nullable: true
    }

    @Override
    int compareTo(LeaveItem o) {
        if (dayOfWeek == null && o.dayOfWeek == null) {
            if (taskSchedule == null && o.taskSchedule == null) {
                week <=> o.week
            } else if (taskSchedule != null && o.taskSchedule != null) {
                week <=> o.week ?:
                taskSchedule.dayOfWeek <=> o.taskSchedule.dayOfWeek ?:
                taskSchedule.startSection <=> o.taskSchedule.startSection
            } else {
                -1
            }
        } else if (dayOfWeek != null && o.dayOfWeek != null) {
            week <=> o.week ?:
            dayOfWeek <=> o.dayOfWeek
        } else {
            -1
        }
    }
}
