package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.master.Term
import cn.edu.bnuz.bell.operation.TaskSchedule
import cn.edu.bnuz.bell.system.SystemConfigService
import grails.gorm.transactions.Transactional
import grails.plugin.cache.Cacheable

@Transactional
class AttendanceTermService {
    SystemConfigService systemConfigService

    /**
     * 获取所有考勤学期
     * @return 学期ID列表
     */
    List<Integer> getTerms() {
        Term.executeQuery '''
select id
from Term
where id >= :startTerm and id <= (
  select id from Term where active = true
)
order by id desc
''', [startTerm: rollcallStartTerm()]
    }

    /**
     * 获取教师考勤学期
     * @param teacherId 教师ID
     * @return 学期ID列表
     */
    List<Integer> getTerms(String teacherId) {
        TaskSchedule.executeQuery('''
select distinct courseClass.term.id
from CourseClass courseClass
join courseClass.tasks task
left join task.schedules schedule
where (schedule.teacher.id = :teacherId or courseClass.teacher.id = :teacherId)
and courseClass.term.id >= :startTerm
order by courseClass.term.id desc
''', [teacherId: teacherId, startTerm: rollcallStartTerm()])
    }

    @Cacheable("rollcall.start_term")
    private Integer rollcallStartTerm() {
        systemConfigService.get('rollcall.start_term', 0)
    }
}
