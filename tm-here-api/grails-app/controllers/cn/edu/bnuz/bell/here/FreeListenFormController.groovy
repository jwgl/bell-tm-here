package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_FREE_LISTEN_WRITE")')
class FreeListenFormController implements ServiceExceptionHandler {
    FreeListenFormService freeListenFormService
    FreeListenReviewerService freeListenReviewerService

    def index(String studentId) {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 10
        renderJson freeListenFormService.list(studentId, offset, max)
    }

    def show(String studentId, Long id) {
        renderJson freeListenFormService.getFormForShow(studentId, id)
    }

    def create(String studentId) {
        renderJson freeListenFormService.getFormForCreate(studentId)
    }

    def save(String studentId) {
        def cmd = new FreeListenFormCommand()
        bindData(cmd, request.JSON)
        def form = freeListenFormService.create(studentId, cmd)
        renderJson([id: form.id])
    }

    def edit(String studentId, Long id) {
        renderJson freeListenFormService.getFormForEdit(studentId, id)
    }

    def update(String studentId, Long id) {
        def cmd = new FreeListenFormCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        freeListenFormService.update(studentId, cmd)
        renderOk()
    }

    def delete(String studentId, Long id) {
        freeListenFormService.delete(studentId, id)
        renderOk()
    }

    def patch(String studentId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                freeListenFormService.submit(studentId, cmd)
                break
        }
        renderOk()
    }

    def checkers(Long freeListenFormId) {
        renderJson freeListenReviewerService.getCheckers(freeListenFormId)
    }
}
