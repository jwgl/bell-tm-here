package cn.edu.bnuz.bell.here

import grails.transaction.Transactional

@Transactional
class AttendanceService {
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
     * 按教学班统计考勤节数
     * @param courseClassId 教学班统计
     * @return 考勤统计
     */
    def statsByCourseClass(UUID courseClassId) {
        StudentAttendance.executeQuery '''
select new map(
  attendance.student.id as id,
  sum(case when type = 1 then schedule.totalSection else 0 end) as absent,
  sum(case when type = 2 then 0.5 else 0 end) as late,
  sum(case when type in (3, 5) then schedule.totalSection else 0 end) as early,
  sum(case when type = 4 then schedule.totalSection else 0 end) as leave
)
from StudentAttendance attendance
join attendance.taskSchedule schedule
join schedule.task task
where task.courseClass.id = :courseClassId
and attendance.valid = true
group by attendance.student
''', [courseClassId: courseClassId]
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param studentId 学生ID
     * @param termId 学期
     * @return 考勤情况
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

    /**
     * 获取学生指定教学班的考勤情况
     * @param studentId 学生ID
     * @param courseClassId 教学班ID
     * @return 考勤情况
     */
    def getStudentAttendances(String studentId, UUID courseClassId) {
        StudentAttendance.executeQuery '''
select new map (
  sa.week as week,
  teacher.name as teacher,
  schedule.dayOfWeek as dayOfWeek,
  schedule.startSection as startSection,
  schedule.totalSection as totalSection,
  sa.type as type,
  course.name as course,
  courseItem.name as courseItem,
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
left join task.courseItem courseItem
where courseClass.id = :courseClassId
and sa.student.id = :studentId
order by week, dayOfWeek, startSection
''', [courseClassId: courseClassId, studentId: studentId]
    }
}
