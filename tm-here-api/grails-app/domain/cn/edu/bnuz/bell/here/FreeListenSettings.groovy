package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term

import java.time.LocalDate

class FreeListenSettings {
    /**
     * 虚拟ID，对应属性#term
     */
    Integer id

    /**
     * 学期
     */
    Term term

    /**
     * 免听申请开始时间
     */
    LocalDate applyStartDate

    /**
     * 免听申请结束时间
     */
    LocalDate applyEndDate

    /**
     * 免听审核开始时间
     */
    LocalDate checkStartDate

    /**
     * 免听审核结束时间
     */
    LocalDate checkEndDate

    /**
     * 当前日期
     */
    LocalDate today = LocalDate.now()

    boolean betweenApplyDateRange() {
        today >= applyStartDate && today <= applyEndDate
    }


    static transients = ['today']

    static mapping = {
        comment '免听设置'
        id             column: 'term_id', type: 'integer', generator: 'foreign', params: [ property: 'term']
        term           comment: '学期', insertable: false, updateable: false
        applyStartDate comment: '免听申请开始时间'
        applyEndDate   comment: '免听申请结束时间'
        checkStartDate comment: '免听审核开始时间'
        checkEndDate   comment: '免听审核结束时间'
    }
}
