package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import grails.transaction.Transactional

@Transactional
class RollcallFormService {
    StudentLeavePublicService studentLeavePublicService
    FreeListenPublicService freeListenPublicService
    AttendanceService attendanceService

    def getFormForCreate(RollcallCommand cmd) {
        def students = TaskStudent.executeQuery '''
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
where courseClass.term.id = :termId
and taskSchedule.teacher.id = :teacherId
and :week between taskSchedule.startWeek and taskSchedule.endWeek
and (taskSchedule.oddEven = 0
  or taskSchedule.oddEven = 1 and :week % 2 = 1
  or taskSchedule.oddEven = 2 and :week % 2 = 0)
and taskSchedule.dayOfWeek = :dayOfWeek
and taskSchedule.startSection = :startSection
''', cmd as Map

        def rollcalls = Rollcall.executeQuery '''
select new Map (
    rollcall.id as id,
    rollcall.student.id as studentId,
    rollcall.type as type
)
from Rollcall rollcall
join rollcall.taskSchedule taskSchedule
join taskSchedule.task task
join task.students taskStudent
join task.courseClass courseClass
where taskStudent.student = rollcall.student
and courseClass.term.id = :termId
and taskSchedule.teacher.id = :teacherId
and rollcall.week = :week
and taskSchedule.dayOfWeek = :dayOfWeek
and taskSchedule.startSection = :startSection
''', cmd as Map

        [
                students   : students,
                rollcalls  : rollcalls,
                leaves     : studentLeavePublicService.listByRollcall(cmd),
                freeListens: freeListenPublicService.listByRollcall(cmd),
                cancelExams: [], // TODO Find cancel examine records
                attendances: attendanceService.statsByRollcall(cmd),
                locked     : false,
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
        rollcall.save()

        return [
                id: rollcall.id,
                attendances: attendanceService.studentCourseClassStats(
                        rollcall.student.id,
                        rollcall.taskSchedule.id
                ),
        ]
    }

    def update(String teacherId, RollcallUpdateCommand cmd) {
        def rollcall = Rollcall.get(cmd.id)

        if (!rollcall) {
            throw new NotFoundException()
        }

        if (rollcall.teacher.id != teacherId) {
            throw new ForbiddenException()
        }

        if (!canUpdate(rollcall)) {
            throw new BadRequestException()
        }

        rollcall.type = cmd.type
        rollcall.save()

        return [
                attendances: attendanceService.studentCourseClassStats(
                        rollcall.student.id,
                        rollcall.taskSchedule.id
                ),
        ]
    }

    def delete(String teacherId, Long id) {
        def rollcall = Rollcall.get(id)

        if (!rollcall) {
            throw new NotFoundException()
        }

        if (rollcall.teacher.id != teacherId) {
            throw new ForbiddenException()
        }

        if (!canUpdate(rollcall)) {
            throw new BadRequestException()
        }

        rollcall.delete()

        return [
                attendances: attendanceService.studentCourseClassStats(
                        rollcall.student.id,
                        rollcall.taskSchedule.id
                ),
        ]
    }

    def canUpdate(Rollcall rollcall) {
        return true
    }
}
