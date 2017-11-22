package cn.edu.bnuz.bell.here

import org.codehaus.groovy.util.HashCodeHelper

/**
 * 考勤类型系数
 * @author Yang Lin
 */
class AttendanceTypeRatio implements Serializable{
    /**
     * 考勤类型
     */
    Integer type

    /**
     * 统计类型
     */
    Integer asType

    /**
     * 每次折算数，如早退一次记0.5课时
     */
    BigDecimal instanceRatio

    /**
     * 按上课长度totalSection折算数，如上课长度为2，比例是0.5，则记2*0.5=1课时
     */
    BigDecimal sectionsRatio

    static belongsTo = [
            attendanceSettings: AttendanceSettings
    ]

    static mapping = {
        comment       '考勤类型系数'
        id            composite: ['attendanceSettings', 'type', 'asType']
        type          comment: '考勤类型-1:旷课;2:迟到;3:早退;4-请假;5退到+早退'
        asType        comment: '统计类型'
        instanceRatio precision: 2, scale: 1, comment: '考勤教师'
        sectionsRatio precision: 2, scale: 1, comment: '学生'
    }

    boolean equals(other) {
        if (!(other instanceof AttendanceTypeRatio)) {
            return false
        }

        other.attendanceSettings?.id == attendanceSettings?.id && other.type == type
    }

    int hashCode() {
        int hash = HashCodeHelper.initHash()
        hash = HashCodeHelper.updateHash(hash, attendanceSettings.id)
        hash = HashCodeHelper.updateHash(hash, type)
        hash
    }
}
