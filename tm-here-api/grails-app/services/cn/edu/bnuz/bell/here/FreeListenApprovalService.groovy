package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import grails.transaction.Transactional

@Transactional
class FreeListenApprovalService extends FreeListenCheckService {
    def getCounts(String teacherId) {
        def unchecked = FreeListenForm.countByStatus(State.SUBMITTED)
        def pending = FreeListenForm.countByStatus(State.CHECKED)
        def processed = FreeListenForm.countByApprover(Teacher.load(teacherId))
        return [
                UNCHECKED: unchecked,
                PENDING: pending,
                PROCESSED: processed,
        ]
    }

    def findUncheckedForms(String teacherId, int offset, int max) {
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
where form.status = :status
order by form.dateSubmitted
''',[status: State.SUBMITTED], [offset: offset, max: max]
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
where form.status = :status
order by form.dateChecked
''',[status: State.CHECKED], [offset: offset, max: max]
    }

    def findProcessedForms(String userId, int offset, int max) {
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
where exists (
  from Workitem workitem
  where workitem.instance = form.workflowInstance
  and workitem.activity.id = :activityId
  and workitem.dateProcessed is not null
)
order by form.dateApproved desc
''',[activityId: "${FreeListenForm.WORKFLOW_ID}.${Activities.APPROVE}"], [offset: offset, max: max]
    }

    void accept(AcceptCommand cmd, String userId, UUID workitemId) {
        FreeListenForm form = FreeListenForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canAccept(form)) {
            throw new BadRequestException()
        }

        def activity = Workitem.get(workitemId).activitySuffix
        if (activity != Activities.APPROVE) {
            throw new BadRequestException()
        }
        checkReviewer(cmd.id, activity, userId)

        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()

        domainStateMachineHandler.accept(form, userId, cmd.comment, workitemId, cmd.to)

        form.save()
    }
}
