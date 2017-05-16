package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.TermService

class TimeslotAttendanceController {
    TermService termService
    TimeslotAttendanceService timeslotAttendanceService

    def index(String teacherId, Integer teacherTimeslotId, Integer weekId) {
        TeacherTimeslotCommand cmd = new TeacherTimeslotCommand(
                termId      : termService.activeTerm.id,
                teacherId   : teacherId,
                week        : weekId,
                timeslot    : teacherTimeslotId,
        )
        renderJson timeslotAttendanceService.list(cmd)
    }

    def show(String teacherId, Integer teacherTimeslotId, Integer weekId, String id) {
        TeacherTimeslotCommand cmd = new TeacherTimeslotCommand(
                termId      : termService.activeTerm.id,
                teacherId   : teacherId,
                week        : weekId,
                timeslot    : teacherTimeslotId,
        )
        renderJson timeslotAttendanceService.get(cmd, id)
    }
}
