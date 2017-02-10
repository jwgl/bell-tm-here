package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_CHECK")')
class FreeListenCheckController implements ServiceExceptionHandler {
	FreeListenCheckService freeListenCheckService

    def index(String teacherId) {
        def status = params.status
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)

        def counts = freeListenCheckService.getCounts(teacherId)
        def forms
        switch (status) {
            case 'PENDING':
                forms = freeListenCheckService.findPendingForms(teacherId, offset, max)
                break
            case 'PROCESSED':
                forms = freeListenCheckService.findProcessedForms(teacherId, offset, max)
                break
            default:
                throw new BadRequestException()
        }

        renderJson([counts: counts, forms: forms])
    }

    def show(String teacherId, Long freeListenCheckId, String id) {
        if (id == 'undefined') {
            renderJson freeListenCheckService.getFormForReview(teacherId, freeListenCheckId, Activities.CHECK)
        } else {
            renderJson freeListenCheckService.getFormForReview(teacherId, freeListenCheckId, UUID.fromString(id))
        }
    }

    def patch(String teacherId, Long freeListenCheckId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = freeListenCheckId
                freeListenCheckService.accept(cmd, teacherId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = freeListenCheckId
                freeListenCheckService.reject(cmd, teacherId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        renderOk()
    }

    def approvers(String teacherId, Long freeListenCheckId) {
        renderJson freeListenCheckService.getApprovers()
    }
}
