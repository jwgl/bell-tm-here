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
    private static final String ERROR_DISQUAL_BY_ADMIN = '教务秘书取消考试资格，无法恢复'
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
    def show(UUID courseClassId, String studentId) {
        checkPermission(courseClassId)

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
    void disqualify(UUID courseClassId, String studentId) {
        checkPermission(courseClassId)

        List taskCodes = getAndCheckTaskCodes(courseClassId, studentId)

        TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = :examFlag,
    operator = :operator
where studentId = :studentId 
  and taskCode in (:taskCodes)
  and examFlag is null
''', [taskCodes: taskCodes, studentId: studentId, examFlag: EXAM_DISQUAL, operator: securityService.userId]

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

        userLogService.log(securityService.userId, securityService.ipAddress, CourseClass,
                'DISQUALIFY', courseClassId.toString(), studentId)
    }

    /**
     * 恢复考试资格
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     */
    void qualify(UUID courseClassId, String studentId) {
        checkPermission(courseClassId)

        List taskCodes = getAndCheckTaskCodes(courseClassId, studentId)

        TaskStudentEto.executeUpdate '''
update TaskStudentEto
set examFlag = null,
    operator = null
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

        userLogService.log(securityService.userId, securityService.ipAddress, CourseClass,
                'QUALIFY', courseClassId.toString(), studentId)
    }

    /**
     * 检查用户操作权限
     * @param courseClassId 教学班
     */
    private void checkPermission(UUID courseClassId) {
        CourseClass courseClass = CourseClass.get(courseClassId)
        if (!courseClass) {
            throw new NotFoundException()
        }

        if (isAdmin()) {
            if (courseClass.departmentId != securityService.departmentId) {
                throw new ForbiddenException()
            }
        } else {
            if (courseClass.teacherId != securityService.userId) {
                throw new ForbiddenException()
            }
        }
    }

    /**
     * 查询选课课号
     * @param teacherId 教工号
     * @param courseClassId 教学班ID
     * @param studentId 学号
     * @return 课号列表
     */
    private List<String> getAndCheckTaskCodes(UUID courseClassId, String studentId) {
        List taskCodes = Task.executeQuery '''
select distinct task.code
from CourseClass courseClass
join courseClass.tasks task
where courseClass.id = :courseClassId
''', [courseClassId: courseClassId]

        if (taskCodes.size() == 0) {
            throw new NotFoundException()
        }

        List<TaskStudentDto> taskStudentDtos = TaskStudentDto.findAllByStudentIdAndTaskCodeInList(studentId, taskCodes)

        if (!isAdmin()) {
            if (taskStudentDtos.any {it.examFlag == EXAM_DISQUAL}) {
                if (taskStudentDtos.any { it.operator != securityService.userId}) {
                    throw new BadRequestException(ERROR_DISQUAL_BY_ADMIN)
                }

                if (taskStudentDtos.any { it.testScheduled }) {
                    throw new BadRequestException(ERROR_TEST_SCHEDULED)
                }
            }
        }

        if (taskStudentDtos.any {it.scoreCommitted}) {
            throw new BadRequestException(ERROR_SCORE_COMMITTED)
        }

        return taskCodes
    }

    /**
     * 是否为管理员
     */
    private Boolean isAdmin() {
        securityService.hasPermission('PERM_EXAM_DISQUAL_DEPT_ADMIN')
    }
}
