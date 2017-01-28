package cn.edu.bnuz.bell.tm.here.web

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes:[]) {
            "/rollcalls"(resources: 'rollcall', includes: ['index'])
        }

        "/students"(resources: 'student', 'includes':[]) {
            "/leaves"(resources: 'studentLeaveForm', includes: ['index'])
        }

        "/leaves"(resources: 'studentLeaveAdmin', includes:[]) {
            "/reviews"(resources: 'studentLeaveReview', includes: ['show'])
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
