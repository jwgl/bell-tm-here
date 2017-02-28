package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.transaction.Transactional

import javax.annotation.Resource

@Transactional
class StudentLeaveApprovalService {
    StudentLeaveFormService studentLeaveFormService
    ScheduleService scheduleService
    DataAccessService dataAccessService

    @Resource(name='studentLeaveFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    /**
     * 各状态申请数量
     * @return 各状态申请数量
     */
    def getCounts(String userId) {
        def todo = dataAccessService.getLong '''
select count(*)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
  and adminClass.counsellor.id = :userId
''', [userId: userId, status: State.SUBMITTED]

        def done = dataAccessService.getLong '''
select count(*)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.approver.id = :userId
''', [userId: userId]

        def next = dataAccessService.getLong '''
select count(*)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
  and form.approver.id = :userId
''', [userId: userId, status: State.FINISHED]

        [
                (ListType.TODO): todo,
                (ListType.DONE): done,
                (ListType.NEXT): next,
        ]
    }

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(userId, cmd.args)
            case ListType.DONE:
                return findDoneList(userId, cmd.args)
            case ListType.NEXT:
                return findNextList(userId, cmd.args)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String userId, Map args) {
        def forms = StudentLeaveForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  adminClass.name as adminClass,
  form.type as type,
  substring(form.reason, 1, 20) as reason,
  form.dateSubmitted as date,
  form.status as status
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
  and adminClass.counsellor.id = :userId
order by form.dateSubmitted
''', [userId: userId, status: State.SUBMITTED], args

        return [forms: forms, counts: getCounts(userId)]
    }

    def findDoneList(String userId, Map args) {
        def forms = StudentLeaveForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  adminClass.name as adminClass,
  form.type as type,
  substring(form.reason, 1, 20) as reason,
  form.dateApproved as date,
  form.status as status
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.approver.id = :userId
order by form.dateApproved desc
''', [userId: userId], args

        return [forms: forms, counts: getCounts(userId)]
    }

    def findNextList(String userId, Map args) {
        def forms = StudentLeaveForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  adminClass.name as adminClass,
  form.type as type,
  substring(form.reason, 1, 20) as reason,
  form.dateApproved as date,
  form.status as status
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
  and form.approver.id = :userId
order by form.dateApproved desc
''', [userId: userId, status: State.FINISHED], args

        return [forms: forms, counts: getCounts(userId)]
    }

    def getFormForReview(String userId, Long id, ListType type) {
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
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
        ]
    }

    def getFormForReview(String userId, Long id, ListType type, UUID workitemId) {
        def form = studentLeaveFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        def schedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        return [
                form: form,
                schedules: schedules,
                counts: getCounts(userId),
                workitemId: workitemId,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
        ]
    }

    Long getPrevReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
and adminClass.counsellor.id = :userId
and form.dateSubmitted < (select dateSubmitted from StudentLeaveForm where id = :id)
order by form.dateSubmitted desc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
where form.approver.id = :userId
and form.dateApproved > (select dateApproved from StudentLeaveForm where id = :id)
order by form.dateApproved asc
''', [userId: userId, id: id])
            case ListType.NEXT:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
where form.status = :status
  and form.approver.id = :userId
and form.dateApproved > (select dateApproved from StudentLeaveForm where id = :id)
order by form.dateApproved asc
''', [userId: userId, id: id, status: State.FINISHED])
        }
    }

    Long getNextReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
and adminClass.counsellor.id = :userId
and form.dateSubmitted > (select dateSubmitted from StudentLeaveForm where id = :id)
order by form.dateSubmitted asc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
where form.approver.id = :userId
and form.dateApproved < (select dateApproved from StudentLeaveForm where id = :id)
order by form.dateApproved desc
''', [userId: userId, id: id])
            case ListType.NEXT:
                return dataAccessService.getLong('''
select form.id
from StudentLeaveForm form
where form.status = :status
  and form.approver.id = :userId
and form.dateApproved < (select dateApproved from StudentLeaveForm where id = :id)
order by form.dateApproved desc
''', [userId: userId, id: id, status: State.FINISHED])
        }
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
