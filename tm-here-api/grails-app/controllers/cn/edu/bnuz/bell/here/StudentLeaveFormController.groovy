package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_STUDENT_LEAVE_WRITE")')
class StudentLeaveFormController implements ServiceExceptionHandler {
    StudentLeaveFormService studentLeaveFormService
    StudentLeaveReviewerService studentLeaveReviewerService

    def index(String studentId) {
        def offset = params.int('offset') ?: 0
        def max = params.int('max') ?: 10
        renderCount studentLeaveFormService.listCount(studentId)
        renderJson studentLeaveFormService.list(studentId, offset, max)
    }

    def show(String studentId, Long id) {
        renderJson studentLeaveFormService.getFormForShow(studentId, id)
    }

    def create(String studentId) {
        renderJson studentLeaveFormService.getFormForCreate(studentId)
    }

    def save(String studentId) {
        def cmd = new StudentLeaveFormCommand()
        bindData(cmd, request.JSON)
        def form = studentLeaveFormService.create(studentId, cmd)
        renderJson([id: form.id])
    }

    def edit(String studentId, Long id) {
        renderJson studentLeaveFormService.getFormForEdit(studentId, id)
    }

    def update(String studentId, Long id) {
        def cmd = new StudentLeaveFormCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        studentLeaveFormService.update(studentId, cmd)
        renderOk()
    }

    def delete(String studentId, Long id) {
        studentLeaveFormService.delete(studentId, id)
        renderOk()
    }

    def patch(String studentId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                studentLeaveFormService.submit(studentId, cmd)
                break
            case Event.FINISH:
                studentLeaveFormService.finish(studentId, id)
                break
        }
        renderOk()
    }

    def approvers(Long studentLeaveFormId) {
        renderJson studentLeaveReviewerService.getApprovers(studentLeaveFormId)
    }
}
