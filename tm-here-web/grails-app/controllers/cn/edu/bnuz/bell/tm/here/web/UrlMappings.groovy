package cn.edu.bnuz.bell.tm.here.web

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes:[]) {
            "/rollcalls"(resources: 'rollcall', includes: ['index'])
        }

        "/students"(resources: 'student', 'includes':[]) {
            "/leaves"(resources: 'studentLeaveForm', includes: ['index'])
        }

        "/reviewers"(resources: 'reviewer', 'includes': []) {
            "/leaves"(resources: 'studentLeaveReview', includes:['index'])
        }

        "/leaves"(resources: 'studentLeavePublic', includes: ['show'])

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
