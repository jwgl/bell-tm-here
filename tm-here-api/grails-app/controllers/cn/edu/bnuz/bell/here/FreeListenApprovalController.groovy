package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_APPROVE")')
class FreeListenApprovalController implements ServiceExceptionHandler{
    FreeListenApprovalService freeListenApprovalService

    def index(String approverId) {
        def status = params.status
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)

        def counts = freeListenApprovalService.getCounts(approverId)
        def forms
        switch (status) {
            case 'PENDING':
                forms = freeListenApprovalService.findPendingForms(approverId, offset, max)
                break
            case 'PROCESSED':
                forms = freeListenApprovalService.findProcessedForms(approverId, offset, max)
                break
            case 'UNCHECKED':
                forms = freeListenApprovalService.findUncheckedForms(approverId, offset, max)
                break
            default:
                throw new BadRequestException()
        }

        renderJson([counts: counts, forms: forms])
    }

    def show(String approverId, Long freeListenApprovalId, String id) {
        if (id == 'undefined') {
            renderJson freeListenApprovalService.getFormForReview(approverId, freeListenApprovalId, Activities.APPROVE)
        } else {
            renderJson freeListenApprovalService.getFormForReview(approverId, freeListenApprovalId, UUID.fromString(id))
        }
    }

    def patch(String approverId, Long freeListenApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = freeListenApprovalId
                freeListenApprovalService.accept(cmd, approverId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = freeListenApprovalId
                freeListenApprovalService.reject(cmd, approverId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, freeListenApprovalId, id)
    }
}
