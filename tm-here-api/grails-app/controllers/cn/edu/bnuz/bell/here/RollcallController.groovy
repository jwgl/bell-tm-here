package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService

class RollcallController implements ServiceExceptionHandler {
    TermService termService
    ScheduleService scheduleService
    RollcallService rollcallService
    StudentLeavePublicService studentLeavePublicService
    FreeListenPublicService freeListenPublicService

    def index(String teacherId) {
        def term = termService.activeTerm
        def schedules = scheduleService.getTeacherSchedules(teacherId, term.id)
        renderJson([
                term: [
                        startWeek: term.startWeek,
                        endWeek: term.endWeek,
                        currentWeek: term.currentWeek,
                ],
                schedules: schedules,
                config: [hideLeave: false, hideFree: false, hideCancel: false, random: 100],
        ])
    }

    def create(String teacherId) {
        def term = termService.activeTerm
        def week = params.int('week')
        def dayOfWeek = params.int('day')
        def startSection = params.int('section')
        def students = rollcallService.getRollcallStudents(term, teacherId, week, dayOfWeek, startSection)
        def rollcalls = rollcallService.getRollcalls(term, teacherId, week, dayOfWeek, startSection)
        def leaves = studentLeavePublicService.getRollcallLeaves(term, teacherId, week, dayOfWeek, startSection)
        def freeListens = freeListenPublicService.getRollcallFreeListens(term, teacherId, week, dayOfWeek, startSection)
        renderJson([
                students: students,
                rollcalls: rollcalls,
                leaves: leaves,
                freeListens: freeListens,
                cancelExams: [],
                locked: false,
        ])
    }

    def save(String teacherId) {
        def cmd = new RollcallCreateCommand()
        bindData(cmd, request.JSON)
        def rollcall = rollcallService.create(teacherId, cmd)
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
