package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService

class RollcallFormController implements ServiceExceptionHandler {
    TermService termService
    ScheduleService scheduleService
    RollcallFormService rollcallFormService

    def index(String teacherId) {
        def term = termService.activeTerm
        def schedules = scheduleService.getTeacherSchedules(teacherId, term.id)
        renderJson([
                term: [
                        startWeek  : term.startWeek,
                        endWeek    : term.endWeek,
                        currentWeek: term.currentWeek,
                ],
                schedules: schedules,
                config: [
                        hideLeave: false,
                        hideFree: false,
                        hideCancel: false,
                        random: 100
                ],
        ])
    }

    def create(String teacherId) {
        RollcallCommand cmd = new RollcallCommand(
                termId      : termService.activeTerm.id,
                teacherId   : teacherId,
                week        : params.int('week'),
                dayOfWeek   : params.int('day'),
                startSection: params.int('section'),
        )
        renderJson rollcallFormService.getFormForCreate(cmd)
    }

    def save(String teacherId) {
        def cmd = new RollcallCreateCommand()
        bindData(cmd, request.JSON)
        renderJson rollcallFormService.create(teacherId, cmd)
    }

    def update(String teacherId, Long id) {
        def cmd = new RollcallUpdateCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        renderJson rollcallFormService.update(teacherId, cmd)
    }

    def delete(String teacherId, Long id) {
        renderJson rollcallFormService.delete(teacherId, id)
    }
}
