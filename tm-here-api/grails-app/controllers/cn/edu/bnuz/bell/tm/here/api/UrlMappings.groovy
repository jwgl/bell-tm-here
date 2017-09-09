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
            "/courseClasses"(resources: 'teacherCourseClass', includes: ['index', 'show']) {
                "/code"(controller: 'teacherCourseClass', action: 'code', method: 'GET')
            }
            "/settings"(resources: 'teacherSetting', includes: ['update'])
        }

        "/students"(resources: 'student', includes: []) {
            "/attendances"(controller: 'attendance', action: 'student', method: 'GET')
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

        "/adminClasses"(resources: 'adminClass', includes: []) {
            "/attendances"(controller: 'attendance', action: 'adminClass', method: 'GET')
        }

        "/attendances"(resources: 'attendance', includes: ['index', 'show']) {
            collection {
                "/adminClasses"(controller: 'attendance', action: 'adminClasses', method: 'GET')
            }
        }

        "/courseClasses"(resources: 'courseClass', includes:[]) {
            "/students"(resources: 'courseClassStudent', includes: ['patch']) {
                "/attendances"(controller: 'courseClassStudent', action: 'attendances', method: 'GET')
            }
        }

        "/departments"(resources: 'department', includes: []) {
            "/courseClassTeachers"(controller: 'departmentCourseClass', action: 'courseClassTeachers', method: 'GET')
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "/freeListens"(resources: 'freeListenPublic', includes: ['show'])

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
