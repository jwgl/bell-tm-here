package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService
import cn.edu.bnuz.bell.workflow.AbstractReviewService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.transaction.Transactional

@Transactional
class StudentLeaveReviewService extends AbstractReviewService {
    StudentLeaveFormService studentLeaveFormService
    ScheduleService scheduleService
    DomainStateMachineHandler domainStateMachineHandler

    /**
     * 各状态申请数量
     * @return 各状态申请数量
     */
    def getCountsByStatus(String userId) {
        def results = StudentLeaveForm.executeQuery """
select form.status, count(*)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where adminClass.counsellor.id = :userId
group by status
""", [userId: userId]
        return results.collectEntries {[it[0].name(), it[1]]}
    }

    /**
     * 查找所有指定状态的申请（DTO）
     * @param status
     * @param offset
     * @param max
     * @return
     */
    def findAllByStatus(String userId, State status, int offset, int max) {
        StudentLeaveForm.executeQuery """
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  adminClass.name as adminClass,
  form.type as type,
  substring(form.reason, 1, 20) as reason,
  form.dateModified as applyDate,
  form.status as status
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
  and adminClass.counsellor.id = :userId 
order by form.dateSubmitted desc
""", [userId: userId, status: status], [offset: offset, max: max]
    }

    def getFormForReview(String userId, Long id) {
        def form = studentLeaveFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load('student.leave.approve'),
                User.load(userId),
        )
        if (workitem) {
            form.workitemId = workitem.id
        }

        checkReviewer(id, 'approve', userId)

        def schedules = scheduleService.getStudentSchedules(form.studentId, Term.get(form.term))

        return [
                schedules: schedules,
                form: form,
        ]
    }

    def getFormForReview(String userId, Long id, UUID workitemId) {
        def form = studentLeaveFormService.getFormInfo(id)

        def workitem = Workitem.get(workitemId)
        if (!workitem ||
            workitem.instance.id != form.workflowInstanceId ||
            workitem.to.id != userId) {
            throw new BadRequestException()
        }

        checkReviewer(id, workitem.activitySuffix, userId)

        def schedules = scheduleService.getStudentSchedules(form.studentId, Term.get(form.term))

        return [
                schedules: schedules,
                form: form,
        ]
    }

    /**
     * 同意
     * @param cmd 同意数据
     * @param userId 用户ID
     * @param workItemId 工作项ID
     */
    void accept(String userId, AcceptCommand cmd, UUID workitemId) {
        StudentLeaveForm form = StudentLeaveForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canAccept(form)) {
            throw new BadRequestException()
        }

        def activity = Workitem.get(workitemId).activitySuffix
        checkReviewer(cmd.id, activity, userId)

        domainStateMachineHandler.accept(form, userId, cmd.comment, workitemId)

        form.save()
    }

    /**
     * 不同意
     * @param cmd 不同意数据
     * @param userId 用户ID
     * @param workItemId 工作项ID
     */
    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        StudentLeaveForm form = StudentLeaveForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canReject(form)) {
            throw new BadRequestException()
        }

        def activity = Workitem.get(workitemId).activitySuffix
        checkReviewer(cmd.id, activity, userId)

        domainStateMachineHandler.reject(form, userId, cmd.comment, workitemId)

        form.save()
    }

    List<Map> getReviewers(String type, Long id) {
        switch (type) {
            case Activities.APPROVE:
                return studentLeaveFormService.approvers(id)
            default:
                throw new BadRequestException()
        }
    }
}
