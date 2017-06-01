package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.here.dto.StudentAttendance
import cn.edu.bnuz.bell.here.eto.TaskStudentEto
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.CourseClass
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.UserLogService
import grails.gorm.transactions.Transactional

@Transactional
class CourseClassStudentService {
    private static final String EXAM_DISQUAL = '取消资格'

    SecurityService securityService
    UserLogService userLogService

    /**
     * 获取教学班学生个人考勤详细信息
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     * @return 学生个人考勤详细信息
     */
    def show(String teacherId, UUID courseClassId, String studentId) {
        CourseClass courseClass = CourseClass.get(courseClassId)
        if (!courseClass) {
            throw new NotFoundException()
        }

        if (courseClass.teacherId != teacherId) {
            throw new ForbiddenException()
        }

        [
                rollcalls: StudentAttendance.findRollcalls(courseClassId, studentId),
                leaves   : StudentAttendance.findLeaves(courseClassId, studentId),
        ]
    }

    /**
     * 取消考试资格
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     */
    Boolean disqualify(String teacherId, UUID courseClassId, String studentId) {
        def count = TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = :examFlag
where studentId = :studentId 
  and taskCode in (
    select task.code
    from CourseClass courseClass
    join courseClass.tasks task
    where courseClass.id = :courseClassId
) and examFlag is null
and testScheduled = false
and locked = false
''', [courseClassId: courseClassId, studentId: studentId, examFlag: EXAM_DISQUAL]

        if (count == 0) {
            return false
        }

        TaskStudent.executeUpdate '''
update TaskStudent
set examFlag = 1
where student.id = :studentId
  and task.id in (
    select task.id
    from CourseClass courseClass
    join courseClass.tasks task
    where courseClass.id = :courseClassId
  ) and examFlag = 0
''', [courseClassId: courseClassId, studentId: studentId]

        userLogService.log(teacherId, securityService.ipAddress, CourseClass,
                'DISQUALIFY', courseClassId.toString(), studentId)

        return true
    }

    /**
     * 恢复考试资格
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     */
    Boolean qualify(String teacherId, UUID courseClassId, String studentId) {
        def count = TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = null
where studentId = :studentId 
  and taskCode in (
    select task.code
    from CourseClass courseClass
    join courseClass.tasks task
    where courseClass.id = :courseClassId
) and examFlag = :examFlag 
and testScheduled = false
and locked = false
''', [courseClassId: courseClassId, studentId: studentId, examFlag: EXAM_DISQUAL]

        if (count == 0) {
            return false
        }

        TaskStudent.executeUpdate '''
update TaskStudent
set examFlag = 0
where student.id = :studentId
  and task.id in (
    select task.id
    from CourseClass courseClass
    join courseClass.tasks task
    where courseClass.id = :courseClassId
  ) and examFlag = 1
''', [courseClassId: courseClassId, studentId: studentId]

        userLogService.log(teacherId, securityService.ipAddress, CourseClass,
                'QUALIFY', courseClassId.toString(), studentId)

        return true
    }
}
