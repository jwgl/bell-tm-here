package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.CourseClass
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class CourseClassAttendanceService {
    AttendanceService attendanceService

    def getCourseClassStats(String teacherId, UUID courseClassId) {
        CourseClass courseClass = CourseClass.get(courseClassId)
        if (!courseClass) {
            throw new NotFoundException()
        }

        if (courseClass.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        attendanceService.statsByCourseClass(courseClassId)
    }

    def getStudentAttendances(String teacherId, UUID courseClassId, String studentId) {
        CourseClass courseClass = CourseClass.get(courseClassId)
        if (!courseClass) {
            throw new NotFoundException()
        }

        if (courseClass.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        attendanceService.getStudentAttendances(courseClassId, studentId)
    }
}
