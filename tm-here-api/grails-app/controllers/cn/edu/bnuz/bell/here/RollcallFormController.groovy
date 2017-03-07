package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.operation.ScheduleService
import cn.edu.bnuz.bell.profile.UserSettingService

class RollcallFormController implements ServiceExceptionHandler {
    static final String ROLLCALL_SETTINGS_KEY = 'rollcall.settings'
    static final String ROLLCALL_VIEW_KEY = 'rollcall.view'

    TermService termService
    ScheduleService scheduleService
    RollcallFormService rollcallFormService
    UserSettingService userSettingService

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
                config: userSettingService.getMap(teacherId, ROLLCALL_SETTINGS_KEY),
                view: userSettingService.getString(teacherId, ROLLCALL_VIEW_KEY, 'detail')
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

    def settings(String teacherId) {
        switch (params.type) {
            case 'settings':
                userSettingService.setMap(teacherId, ROLLCALL_SETTINGS_KEY, request.JSON as Map)
                break
            case 'view':
                userSettingService.setString(teacherId, ROLLCALL_VIEW_KEY, request.JSON.view as String)
                break
        }
        renderOk()
    }
}
