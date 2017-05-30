package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.operation.ScheduleService

class TeacherTimeslotController implements ServiceExceptionHandler {
    TermService termService
    ScheduleService scheduleService
    TeacherSettingService teacherSettingService

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
                config: teacherSettingService.getRollcallSettings(teacherId),
                view: teacherSettingService.getRollcallView(teacherId)
        ])
    }
}
