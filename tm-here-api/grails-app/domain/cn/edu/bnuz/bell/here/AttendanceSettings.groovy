package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term

/**
 * 考勤设置
 * @author Yang Lin
 */
class AttendanceSettings {
    /**
     * ID
     */
    Integer id

    /**
     * 开始学期
     */
    Term startTerm

    /**
     * 结束学期
     */
    Term endTerm

    /**
     * 旷课取消资格比例
     */
    BigDecimal absentDisqualRatio

    /**
     * 缺勤取消资格比例
     */
    BigDecimal attendDisqualRatio

    static hasMany = [
            typeRatios: AttendanceTypeRatio
    ]

    static mapping = {
        comment            '考勤设置'
        id                 generator: 'assigned', comment: '考勤设置ID'
        startTerm          comment: '开始学期'
        endTerm            comment: '结束学期'
        absentDisqualRatio precision: 6, scale: 6, comment: '旷课取消资格比例'
        attendDisqualRatio precision: 6, scale: 6, comment: '缺勤取消资格比例'
    }

    static constraints = {
        startTerm  nullable: true
        endTerm    nullable: true
    }
}
