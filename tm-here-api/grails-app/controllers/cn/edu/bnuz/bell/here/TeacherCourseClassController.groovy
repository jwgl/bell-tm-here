package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 按教师查询排课的教学班情况。
 */
@PreAuthorize('hasAnyAuthority("PERM_COURSE_CLASS_READ", "PERM_EXAM_DISQUAL_DEPT_ADMIN")')
class TeacherCourseClassController implements ServiceExceptionHandler {
    TeacherCourseClassService teacherCourseClassService
    SecurityService securityService
    TermService termService
    RollcallTermService rollcallTermService

    def index(String teacherId) {
        def termId = params.int('termId') ?: termService.activeTerm.id
        renderJson teacherCourseClassService.getTeacherCourseClasses(termId, teacherId, getDepartmentId(teacherId))
    }

    def show(String teacherId, String id) {
        renderJson teacherCourseClassService.getCourseClassInfo(teacherId, UUID.fromString(id), getDepartmentId(teacherId))
    }

    def code(String teacherId, String teacherCourseClassId) {
        render teacherCourseClassService.getCourseClassCode(teacherId, UUID.fromString(teacherCourseClassId))
    }

    def terms(String teacherId) {
        renderJson rollcallTermService.getTerms(teacherId)
    }

    private String getDepartmentId(String teacherId) {
        if (this.securityService.hasPermission('PERM_EXAM_DISQUAL_DEPT_ADMIN')) {
            return this.securityService.departmentId
        } else if (this.securityService.userId == teacherId) {
            return '%'
        } else {
            throw new BadRequestException()
        }
    }
}
