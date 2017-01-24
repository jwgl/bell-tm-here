package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService

class RollcallController implements ServiceExceptionHandler {
    TermService termService
    ScheduleService scheduleService
    RollcallService rollcallService

    def index(String teacherId) {
        def term = termService.activeTerm
        def schedules = scheduleService.getTeacherSchedules(teacherId, term)
        renderJson([
                term: [
                        startWeek: term.startWeek,
                        endWeek: term.endWeek,
                        currentWeek: 5/*term.currentWeek*/,
                ],
                schedules: schedules,
                config: [hideLeave: false, hideFree: false, hideCancel: false, random: 0],
        ])
    }

    def create(String teacherId) {
        def term = termService.activeTerm
        def week = params.int('week')
        def dayOfWeek = params.int('day')
        def startSection = params.int('section')
        def students = rollcallService.getRollcallStudents(term, teacherId, week, dayOfWeek, startSection)
        def rollcalls = rollcallService.getRollcalls(term, teacherId, week, dayOfWeek, startSection)
        renderJson([
                students: students,
                rollcalls: rollcalls,
                leaves: [],
                locked: false,
        ])
    }

    def save(String teacherId) {
        Thread.sleep(1000)
        def cmd = new RollcallCreateCommand()
        bindData(cmd, request.JSON)
        def rollcall = rollcallService.create(teacherId, cmd)
        renderJson([id: rollcall.id])
    }

    def update(String teacherId, Long id) {
        Thread.sleep(1000)
        def cmd = new RollcallUpdateCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        rollcallService.update(teacherId, cmd)
        renderOk()
    }

    def delete(String teacherId, Long id) {
        Thread.sleep(1000)
        rollcallService.delete(teacherId, id)
        renderOk()
    }
}
