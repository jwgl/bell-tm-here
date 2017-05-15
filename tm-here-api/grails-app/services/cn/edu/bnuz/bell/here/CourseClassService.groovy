package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.CourseClass
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class CourseClassService {
    def list(String teacherId, Integer termId) {
        CourseClass.executeQuery '''
select distinct new map(
  courseClass.id as id,
  courseClass.name as name,
  course.name as course,
  count(distinct taskStudent.student) as count
)
from CourseClass courseClass
join courseClass.course course
join courseClass.tasks task
join task.students taskStudent
where exists elements(task.schedules) 
and courseClass.teacher.id = :teacherId
and courseClass.term.id = :termId
group by courseClass.id, courseClass.name, course.name
order by course.name, courseClass.name
''', [teacherId: teacherId, termId: termId]
    }

    def getCourseClassInfo(String teacherId, UUID courseClassId) {
        def results = CourseClass.executeQuery '''
select distinct new map(
  courseClass.id as id,
  courseClass.code as code,
  courseClass.name as name,
  courseClass.startWeek as startWeek,
  courseClass.endWeek as endWeek,
  course.name as course,
  department.name as department
)
from CourseClass courseClass
join courseClass.course course
join courseClass.department department
where courseClass.teacher.id = :teacherId 
and courseClass.id = :courseClassId
''', [teacherId: teacherId, courseClassId: courseClassId]

        if (!results) {
            throw new NotFoundException()
        }

        def courseClass = results[0]
        courseClass.students = CourseClass.executeQuery '''
select distinct new map(
  student.id as id,
  student.name as name,
  subject.name as subject,
  adminClass.name as adminClass
)
from CourseClass courseClass
join courseClass.tasks task
join task.students taskStudent
join taskStudent.student student
join student.major major
join major.subject subject
join student.adminClass adminClass
where courseClass.id = :courseClassId
order by student.id
''', [courseClassId: courseClassId]

        return courseClass
    }

    String getCourseClassCode(String teacherId, UUID courseClassId) {
        CourseClass courseClass = CourseClass.get(courseClassId)
        if (!courseClass) {
            throw new NotFoundException()
        }

        if (courseClass.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        return courseClass.code
    }
}
