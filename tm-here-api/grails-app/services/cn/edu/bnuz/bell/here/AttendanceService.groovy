package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.here.dto.AdminClassAttendanceStats
import cn.edu.bnuz.bell.here.dto.AdminClassStats
import cn.edu.bnuz.bell.here.dto.StudentAttendance
import grails.transaction.Transactional

@Transactional(readOnly = true)
class AttendanceService {
    /**
     * 按学院统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 考勤统计
     */
    def studentStatsByDepartment(Integer termId, String departmentId) {
        AdminClassAttendanceStats.statsByDepartment(termId, departmentId)
    }

    /**
     * 按学院统计教学班学生数量
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 教学班学生数
     */
    def adminClassesByDepartment(Integer termId, String departmentId) {
        AdminClassStats.statsByDepartment(termId, departmentId)
    }

    /**
     * 按班主任或辅导员统计学生考勤
     * @param termId 学期
     * @param departmentId 学院ID
     * @return 考勤统计
     */
    def studentStatsByAdministrator(Integer termId, String userId) {
        AdminClassAttendanceStats.statsByAdministrator(termId, userId)
    }

    /**
     * 按班主任或辅导员统计教学班学生数量
     * @param termId 学期
     * @param userId 用户ID
     * @return 教学班学生数
     */
    def adminClassesByAdministrator(Integer termId, String userId) {
        AdminClassStats.statsByAdministrator(termId, userId)
    }

    /**
     * 按行政班统计学生考勤
     * @param termId 学期
     * @param adminClassId 学院ID
     * @return 考勤统计
     */
    def studentStatsByAdminClass(Integer termId, Long adminClassId) {
        AdminClassAttendanceStats.statsByAdminClass(termId, adminClassId)
    }

    /**
     * 获取学生指定学期的考勤情况
     * @param termId 学期
     * @param studentId 学生ID
     * @return 考勤情况
     */
    def getStudentAttendances(Integer termId, String studentId) {
        [
                rollcalls: StudentAttendance.findRollcalls(termId, studentId),
                leaves   : StudentAttendance.findLeaves(termId, studentId),
        ]
    }
}
