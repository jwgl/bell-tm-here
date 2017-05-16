package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.TermService

class RollcallController {
    TermService termService
    RollcallService rollcallService

    def index(String teacherId, Integer teacherTimeslotId, Integer weekId) {
        TeacherTimeslotCommand cmd = new TeacherTimeslotCommand(
                termId      : termService.activeTerm.id,
                teacherId   : teacherId,
                week        : weekId,
                timeslot    : teacherTimeslotId,
        )
        renderJson rollcallService.list(cmd)
    }

    def save(String teacherId) {
        def cmd = new RollcallCreateCommand()
        bindData(cmd, request.JSON)
        Rollcall rollcall = rollcallService.create(teacherId, cmd)
        renderJson([id: rollcall.id])
    }

    def update(String teacherId, Long id) {
        def cmd = new RollcallUpdateCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        rollcallService.update(teacherId, cmd)
        renderOk()
    }

    def delete(String teacherId, Long id) {
        rollcallService.delete(teacherId, id)
        renderOk()
    }
}
