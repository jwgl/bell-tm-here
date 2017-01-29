package cn.edu.bnuz.bell.tm.here.api

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes:[]) {
            "/rollcalls"(resources: 'rollcall')
        }

        "/students"(resources: 'student', includes:[]) {
            "/leaves"(resources: 'studentLeaveForm') {
                "/checkers"(controller: 'studentLeaveForm', action: 'checkers', method: 'GET')
            }
        }

        "/reviewers"(resources: 'reviewer', includes:[]) {
            "/leaves"(resources: 'studentLeaveReview', includes: ['index']) {
                "/workitems"(resources: 'studentLeaveReview', includes: ['show', 'patch'])
            }
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show']) {
            "/reviewers"(controller: 'studentLeaveReview', action: 'reviewers', method: 'GET')
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
