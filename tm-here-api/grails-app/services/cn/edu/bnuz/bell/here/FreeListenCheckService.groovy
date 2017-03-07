package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.operation.ScheduleService
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
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
class FreeListenCheckService {
    FreeListenFormService freeListenFormService
    ScheduleService scheduleService
    DataAccessService dataAccessService

    @Resource(name='freeListenFormStateHandler')
    DomainStateMachineHandler domainStateMachineHandler

    def getCounts(String teacherId) {
        def teacher = Teacher.load(teacherId)
        def todo = FreeListenForm.countByCheckerAndStatus(teacher, State.SUBMITTED)
        def done = FreeListenForm.countByCheckerAndStatusNotEqualAndDateCheckedIsNotNull(teacher, State.SUBMITTED)

        [
                (ListType.TODO): todo,
                (ListType.DONE): done,
        ]
    }

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(userId, cmd.args)
            case ListType.DONE:
                return findDoneList(userId, cmd.args)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String teacherId, Map args) {
        def forms = FreeListenForm.executeQuery '''
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
''',[teacherId: teacherId, status: State.SUBMITTED], args

        return [forms: forms, counts: getCounts(teacherId)]
    }

    def findDoneList(String teacherId, Map args) {
        def forms = FreeListenForm.executeQuery '''
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
and form.status <> :status
order by form.dateChecked desc
''',[teacherId: teacherId, status: State.SUBMITTED], args

        return [forms: forms, counts: getCounts(teacherId)]
    }

    def getFormForReview(String teacherId, Long id, ListType type, String activity) {
        def form = freeListenFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${FreeListenForm.WORKFLOW_ID}.${activity}"),
                User.load(teacherId),
        )
        domainStateMachineHandler.checkReviewer(id, teacherId, activity)

        def studentSchedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
                counts: getCounts(teacherId),
                workitemId: workitem ? workitem.id : null,
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
        ]
    }

    def getFormForReview(String teacherId, Long id, ListType type, UUID workitemId) {
        def form = freeListenFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, teacherId, activity)

        def studentSchedules = scheduleService.getStudentSchedules(form.studentId, form.term)
        def departmentSchedules = freeListenFormService.findDepartmentOtherSchedules(form.id)
        return [
                form: form,
                studentSchedules: studentSchedules,
                departmentSchedules: departmentSchedules,
                counts: getCounts(teacherId),
                workitemId: workitemId,
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
        ]
    }


    Long getPrevReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from FreeListenForm form
where form.checker.id = :teacherId
and form.status = :status
and form.dateSubmitted < (select dateSubmitted from FreeListenForm where id = :id)
order by form.dateSubmitted desc
''', [teacherId: teacherId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from FreeListenForm form
where form.checker.id = :teacherId
and form.dateChecked is not null
and form.status <> :status
and form.dateChecked > (select dateChecked from FreeListenForm where id = :id)
order by form.dateChecked asc
''', [teacherId: teacherId, id: id, status: State.SUBMITTED])
        }
    }

    Long getNextReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from FreeListenForm form
where form.checker.id = :teacherId
and form.status = :status
and form.dateSubmitted > (select dateSubmitted from FreeListenForm where id = :id)
order by form.dateSubmitted asc
''', [teacherId: teacherId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from FreeListenForm form
where form.checker.id = :teacherId
and form.dateChecked is not null
and form.status <> :status
and form.dateChecked < (select dateChecked from FreeListenForm where id = :id)
order by form.dateChecked desc
''', [teacherId: teacherId, id: id, status: State.SUBMITTED])
        }
    }

    void accept(AcceptCommand cmd, String teacherId, UUID workitemId) {
        FreeListenForm form = FreeListenForm.get(cmd.id)
        domainStateMachineHandler.accept(form, teacherId, Activities.CHECK, cmd.comment, workitemId, cmd.to)
        // checker is set in form
        form.dateChecked = new Date()
        form.save()
    }

    void reject(RejectCommand cmd, String teacherId, UUID workitemId) {
        FreeListenForm form = FreeListenForm.get(cmd.id)
        domainStateMachineHandler.reject(form, teacherId, Activities.CHECK, cmd.comment, workitemId)
        // checker is set in form
        form.dateChecked = new Date()
        form.save()
    }
}
