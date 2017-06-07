package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.TermService

class DepartmentCourseClassController {
    DepartmentCourseClassService departmentCourseClassService
    TermService termService

    def courseClassTeachers(String departmentId) {
        def termId = params.int('termId') ?: termService.activeTerm.id
        renderJson departmentCourseClassService.getCourseClassTeachers(termId, departmentId)
    }
}
