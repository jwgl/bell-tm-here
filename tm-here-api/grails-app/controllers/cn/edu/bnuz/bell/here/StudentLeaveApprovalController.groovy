package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_STUDENT_LEAVE_APPROVE")')
class StudentLeaveApprovalController implements ServiceExceptionHandler {
    StudentLeaveApprovalService studentLeaveApprovalService

    def index(String approverId) {
        def status = State.valueOf(params.status as String)
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)
        def forms = studentLeaveApprovalService.findAllByStatus(approverId, status, offset, max)
        def counts = studentLeaveApprovalService.getCountsByStatus(approverId)
        renderJson([counts: counts, forms: forms])
    }

    def show(String approverId, Long studentLeaveApprovalId, String id) {
        if (id == 'undefined') {
            renderJson studentLeaveApprovalService.getFormForReview(approverId, studentLeaveApprovalId)
        } else {
            renderJson studentLeaveApprovalService.getFormForReview(approverId, studentLeaveApprovalId, UUID.fromString(id))
        }
    }

    def patch(String approverId, Long studentLeaveApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = studentLeaveApprovalId
                studentLeaveApprovalService.accept(approverId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = studentLeaveApprovalId
                studentLeaveApprovalService.reject(approverId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, studentLeaveApprovalId, id)
    }
}
