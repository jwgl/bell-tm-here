package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.here.dto.StudentAttendance
import cn.edu.bnuz.bell.here.dto.TaskStudentDto
import cn.edu.bnuz.bell.here.eto.TaskStudentEto
import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.operation.CourseClass
import cn.edu.bnuz.bell.operation.Task
import cn.edu.bnuz.bell.operation.TaskStudent
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.UserLogService
import grails.gorm.transactions.Transactional

@Transactional
class CourseClassStudentService {
    private static final String EXAM_DISQUAL = '取消资格'
    private static final String ERROR_TEST_SCHEDULED = '教学班已安排考试'
    private static final String ERROR_SCORE_COMMITTED = '学生成绩已提交'

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
    void disqualify(String teacherId, UUID courseClassId, String studentId) {
        List taskCodes = getAndCheckTaskCodes(teacherId, courseClassId, studentId)

        TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = :examFlag
where studentId = :studentId 
  and taskCode in (:taskCodes)
  and examFlag is null
''', [taskCodes: taskCodes, studentId: studentId, examFlag: EXAM_DISQUAL]

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
    }

    /**
     * 恢复考试资格
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     */
    void qualify(String teacherId, UUID courseClassId, String studentId) {
        List taskCodes = getAndCheckTaskCodes(teacherId, courseClassId, studentId)

        TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = null
where studentId = :studentId 
  and taskCode in (:taskCodes)
  and examFlag = :examFlag
''', [taskCodes: taskCodes, studentId: studentId, examFlag: EXAM_DISQUAL]

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
    }

    /**
     * 查询选课课号
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     * @return 课号列表
     */
    private List<String> getAndCheckTaskCodes(String teacherId, UUID courseClassId, String studentId) {
        List taskCodes = Task.executeQuery '''
select distinct task.code
from CourseClass courseClass
join courseClass.tasks task
where courseClass.id = :courseClassId
and courseClass.teacher.id = :teacherId
''', [teacherId: teacherId, courseClassId: courseClassId]

        if (taskCodes.size() == 0) {
            throw new NotFoundException()
        }

        List<TaskStudentDto> taskStudentDtos = TaskStudentDto.findAllByStudentIdAndTaskCodeInList(studentId, taskCodes)

        if (taskStudentDtos.any {it.testScheduled}) {
            throw new BadRequestException(ERROR_TEST_SCHEDULED)
        }

        if (taskStudentDtos.any {it.scoreCommitted}) {
            throw new BadRequestException(ERROR_SCORE_COMMITTED)
        }

        return taskCodes
    }
}
