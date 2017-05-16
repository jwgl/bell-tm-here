package cn.edu.bnuz.bell.here

import grails.transaction.Transactional

@Transactional
class TimeslotAttendanceService {
    /**
     * 按时段命令统计考勤次数，按教学班汇总
     * @param cmd 时段命令
     * @return 考勤统计 [studentId: [absent, late, early, leave]]
     */
    def list(TeacherTimeslotCommand cmd) {
        def results = StudentAttendance.executeQuery '''
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

        results.collectEntries { Object[] item ->
            [item[0], item[1..4]]
        }
    }

    /**
     * 按安排统计指定学生的教学班考勤次数统计
     * @param cmd 时段命令
     * @param studentId 学生ID
     * @return [absent, late, early, leave]
     */
    def get(TeacherTimeslotCommand cmd, String studentId) {
        Map params = cmd as Map
        params.studentId = studentId

        def results = StudentAttendance.executeQuery '''
select 
  count(case attendance.type when 1 then 1 end) as absent,
  count(case attendance.type when 2 then 1 when 5 then 1 end) as late,
  count(case attendance.type when 3 then 1 when 5 then 1 end) as early,
  count(case attendance.type when 4 then 1 end) as leave
from StudentAttendance attendance
where attendance.taskSchedule.id in (
  select taskSchedule2.id
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
and attendance.student.id = :studentId
group by attendance.student
''', params

        results ? results[0] : [0, 0, 0, 0]
    }
}
