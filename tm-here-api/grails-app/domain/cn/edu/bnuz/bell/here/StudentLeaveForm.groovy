package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher

/**
 * 学生请假
 */
class StudentLeaveForm {
    /**
     * 学生
     */
    Student student

    /**
     * 学期
     */
    Term term

    /**
     * 请假类型
     */
    Integer type

    /**
     * 事由
     */
    String reason

    /**
     * 状态
     */
    Integer status

    /**
     * 创建时间
     */
    Date dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 批准人
     */
    Teacher approver

    /**
     * 批准时间
     */
    Date dateApproved

    static belongsTo = [student: Student]

    static hasMany = [items: StudentLeaveItem]

    static mapping = {
        id              generator: 'identity', comment: '假条ID'
        student         comment: '学生'
        term            comment: '学期'
        reason          comment: '事由'
        type            comment: '类别'
        status          comment: '状态'
        dateCreated     comment: '创建时间'
        dateModified    comment: '修改时间'
        approver        comment: '审批人'
        dateApproved    comment: '批准时间'
    }

    static constraints = {
        dateApproved nullable: true
        approver     nullable: true
    }
}
