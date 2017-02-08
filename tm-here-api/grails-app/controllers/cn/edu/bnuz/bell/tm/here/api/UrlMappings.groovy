package cn.edu.bnuz.bell.tm.here.api

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes: []) {
            "/rollcalls"(resources: 'rollcall')
            "/freeListens"(resources: 'freeListenCheck', includes: ['index']) {
                "/workitems"(resources: 'freeListenCheck', includes: ['show', 'patch'])
            }
        }

        "/students"(resources: 'student', includes: []) {
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

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "/freeListens"(resources: 'freeListenPublic', includes: ['show'])

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
