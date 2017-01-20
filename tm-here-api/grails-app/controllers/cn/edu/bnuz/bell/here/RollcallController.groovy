package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.tm.common.master.TermService
import cn.edu.bnuz.bell.tm.common.operation.ScheduleService

class RollcallController {
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
        renderJson([
                students: students,
                rollcallItems: [],
                leaveRequests: [],
                locked: false,
        ])
    }
}
