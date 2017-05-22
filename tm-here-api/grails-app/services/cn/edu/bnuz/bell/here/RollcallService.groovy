package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import grails.gorm.transactions.Transactional
import org.hibernate.SessionFactory
import org.hibernate.result.ResultSetOutput

import javax.persistence.ParameterMode

@Transactional
class RollcallService {
    StudentLeavePublicService studentLeavePublicService
    FreeListenPublicService freeListenPublicService
    SessionFactory sessionFactory

    def list(TeacherTimeslotCommand cmd) {
        println(cmd as Map)
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
''', cmd as Map

        def students = TaskStudent.executeQuery '''
select new map (
  student.id as id,
  student.name as name,
  subject.name as subject,
  adminClass.name as adminClass,
  taskSchedule.id as taskScheduleId
)
from Task task
join task.schedules taskSchedule
join task.students taskStudent
join taskStudent.student student
join student.major major
join major.subject subject
join student.adminClass adminClass
where taskSchedule.id in (:taskScheduleIds)
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
                students     : students,
                rollcalls    : rollcalls,
                leaves       : studentLeavePublicService.listByTimeslot(cmd),
                freeListens  : freeListenPublicService.listByTimeslot(cmd),
                cancelExams  : [], // TODO Find cancel examine records
                attendances  : getStudentAttendaceByTimeslot(cmd),
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
                attendances: getStudentAttendanceByRollcall(rollcall)
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
        rollcall.save(flush: true)

        [
                attendances: getStudentAttendanceByRollcall(rollcall)
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

        rollcall.delete(flush: true)

        [
                attendances: getStudentAttendanceByRollcall(rollcall)
        ]
    }

    def canUpdate(Rollcall rollcall) {
        return true
    }

    /**
     * 按时段命令统计考勤次数，按教学班汇总
     * @param cmd 时段命令
     * @return 考勤统计 [studentId: [absent, late, early, leave]]
     */
    def getStudentAttendaceByTimeslot(TeacherTimeslotCommand cmd) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_timeslot')
        def outputs = query.with {
            [
                    p_term_id      : cmd.termId,
                    p_teacher_id   : cmd.teacherId,
                    p_week         : cmd.week,
                    p_day_of_week  : cmd.dayOfWeek,
                    p_start_section: cmd.startSection,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collectEntries { item ->
            [item[0], item[1..4]]
        }
    }

    /**
     * 按安排统计指定学生的教学班考勤次数统计
     * @param taskScheduleId 安排
     * @param studentId 学生ID
     * @return [absent, late, early, leave]
     */
    def getStudentAttendanceByRollcall(Rollcall rollcall) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_task_schedule_student')
        def outputs = query.with {
            [
                    p_task_schedule_id: rollcall.taskScheduleId,
                    p_student_id      : rollcall.student.id
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        def results = ((ResultSetOutput) outputs.current).resultList

        results ? results[0] : [0, 0, 0, 0]
    }
}
