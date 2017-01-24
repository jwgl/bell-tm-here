package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
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
and (taskSchedule.oddEven = 0
  or taskSchedule.oddEven = 1 and :week % 2 = 1
  or taskSchedule.oddEven = 2 and :week % 2 = 0)
and taskSchedule.dayOfWeek = :dayOfWeek
and taskSchedule.startSection = :startSection
''', [term: term, teacherId: teacherId, week: week, dayOfWeek: dayOfWeek, startSection: startSection]
    }

    def getRollcalls(Term term, String teacherId, Integer week, Integer dayOfWeek, Integer startSection) {
        Rollcall.executeQuery '''
select new Map (
    rollcall.id as id,
    student.id as studentId,
    rollcall.type as type
)
from Rollcall rollcall
join rollcall.student student
join rollcall.taskSchedule taskSchedule
join taskSchedule.task task
join task.courseClass courseClass
where courseClass.term = :term
and taskSchedule.teacher.id = :teacherId
and rollcall.week = :week
and taskSchedule.dayOfWeek = :dayOfWeek
and taskSchedule.startSection = :startSection
''', [term: term, teacherId: teacherId, week: week, dayOfWeek: dayOfWeek, startSection: startSection]
    }

    def create(String teacherId, RollcallCreateCommand cmd) {
        def rollcall = new Rollcall(
                teacher: Teacher.load(teacherId),
                student: Student.load(cmd.studentId),
                taskSchedule: TaskSchedule.load(cmd.taskScheduleId),
                week: cmd.week,
                type: cmd.type,
                dateCreated: new Date(),
                dateModified: new Date(),
        )
        rollcall.save()
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
    }

    def canUpdate(Rollcall rollcall) {
        return true
    }
}
