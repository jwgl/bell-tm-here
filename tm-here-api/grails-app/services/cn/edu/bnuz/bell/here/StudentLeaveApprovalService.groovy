package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService
import cn.edu.bnuz.bell.workflow.*
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.transaction.Transactional

import javax.annotation.Resource

@Transactional
class StudentLeaveApprovalService {
    StudentLeaveFormService studentLeaveFormService
    ScheduleService scheduleService

    @Resource(name='studentLeaveFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    /**
     * 各状态申请数量
     * @return 各状态申请数量
     */
    def getCounts(String userId) {
        def results = StudentLeaveForm.executeQuery '''
select form.status, count(*)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where adminClass.counsellor.id = :userId
group by status
''', [userId: userId]
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
        def forms = StudentLeaveForm.executeQuery '''
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
''', [userId: userId, status: status], [offset: offset, max: max]

        return [forms: forms, counts: getCounts(userId)]
    }

    def getFormForReview(String userId, Long id) {
        def form = studentLeaveFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${StudentLeaveForm.WORKFLOW_ID}.${Activities.APPROVE}"),
                User.load(userId),
        )
        domainStateMachineHandler.checkReviewer(id, userId, Activities.APPROVE)

        def schedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        return [
                form: form,
                schedules: schedules,
                counts: getCounts(userId),
                workitemId: workitem ? workitem.id : null,
        ]
    }

    def getFormForReview(String userId, Long id, UUID workitemId) {
        def form = studentLeaveFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        def schedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        return [
                form: form,
                schedules: schedules,
                counts: getCounts(userId),
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
        domainStateMachineHandler.accept(form, userId, Activities.APPROVE, cmd.comment, workitemId, form.student.id)
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
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
        domainStateMachineHandler.reject(form, userId, Activities.APPROVE, cmd.comment, workitemId)
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
        form.save()
    }
}
