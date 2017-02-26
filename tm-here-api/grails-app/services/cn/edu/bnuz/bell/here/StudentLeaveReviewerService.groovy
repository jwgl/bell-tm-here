package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.transaction.Transactional

@Transactional(readOnly = true)
class StudentLeaveReviewerService implements ReviewerProvider{
    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.APPROVE:
                return getApprovers(id as Long)
            default:
                throw new BadRequestException()
        }
    }

    def getApprovers(Long id) {
        Student.executeQuery '''
select new map(
  counsellor.id as id,
  counsellor.name as name
)
from StudentLeaveForm form
join form.student student
join student.adminClass adminClass
join adminClass.counsellor counsellor
where form.id = :id
''', [id: id]
    }
}
