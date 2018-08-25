package cn.edu.bnuz.bell.tm.here.api

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes: []) {
            "/timeslots"(resources: 'teacherTimeslot', includes: ['index', 'show']) {
                "/weeks"(resources: 'week', includes: []) {
                    "/rollcalls"(resources: 'rollcall')
                    "/attendances"(resources: 'timeslotAttendance', includes: ['index', 'show'])
                }
            }
            "/freeListens"(resources: 'freeListenCheck', includes: ['index']) {
                "/workitems"(resources: 'freeListenCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'freeListenCheck', action: 'approvers', method: 'GET')
                collection {
                    "/settings"(controller: 'freeListenCheck', action: 'settings', method: 'GET')
                }
            }
            "/adminClasses"(resources: 'teacherAdminClass', includes: ['index']) {
                "/attendances"(controller: 'teacherAdminClass', action: 'attendances', method: 'GET')
                collection {
                    "/attendances"(controller: 'teacherAdminClass', action: 'allAttendances', method: 'GET')
                    "/statisReport"(controller: 'teacherAdminClass',action: 'statisReport', method: 'GET')
                    "/detailReport"(controller: 'teacherAdminClass',action: 'detailReport', method: 'GET')
                }
            }
            "/courseClasses"(resources: 'teacherCourseClass', includes: ['index', 'show']) {
                "/code"(controller: 'teacherCourseClass', action: 'code', method: 'GET')
                "/report"(controller: 'teacherCourseClass', action: 'report', method: 'GET')
                collection {
                    "/terms"(controller: 'teacherCourseClass', action: 'terms', method: 'GET')
                }
            }
            "/settings"(resources: 'teacherSetting', includes: ['update'])
        }

        "/students"(resources: 'student', includes: []) {
            "/attendances"(resources: 'studentAttendance', includes: ['index'])
            "/leaves"(resources: 'studentLeaveForm') {
                "/approvers"(controller: 'studentLeaveForm', action: 'approvers', method: 'GET')
            }
            "/freeListens"(resources: 'freeListenForm') {
                "/checkers"(controller: 'freeListenForm', action: 'checkers', method: 'GET')
            }
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/leaves"(resources: 'studentLeaveApproval', includes: ['index']) {
                "/workitems"(resources: 'studentLeaveApproval', includes: ['show', 'patch'])
            }
            "/freeListens"(resources: 'freeListenApproval', includes: ['index']) {
                "/workitems"(resources: 'freeListenApproval', includes: ['show', 'patch'])
            }
        }

        "/attendances"(resources: 'attendance', includes: ['show']) {
            collection {
                "/terms"(action: 'terms', method: 'GET')
            }
        }

        "/courseClasses"(resources: 'courseClass', includes:[]) {
            "/students"(resources: 'courseClassStudent', includes: ['patch']) {
                "/attendances"(controller: 'courseClassStudent', action: 'attendances', method: 'GET')
            }
        }

        "/departments"(resources: 'department', includes: []) {
            "/adminClasses"(resources: 'departmentAdminClass', includes: ['index', 'show']) {
                "/attendances"(controller: 'departmentAdminClass', action: 'attendances', method: 'GET')
                collection {
                    "/attendances"(controller: 'departmentAdminClass', action: 'allAttendances', method: 'GET')
                    "/statisReport"(controller: 'departmentAdminClass',action: 'statisReport', method: 'GET')
                    "/detailReport"(controller: 'departmentAdminClass',action: 'detailReport', method: 'GET')
                    "/disqualReport"(controller: 'departmentAdminClass',action: 'disqualReport', method: 'GET')
                }
            }
            "/courseClassTeachers"(controller: 'departmentCourseClass', action: 'courseClassTeachers', method: 'GET')
            "/courseClasses"(resources: 'departmentCourseClass', includes: ['show'])
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "/freeListens"(resources: 'freeListenPublic', includes: ['show']) {
            collection {
                "/settings"(controller: 'freeListenPublic', action: 'settings', method: 'GET')
                "/notice"(controller: 'freeListenPublic', action: 'notice', method: 'GET')
            }
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
