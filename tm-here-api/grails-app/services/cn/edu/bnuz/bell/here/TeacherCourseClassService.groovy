package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.here.dto.CourseClassAttendanceStats
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.CourseClass
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class TeacherCourseClassService {
    def getTeacherCourseClasses(Integer termId, String teacherId, String departmentId) {
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
where courseClass.term.id = :termId
 and courseClass.teacher.id = :teacherId
 and courseClass.department.id like :departmentId
group by courseClass.id, courseClass.name, course.name
order by course.name, courseClass.name
''', [termId: termId, teacherId: teacherId, departmentId: departmentId]
    }

    def getCourseClassInfo(String teacherId, UUID courseClassId, String departmentId) {
        def results = CourseClass.executeQuery '''
select distinct new map(
  courseClass.id as id,
  courseClass.code as code,
  courseClass.name as name,
  courseClass.startWeek as startWeek,
  courseClass.endWeek as endWeek,
  course.name as course,
  courseClass.teacher.id as teacherId,
  department.name as department,
  (
    select sum(taskSchedule.totalSection * floor((taskSchedule.endWeek - taskSchedule.startWeek + 1) / (case taskSchedule.oddEven when 0 then 1 else 2 end)))
    from courseClass.tasks task
    join task.schedules taskSchedule
  ) as totalSection,
  courseClass.term.id as termId,
  (
    select active from Term where id = courseClass.term.id
  ) as activeTerm
)
from CourseClass courseClass
join courseClass.course course
join courseClass.department department
where courseClass.teacher.id = :teacherId
  and courseClass.id = :courseClassId
  and courseClass.department.id like :departmentId
''', [teacherId: teacherId, courseClassId: courseClassId, departmentId: departmentId]

        if (!results) {
            throw new NotFoundException()
        }

        def courseClass = results[0]

        def attendanceSettings = AttendanceSettings.executeQuery '''
select new map(
  absentDisqualRatio as absentDisqualRatio,
  attendDisqualRatio as attendDisqualRatio
)
from AttendanceSettings
where :termId between coalesce(startTerm.id, 00000) and coalesce(endTerm.id, 99999)
''', [termId: courseClass.termId]

        if (!attendanceSettings) {
            throw new NotFoundException()
        }

        courseClass.absentDisqualRatio = attendanceSettings[0].absentDisqualRatio
        courseClass.attendDisqualRatio = attendanceSettings[0].attendDisqualRatio

        courseClass.students = CourseClass.executeQuery '''
select distinct new map(
  student.id as id,
  student.name as name,
  subject.name as subject,
  adminClass.name as adminClass,
  taskStudent.examFlag = 1 as disqualified 
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

        courseClass.attendances = CourseClassAttendanceStats.statsByCourseClass(courseClassId)

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
