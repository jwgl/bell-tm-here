package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.workflow.State
import grails.transaction.Transactional

@Transactional
class StudentLeaveRequestService {

    /**
     * 各状态申请数量
     * @return 各状态申请数量
     */
    def getCountsByStatus() {
        def results = StudentLeaveForm.executeQuery("""
select status, count(*)
from StudentLeaveForm
group by status
""")
        return results.collectEntries {[it[0].name(), it[1]]}
    }

    /**
     * 查找所有指定状态的申请（DTO）
     * @param status
     * @param offset
     * @param max
     * @return
     */
    def findAllByStatus(State status, int offset, int max) {
        StudentLeaveForm.executeQuery """
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  adminClass.name as adminClass,
  form.type as type,
  form.reason as reason,
  form.dateModified as applyDate,
  form.status as status
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
where form.status = :status
order by form.dateModified desc
""", [status: status], [offset: offset, max: max]
    }


}
