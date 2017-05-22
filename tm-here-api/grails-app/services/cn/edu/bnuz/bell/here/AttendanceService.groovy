package cn.edu.bnuz.bell.here

import grails.transaction.Transactional
import org.hibernate.SessionFactory
import org.hibernate.result.ResultSetOutput

import javax.persistence.ParameterMode

@Transactional
class AttendanceService {
    SessionFactory sessionFactory

    /**
     * 按学院统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByDepartment(Integer termId, String departmentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_department')
        def outputs = query.with {
            [
                    p_term_id      : termId,
                    p_department_id: departmentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id        : item[0],
                    name      : item[1],
                    adminClass: item[2],
                    absent    : item[3],
                    late      : item[4],
                    early     : item[5],
                    total     : item[6],
                    leave     : item[7],
            ]
        }
    }

    /**
     * 按学院统计教学班学生数量
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 教学班学生数
     */
    def adminClassesByDepartment(Integer termId, String departmentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_admin_class_attendance_stats_by_department')
        def outputs = query.with {
            [
                    p_term_id      : termId,
                    p_department_id: departmentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id   : item[0],
                    name : item[1],
                    count: item[2],
            ]
        }
    }

    /**
     * 按班主任或辅导员统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByAdministrator(Integer termId, String userId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_administrator')
        def outputs = query.with {
            [
                    p_term_id: termId,
                    p_user_id: userId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id        : item[0],
                    name      : item[1],
                    adminClass: item[2],
                    absent    : item[3],
                    late      : item[4],
                    early     : item[5],
                    total     : item[6],
                    leave     : item[7],
            ]
        }
    }

    /**
     * 按班主任或辅导员统计教学班学生数量
     * @param termId 学期
     * @param userId 用户ID
     * @return 教学班学生数
     */
    def adminClassesByAdministrator(Integer termId, String userId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_admin_class_attendance_stats_by_administrator')
        def outputs = query.with {
            [
                    p_term_id: termId,
                    p_user_id: userId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id   : item[0],
                    name : item[1],
                    count: item[2],
            ]
        }
    }

    /**
     * 按行政班统计学生考勤
     * @param termId 学期
     * @param adminClassId 学院ID
     * @param offset 偏移量
     * @param max 最大记录数
     * @return 考勤统计
     */
    def studentStatsByAdminClass(Integer termId, Long adminClassId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_admin_class')
        def outputs = query.with {
            [
                    p_term_id       : termId,
                    p_admin_class_id: adminClassId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id        : item[0],
                    name      : item[1],
                    adminClass: item[2],
                    absent    : item[3],
                    late      : item[4],
                    early     : item[5],
                    total     : item[6],
                    leave     : item[7],
            ]
        }
    }

    /**
     * 按教学班统计考勤节数
     * @param courseClassId 教学班统计
     * @return 考勤统计
     */
    def statsByCourseClass(UUID courseClassId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_attendance_stats_by_course_class')
        def outputs = query.with {
            [
                    p_course_class_id: courseClassId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }
        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    id    : item[0],
                    absent: item[1],
                    late  : item[2],
                    early : item[3],
                    total : item[4],
                    leave : item[5],
            ]
        }
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param studentId 学生ID
     * @param termId 学期
     * @return 考勤情况
     */
    def getStudentAttendances(Integer termId, String studentId) {
        [
                rollcalls    : getRollcallDetails(termId, studentId),
                studentLeaves: getStudentLeaveDetails(termId, studentId),
        ]
    }

    private getRollcallDetails(Integer termId, String studentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_rollcall_details_by_student')
        def outputs = query.with {
            [
                    p_term_id   : termId,
                    p_student_id: studentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    week        : item[0],
                    dayOfWeek   : item[1],
                    startSection: item[2],
                    totalSection: item[3],
                    type        : item[4],
                    course      : item[5],
                    courseItem  : item[6],
                    teacher     : item[7],
                    studentLeave: item[8],
                    freeListen  : item[9],
            ]
        }
    }

    private getStudentLeaveDetails(Integer termId, String studentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_leave_details_by_student')
        def outputs = query.with {
            [
                    p_term_id   : termId,
                    p_student_id: studentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    week        : item[0],
                    dayOfWeek   : item[1],
                    startSection: item[2],
                    totalSection: item[3],
                    type        : item[4],
                    course      : item[5],
                    courseItem  : item[6],
                    teacher     : item[7],
                    studentLeave: item[8],
                    freeListen  : item[9],
            ]
        }
    }

    /**
     * 获取学生指定教学班的考勤情况
     * @param studentId 学生ID
     * @param courseClassId 教学班ID
     * @return 考勤情况
     */
    def getStudentAttendances(UUID courseClassId, String studentId) {
        println('leave details')

        [
                rollcalls    : getRollcallDetails(courseClassId, studentId),
                studentLeaves: getStudentLeaveDetails(courseClassId, studentId),
        ]
    }

    private getRollcallDetails(UUID courseClassId, String studentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_rollcall_details_by_course_class_student')
        def outputs = query.with {
            [
                    p_course_class_id: courseClassId,
                    p_student_id     : studentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    week        : item[0],
                    dayOfWeek   : item[1],
                    startSection: item[2],
                    totalSection: item[3],
                    type        : item[4],
                    course      : item[5],
                    courseItem  : item[6],
                    teacher     : item[7],
                    studentLeave: item[8],
                    freeListen  : item[9],
            ]
        }
    }

    private getStudentLeaveDetails(UUID courseClassId, String studentId) {
        def session = sessionFactory.currentSession
        def query = session.createStoredProcedureCall('sp_get_student_leave_details_by_course_class_student')
        def outputs = query.with {
            [
                    p_course_class_id: courseClassId,
                    p_student_id     : studentId,
            ].each { k, v ->
                registerParameter(k, v.class, ParameterMode.IN).bindValue(v)
            }
            outputs
        }

        ((ResultSetOutput) outputs.current).resultList.collect { item ->
            [
                    week        : item[0],
                    dayOfWeek   : item[1],
                    startSection: item[2],
                    totalSection: item[3],
                    type        : item[4],
                    course      : item[5],
                    courseItem  : item[6],
                    teacher     : item[7],
                    studentLeave: item[8],
                    freeListen  : item[9],
            ]
        }
    }
}
