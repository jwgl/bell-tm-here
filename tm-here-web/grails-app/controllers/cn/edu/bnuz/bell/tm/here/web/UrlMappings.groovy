package cn.edu.bnuz.bell.tm.here.web

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes: []) {
            "/rollcalls"(resources: 'rollcallForm', includes: ['index'])
            "/freeListens"(resources: 'freeListenCheck', includes: ['index'])
        }

        "/students"(resources: 'student', 'includes': []) {
            "/attendances"(controller: 'attendance', action: 'student', method: 'GET')
            "/leaves"(resources: 'studentLeaveForm', includes: ['index'])
            "/freeListens"(resources: 'freeListenForm', includes: ['index'])
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/leaves"(resources: 'studentLeaveApproval', includes:['index'])
            "/freeListens"(resources: 'freeListenApproval', includes: ['index'])
        }

        "/attendances"(resources: 'attendance', includes: ['index', 'show']) {
            collection {
                "/statisReport"(controller: 'attendanceReport', action: 'statis', method: 'GET')
                "/detailReport"(controller: 'attendanceReport', action: 'detail', method: 'GET')
            }
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "/freeListens"(resources: 'freeListenPublic', includes: ['show'])

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
