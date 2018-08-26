package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.here.dto.TimeslotAttendanceStats
import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import grails.gorm.transactions.Transactional

@Transactional
class RollcallService {
    StudentLeavePublicService studentLeavePublicService
    FreeListenPublicService freeListenPublicService

    def list(TeacherTimeslotCommand cmd) {
        def taskScheduleIds = TaskSchedule.executeQuery '''
select taskSchedule.id
from CourseClass courseClass
join courseClass.tasks task
join task.schedules taskSchedule
where courseClass.term.id = :termId
  and taskSchedule.teacher.id = :teacherId
  and :week between taskSchedule.startWeek and taskSchedule.endWeek
  and (taskSchedule.oddEven = 0
   or taskSchedule.oddEven = 1 and :week % 2 = 1
   or taskSchedule.oddEven = 2 and :week % 2 = 0)
  and taskSchedule.dayOfWeek = :dayOfWeek
  and taskSchedule.startSection = :startSection
  and taskSchedule.totalSection = :totalSection
''', cmd as Map

        def students = TaskStudent.executeQuery '''
select new map (
  student.id as id,
  student.name as name,
  subject.name as subject,
  adminClass.name as adminClass,
  taskSchedule.id as taskScheduleId,
  taskStudent.examFlag = 1 as disqualified
)
from Task task
join task.schedules taskSchedule
join task.students taskStudent
join taskStudent.student student
join student.major major
join major.subject subject
join student.adminClass adminClass
where taskSchedule.id in (:taskScheduleIds)
order by student.id
''', [taskScheduleIds: taskScheduleIds]

        def rollcalls = Rollcall.executeQuery '''
select new map (
  rollcall.id as id,
  rollcall.student.id as studentId,
  rollcall.type as type
)
from Rollcall rollcall
join rollcall.taskSchedule taskSchedule
join taskSchedule.task task
join task.students taskStudent
where taskStudent.student = rollcall.student
and rollcall.week = :week
and taskSchedule.id in (:taskScheduleIds)
''', [week: cmd.week, taskScheduleIds: taskScheduleIds]

        [
                students   : students,
                rollcalls  : rollcalls,
                leaves     : studentLeavePublicService.listByTimeslot(cmd),
                freeListens: freeListenPublicService.listByTimeslot(cmd),
                attendances: TimeslotAttendanceStats.statsByTimeslot(cmd),
        ]
    }

    def create(String teacherId, RollcallCreateCommand cmd) {
        def now = new Date()
        def rollcall = new Rollcall(
                teacher: Teacher.load(teacherId),
                student: Student.load(cmd.studentId),
                taskSchedule: TaskSchedule.load(cmd.taskScheduleId),
                week: cmd.week,
                type: cmd.type,
                dateCreated: now,
                dateModified: now,
        )
        rollcall.save(flush: true)

        [
                id         : rollcall.id,
                attendances: TimeslotAttendanceStats.statsByRollcall(rollcall)
        ]
    }

    def update(String teacherId, RollcallUpdateCommand cmd) {
        def rollcall = Rollcall.get(cmd.id)

        if (!rollcall) {
            throw new NotFoundException()
        }

        if (rollcall.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        if (!canUpdate(rollcall)) {
            throw new BadRequestException()
        }

        rollcall.type = cmd.type
        rollcall.save(flush: true)

        [
                attendances: TimeslotAttendanceStats.statsByRollcall(rollcall)
        ]
    }

    def delete(String teacherId, Long id) {
        def rollcall = Rollcall.get(id)

        if (!rollcall) {
            throw new NotFoundException()
        }

        if (rollcall.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        if (!canUpdate(rollcall)) {
            throw new BadRequestException()
        }

        rollcall.delete(flush: true)

        [
                attendances: TimeslotAttendanceStats.statsByRollcall(rollcall)
        ]
    }

    def canUpdate(Rollcall rollcall) {
        return true
    }
}
