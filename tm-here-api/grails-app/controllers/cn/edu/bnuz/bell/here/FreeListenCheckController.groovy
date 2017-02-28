package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_CHECK")')
class FreeListenCheckController implements ServiceExceptionHandler {
    FreeListenCheckService freeListenCheckService
    FreeListenReviewerService freeListenReviewerService

    def index(String teacherId, ListCommand cmd) {
        renderJson freeListenCheckService.list(teacherId, cmd)
    }

    def show(String teacherId, Long freeListenCheckId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson freeListenCheckService.getFormForReview(teacherId, freeListenCheckId, listType, Activities.CHECK)
        } else {
            renderJson freeListenCheckService.getFormForReview(teacherId, freeListenCheckId, listType, UUID.fromString(id))
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

        show(teacherId, freeListenCheckId, id, 'todo')
    }

    def approvers(String teacherId, Long freeListenCheckId) {
        renderJson freeListenReviewerService.getApprovers()
    }
}
