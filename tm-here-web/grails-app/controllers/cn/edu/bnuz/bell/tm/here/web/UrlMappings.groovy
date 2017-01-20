package cn.edu.bnuz.bell.tm.here.web

class UrlMappings {

    static mappings = {
        "/teachers"(resources: 'teacher', includes:[]) {
            "/rollcalls"(resources: 'rollcall', includes: ['index'])
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
