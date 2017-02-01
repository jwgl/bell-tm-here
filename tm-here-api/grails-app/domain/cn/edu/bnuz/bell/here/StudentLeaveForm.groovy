package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

/**
 * 学生请假
 */
class StudentLeaveForm implements StateObject {
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
    State status

    /**
     * 创建时间
     */
    Date dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 修改时间
     */
    Date dateSubmitted

    /**
     * 批准人
     */
    Teacher approver

    /**
     * 批准时间
     */
    Date dateApproved

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static belongsTo = [student: Student]

    static hasMany = [items: StudentLeaveItem]

    static mapping = {
        comment          '学生请假'
        dynamicUpdate    true
        id               generator: 'identity', comment: '假条ID'
        student          comment: '学生'
        term             comment: '学期'
        type             comment: '请假类型'
        reason           length: 250, comment: '请假事由'
        status           sqlType: 'state', type: StateUserType, comment: '状态'
        dateCreated      comment: '创建时间'
        dateModified     comment: '修改时间'
        dateSubmitted    comment: '提交时间'
        approver         comment: '审批人'
        dateApproved     comment: '批准时间'
        workflowInstance comment: '工作流实例'
    }

    static constraints = {
        dateSubmitted    nullable: true
        dateApproved     nullable: true
        approver         nullable: true
        workflowInstance nullable: true
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'student.leave'
}
