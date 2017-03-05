package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.tm.common.organization.StudentService
import grails.transaction.Transactional

@Transactional
class AttendanceService {
    SecurityService securityService
    StudentService studentService

    /**
     * 按学院统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByDepartment(Integer termId, String departmentId, Integer offset, Integer max) {
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

    /**
     * 按学院统计教学班学生数量
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 教学班学生数
     */
    def adminClassesByDepartment(Integer termId, String departmentId) {
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

    /**
     * 按班主任或辅导员统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByAdministrator(Integer termId, String userId, Integer offset, Integer max) {
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

    /**
     * 按班主任或辅导员统计教学班学生数量
     * @param termId 学期
     * @param userId 用户ID
     * @return 教学班学生数
     */
    def adminClassesByAdministrator(Integer termId, String userId) {
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

    /**
     * 按行政班统计学生考勤
     * @param termId 学期
     * @param adminClassId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByAdminClass(Integer termId, Long adminClassId, Integer offset, Integer max) {
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
and adminClass.id = :adminClassId
and sa.valid = true
group by student, adminClass
order by total desc
''', [termId: termId, adminClassId: adminClassId], [offset: offset, max: max]
    }

    /**
     * 按考勤命令统计考勤次数，按教学班汇总
     * @param cmd 考勤命令
     * @return 考勤统计 [studentId: [absent, late, early, leave]]
     */
    def statsByRollcall(RollcallCommand cmd) {
        def results = TaskStudent.executeQuery '''
select attendance.student.id as id,
    count(case attendance.type when 1 then 1 end) as absent,
    count(case attendance.type when 2 then 1 when 5 then 1 end) as late,
    count(case attendance.type when 3 then 1 when 5 then 1 end) as early,
    count(case attendance.type when 4 then 1 end) as leave
from StudentAttendance attendance
where (attendance.student.id, attendance.taskSchedule.id) in (
    select student.id, taskSchedule2.id
    from CourseClass courseClass
    join courseClass.tasks task1
    join task1.schedules taskSchedule1
    join task1.students taskStudent1
    join taskStudent1.student student
    join courseClass.tasks task2
    join task2.schedules taskSchedule2
    where courseClass.term.id = :termId
    and taskSchedule1.teacher.id = :teacherId
    and :week between taskSchedule1.startWeek and taskSchedule1.endWeek
    and (taskSchedule1.oddEven = 0
      or taskSchedule1.oddEven = 1 and :week % 2 = 1
      or taskSchedule1.oddEven = 2 and :week % 2 = 0)
    and taskSchedule1.dayOfWeek = :dayOfWeek
    and taskSchedule1.startSection = :startSection
)
and attendance.valid = true
group by attendance.student
''', cmd as Map

        results.collectEntries {Object[]  item ->
            [item[0], item[1..4]]
        }
    }

    /**
     * 按安排统计指定学生的教学班考勤次数统计
     * @param studentId 学生ID
     * @param taskScheduleId 安排ID
     * @return [absent, late, early, leave]
     */
    def studentCourseClassStats(String studentId, UUID taskScheduleId) {
        def results = TaskStudent.executeQuery '''
select count(case attendance.type when 1 then 1 end) as absent,
    count(case attendance.type when 2 then 1 when 5 then 1 end) as late,
    count(case attendance.type when 3 then 1 when 5 then 1 end) as early,
    count(case attendance.type when 4 then 1 end) as leave
from StudentAttendance attendance
where attendance.taskSchedule.id in (
  select taskSchedule2.id
  from CourseClass courseClass
  join courseClass.tasks task1
  join task1.schedules taskSchedule1
  join courseClass.tasks task2
  join task2.schedules taskSchedule2
  where taskSchedule1.id = :taskScheduleId 
)
and attendance.student.id = :studentId
and attendance.valid = true
''', [studentId: studentId, taskScheduleId: taskScheduleId]

        results ? results[0] : [0, 0, 0, 0]
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param studentId 学生ID
     * @param termId 学期
     */
    def getStudentAttendances(String studentId, Integer termId) {
        StudentAttendance.executeQuery '''
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
    }
}
