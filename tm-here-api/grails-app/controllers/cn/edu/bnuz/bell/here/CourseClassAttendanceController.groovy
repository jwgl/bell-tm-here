package cn.edu.bnuz.bell.here

/**
 * 教学班考勤情况。
 */
class CourseClassAttendanceController {
    CourseClassAttendanceService courseClassAttendanceService

    def index(String teacherId, String courseClassId) {
        renderJson courseClassAttendanceService.getCourseClassStats(teacherId, UUID.fromString(courseClassId))
    }

    def show(String teacherId, String courseClassId, String id) {
        renderJson courseClassAttendanceService.getStudentAttendances(teacherId, UUID.fromString(courseClassId), id)
    }
}
