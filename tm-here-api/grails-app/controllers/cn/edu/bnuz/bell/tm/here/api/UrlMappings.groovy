package cn.edu.bnuz.bell.tm.here.api

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes:[]) {
            "/rollcalls"(resources: 'rollcall')
        }

        "/students"(resources: 'student', includes:[]) {
            "/leaves"(resources: 'studentLeaveForm') {
                "/approvers"(controller: 'studentLeaveForm', action: 'approvers', method: 'GET')
            }
        }

        "/reviewers"(resources: 'reviewer', includes:[]) {
            "/leaves"(resources: 'studentLeaveReview', includes: ['index']) {
                "/workitems"(resources: 'studentLeaveReview', includes: ['show', 'patch'])
            }
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
