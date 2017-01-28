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
class StudentLeaveItem implements Comparable<StudentLeaveItem> {

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

    static belongsTo = [form : StudentLeaveForm]

    static mapping = {
        id           generator: 'identity', comment: '请假项ID'
        form         comment: '请假'
        week         comment: '周次'
        dayOfWeek    comment: '星期几'
        taskSchedule comment: '安排'
    }

    static constraints = {
        dayOfWeek    nullable: true
        taskSchedule nullable: true
    }

    @Override
    int compareTo(StudentLeaveItem o) {
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
