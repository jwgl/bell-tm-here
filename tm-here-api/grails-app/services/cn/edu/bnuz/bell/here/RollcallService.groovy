package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskStudent
import grails.transaction.Transactional

@Transactional
class RollcallService {
    def getRollcallStudents(Term term, String teacherId, Integer week, Integer dayOfWeek, Integer startSection) {
        TaskStudent.executeQuery '''
select new Map (
    student.id as id,
    student.name as name,
    subject.name as subject,
    adminClass.name as adminClass,
    taskSchedule.id as taskScheduleId
)
from Task task
join task.students taskStudent
join taskStudent.student student
join student.major major
join major.subject subject
join student.adminClass adminClass
join task.schedules taskSchedule
join task.courseClass courseClass
where courseClass.term = :term
and taskSchedule.teacher.id = :teacherId
and :week between taskSchedule.startWeek and taskSchedule.endWeek
and taskSchedule.dayOfWeek = :dayOfWeek
and taskSchedule.startSection = :startSection
and (taskSchedule.oddEven = 0
  or taskSchedule.oddEven = 1 and :week % 2 = 1
  or taskSchedule.oddEven = 2 and :week % 2 = 0)
''', [term: term, teacherId: teacherId, week: week, dayOfWeek: dayOfWeek, startSection: startSection]
    }
}
