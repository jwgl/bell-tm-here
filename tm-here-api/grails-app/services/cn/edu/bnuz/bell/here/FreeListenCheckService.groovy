package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
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

import javax.annotation.Resource

@Transactional
class FreeListenCheckService extends AbstractReviewService {
    FreeListenFormService freeListenFormService
    ScheduleService scheduleService
    DataAccessService dataAccessService

    @Resource(name='freeListenFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    def getCounts(String teacherId) {
        def pending = dataAccessService.getInteger '''
select count(*)
from FreeListenForm form
where form.checker.id = :teacherId
and form.status = :status
''',[teacherId: teacherId, status: State.SUBMITTED]
        def processed = dataAccessService.getInteger '''
select count(*)
from FreeListenForm form
where form.checker.id = :teacherId
and form.dateChecked is not null
''',[teacherId: teacherId]
        return [
                PENDING: pending,
                PROCESSED: processed,
        ]
    }

    def findPendingForms(String teacherId, int offset, int max) {
        FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  subject.name as subject,
  major.grade as grade,
  form.dateSubmitted as date,
  form.reason as reason,
  form.status as status
)
from FreeListenForm form
join form.student student
join student.major major
join major.subject subject
where form.checker.id = :teacherId
and form.status = :status
order by form.dateSubmitted
''',[teacherId: teacherId, status: State.SUBMITTED], [offset: offset, max: max]
    }

    def findProcessedForms(String teacherId, int offset, int max) {
        FreeListenForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  subject.name as subject,
  major.grade as grade,
  form.dateChecked as date,
  form.reason as reason,
  form.status as status
)
from FreeListenForm form
join form.student student
join student.major major
join major.subject subject
where form.checker.id = :teacherId
and form.dateChecked is not null
order by form.dateChecked desc
''',[teacherId: teacherId], [offset: offset, max: max]
    }

    def getFormForReview(String teacherId, Long id, String activity) {
        def form = freeListenFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${FreeListenForm.WORKFLOW_ID}.${activity}"),
                User.load(teacherId),
        )
        if (workitem) {
            form.workitemId = workitem.id
        }
        checkReviewer(id, activity, teacherId)

        def studentSchedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
        ]
    }

    def getFormForReview(String teacherId, Long id, UUID workitemId) {
        def form = freeListenFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        checkReviewer(id, activity, teacherId)

        def studentSchedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
        ]
    }

    void accept(AcceptCommand cmd, String teacherId, UUID workitemId) {
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canAccept(form)) {
            throw new BadRequestException()
        }

        def workitem = Workitem.get(workitemId)
        def activity = workitem.activitySuffix
        if (activity != Activities.CHECK ||  workitem.dateProcessed || workitem.to.id != teacherId ) {
            throw new BadRequestException()
        }

        checkReviewer(cmd.id, activity, teacherId)

        form.checker = Teacher.load(teacherId)
        form.dateChecked = new Date()

        domainStateMachineHandler.accept(form, teacherId, cmd.comment, workitemId, cmd.to)

        form.save()
    }

    void reject(RejectCommand cmd, String teacherId, UUID workitemId) {
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canReject(form)) {
            throw new BadRequestException()
        }

        def activity = Workitem.get(workitemId).activitySuffix

        checkReviewer(cmd.id, activity, teacherId)

        domainStateMachineHandler.reject(form, teacherId, cmd.comment, workitemId)

        form.save()
    }

    @Override
    List<Map> getReviewers(String activity, Long id) {
        switch (activity) {
            case Activities.CHECK:
                return freeListenFormService.getCheckers(id)
            case Activities.APPROVE:
                return getApprovers()
            default:
                throw new BadRequestException()
        }
    }

    def getApprovers() {
        User.findAllWithPermission('PERM_FREE_LISTEN_APPROVE')
    }
}
