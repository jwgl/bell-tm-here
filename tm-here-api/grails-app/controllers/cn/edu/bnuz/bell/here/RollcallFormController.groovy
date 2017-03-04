package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService

class RollcallFormController implements ServiceExceptionHandler {
    TermService termService
    ScheduleService scheduleService
    RollcallFormService rollcallFormService
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
        def students = rollcallFormService.getRollcallStudents(term, teacherId, week, dayOfWeek, startSection)
        def rollcalls = rollcallFormService.getRollcalls(term, teacherId, week, dayOfWeek, startSection)
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
        def rollcall = rollcallFormService.create(teacherId, cmd)
        renderJson([id: rollcall.id])
    }

    def update(String teacherId, Long id) {
        def cmd = new RollcallUpdateCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        rollcallFormService.update(teacherId, cmd)
        renderOk()
    }

    def delete(String teacherId, Long id) {
        rollcallFormService.delete(teacherId, id)
        renderOk()
    }
}
