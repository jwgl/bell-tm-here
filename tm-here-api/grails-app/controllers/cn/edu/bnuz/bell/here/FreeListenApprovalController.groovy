package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_APPROVE")')
class FreeListenApprovalController implements ServiceExceptionHandler{
    FreeListenApprovalService freeListenApprovalService

    def index(String approverId, ListCommand cmd) {
       renderJson freeListenApprovalService.list(approverId, cmd)
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
