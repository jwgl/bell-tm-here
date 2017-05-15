package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.TermService

/**
 * 按教师查询排课的教学班情况。
 */
class CourseClassController {
    CourseClassService courseClassService
    TermService termService

    def index(String teacherId) {
        def termId = params.int('termId') ?: termService.activeTerm.id
        renderJson courseClassService.list(teacherId, termId)
    }

    def show(String teacherId, String id) {
        renderJson courseClassService.getCourseClassInfo(teacherId, UUID.fromString(id))
    }

    def code(String teacherId, String courseClassId) {
        render courseClassService.getCourseClassCode(teacherId, UUID.fromString(courseClassId))
    }
}
