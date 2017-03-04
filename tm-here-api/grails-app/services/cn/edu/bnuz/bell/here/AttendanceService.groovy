package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.tm.common.organization.StudentService
import grails.transaction.Transactional

@Transactional
class AttendanceService {
    SecurityService securityService
    StudentService studentService

    def getAll(Integer termId, Integer offset, Integer max) {
        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            [
                    adminClasses: countByDepartment(termId, securityService.departmentId),
                    students    : getByDepartment(termId, securityService.departmentId, offset, max),
            ]
        } else if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR') ||
                   securityService.hasRole('ROLE_CLASS_SUPERVISOR')){
            [
                    adminClasses: countByAdminstrator(termId, securityService.userId),
                    students    : getByAdministrator(termId, securityService.userId, offset, max),
            ]
        } else {
            throw new ForbiddenException()
        }
    }

    def getByDepartment(Integer termId, String departmentId, Integer offset, Integer max) {
        StudentAttendance.executeQuery '''
select new map (
  student.id as id,
  student.name as name,
  adminClass.name as adminClass,
  sum(case type when 1 then schedule.totalSection else 0 end) as absent,
  sum(case type when 2 then 0.5 else 0 end) as late,
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as early,
  sum(case type when 1 then schedule.totalSection else 0 end) +
  sum(case type when 2 then 0.5 else 0 end) +
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as total,
  sum(case type when 4 then schedule.totalSection else 0 end) as leave
)
from StudentAttendance sa
join sa.student student
join sa.taskSchedule schedule
join student.adminClass adminClass
where sa.term.id = :termId
and student.department.id = :departmentId
and sa.valid = true
group by student, adminClass
order by total desc
''', [termId: termId, departmentId: departmentId], [offset: offset, max: max]
    }

    def countByDepartment(Integer termId, String departmentId) {
        StudentAttendance.executeQuery '''
select new map (
  adminClass.id as id,
  adminClass.name as name,
  count(distinct student) as count
)
from StudentAttendance sa
join sa.student student
join student.adminClass adminClass
where sa.term.id = :termId
and student.department.id = :departmentId
and sa.valid = true
group by adminClass
order by count(distinct student) desc
''', [termId: termId, departmentId: departmentId]
    }

    def getByAdministrator(Integer termId, String userId, Integer offset, Integer max) {
        StudentAttendance.executeQuery '''
select new map (
  student.id as id,
  student.name as name,
  adminClass.name as adminClass,
  sum(case type when 1 then schedule.totalSection else 0 end) as absent,
  sum(case type when 2 then 0.5 else 0 end) as late,
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as early,
  sum(case type when 1 then schedule.totalSection else 0 end) +
  sum(case type when 2 then 0.5 else 0 end) +
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as total,
  sum(case type when 4 then schedule.totalSection else 0 end) as leave
)
from StudentAttendance sa
join sa.student student
join sa.taskSchedule schedule
join student.adminClass adminClass
where sa.term.id = :termId
and (adminClass.counsellor.id = :userId or adminClass.supervisor.id = :userId) 
and sa.valid = true
group by student, adminClass
order by total desc
''', [termId: termId, userId: userId], [offset: offset, max: max]
    }

    def countByAdminstrator(Integer termId, String userId) {
        StudentAttendance.executeQuery '''
select new map (
  adminClass.id as id,
  adminClass.name as name,
  count(distinct student) as count
)
from StudentAttendance sa
join sa.student student
join student.adminClass adminClass
where sa.term.id = :termId
and (adminClass.counsellor.id = :userId or adminClass.supervisor.id = :userId) 
and sa.valid = true
group by adminClass
order by count(distinct student) desc
''', [termId: termId, userId: userId]
    }

    def getByAdminClass(Integer termId, Long adminClassId, Integer offset, Integer max) {
        def students = StudentAttendance.executeQuery '''
select new map (
  student.id as id,
  student.name as name,
  adminClass.name as adminClass,
  sum(case type when 1 then schedule.totalSection else 0 end) as absent,
  sum(case type when 2 then 0.5 else 0 end) as late,
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as early,
  sum(case type when 1 then schedule.totalSection else 0 end) +
  sum(case type when 2 then 0.5 else 0 end) +
  sum(case type when 3 then schedule.totalSection when 5 then schedule.totalSection else 0 end) as total,
  sum(case type when 4 then schedule.totalSection else 0 end) as leave
)
from StudentAttendance sa
join sa.student student
join sa.taskSchedule schedule
join student.adminClass adminClass
where sa.term.id = :termId
and adminClass.id = :adminClassId
and sa.valid = true
group by student, adminClass
order by total desc
''', [termId: termId, adminClassId: adminClassId], [offset: offset, max: max]

        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            [
                    adminClasses: countByDepartment(termId, securityService.departmentId),
                    students    : students
            ]
        } else {
            [
                    adminClasses: countByAdminstrator(termId, securityService.userId),
                    students    : students
            ]
        }
    }

    /**
     * 获取学生考勤情况
     * @param studentId 学生ID
     * @param termId 学期
     */
    def getStudentAttendances(String studentId, Integer termId) {
        def userId = securityService.userId

        if (!canViewStudentAttendances(userId, studentId)) {
            throw new ForbiddenException()
        }

        def list = StudentAttendance.executeQuery '''
select new map (
  sa.week as week,
  teacher.name as teacher,
  schedule.dayOfWeek as dayOfWeek,
  schedule.startSection as startSection,
  schedule.totalSection as totalSection,
  sa.type as type,
  course.name as course,
  sa.freeListenForm.id as freeListen,
  sa.studentLeaveForm.id as studentLeave,
  sa.valid as valid
)
from StudentAttendance sa
join sa.teacher teacher
join sa.taskSchedule schedule
join schedule.task task
join task.courseClass courseClass
join courseClass.course course
where sa.term.id = :termId
and sa.student.id = :studentId
order by week, dayOfWeek, startSection
''', [termId: termId, studentId: studentId]

        if (userId != studentId) {
            return [list: list, student: studentService.getStudentInfo(studentId)]
        } else {
            return [list: list]
        }
    }

    def canViewStudentAttendances(String userId, String studentId) {
        if (userId == studentId) {
            return true
        }

        if (securityService.hasRole('ROLE_ROLLCALL_DEPT_ADMIN')) {
            if (securityService.departmentId == studentService.getDepartment(studentId)?.id) {
                return true
            }
        }

        if (securityService.hasRole('ROLE_STUDENT_COUNSELLOR')) {
            if (userId == studentService.getCounsellor(studentId)?.id) {
                return true
            }
        }

        if (securityService.hasRole('ROLE_CLASS_SUPERVISOR')) {
            if (userId == studentService.getSupervisor(studentId)?.id) {
                return true
            }
        }

        return false
    }
}
