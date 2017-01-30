package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand

class StudentLeaveReviewController {
    StudentLeaveReviewService studentLeaveReviewService

    def index(String reviewerId) {
        def status = State.valueOf(params.status)
        def offset = params.int("offset") ?: 0
        def max = params.int("max") ?: (params.int("offset") ? 20 : Integer.MAX_VALUE)
        def forms = studentLeaveReviewService.findAllByStatus(reviewerId, status, offset, max)
        def counts = studentLeaveReviewService.getCountsByStatus(reviewerId)
        renderJson([counts: counts, forms: forms])
    }

    def show(String reviewerId, Long studentLeaveReviewId, String id) {
        if (id == 'undefined') {
            renderJson studentLeaveReviewService.getFormForReview(reviewerId, studentLeaveReviewId)
        } else {
            renderJson studentLeaveReviewService.getFormForReview(reviewerId, studentLeaveReviewId, UUID.fromString(id))
        }
    }

    def patch(String reviewerId, Long studentLeaveReviewId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = studentLeaveReviewId
                studentLeaveReviewService.accept(cmd, reviewerId, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = studentLeaveReviewId
                studentLeaveReviewService.reject(cmd, reviewerId, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        renderOk()
    }
}
