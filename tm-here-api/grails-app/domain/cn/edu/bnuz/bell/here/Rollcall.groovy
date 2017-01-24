package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import org.joda.time.DateTime

/**
 * 考勤记录。
 * 以下情况考勤记录无效：
 * 1、如果因退课或教学班调整，学生不在此教学班；
 * 2、如果学生请假涵盖此考勤，并批准；
 * 3、学生申请免听，并批准；
 * 4、教师申请考勤变更，变更类型为删除，并批准。
 * 创建考勤记录时，应检查以上情况，避免因并发引起不必要的插入操作。
 */
class Rollcall {
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
     * 创建时间
     */
    Date dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    static mapping = {
        id generator: 'identity', comment: '考勤ID'
        teacher comment: '考勤教师'
        student comment: '学生'
        taskSchedule comment: '安排'
        week comment: '周次'
        type comment: '类型'
        dateCreated comment: '创建时间'
        dateModified comment: '修改时间'
    }
}
