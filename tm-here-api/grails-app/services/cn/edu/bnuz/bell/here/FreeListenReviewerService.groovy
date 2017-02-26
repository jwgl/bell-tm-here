package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.transaction.Transactional

@Transactional(readOnly = true)
class FreeListenReviewerService implements ReviewerProvider{
    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.CHECK:
                return getCheckers(id as Long)
            case Activities.APPROVE:
                return getApprovers()
            default:
                throw new BadRequestException()
        }
    }

    List<Map> getCheckers(Long id) {
        FreeListenForm.executeQuery '''
select new map (
  checker.id as id,
  checker.name as name
)
from FreeListenForm form
join form.checker checker
where form.id = :id
''', [id: id]
    }

    List<Map> getApprovers() {
        User.findAllWithPermission('PERM_FREE_LISTEN_APPROVE')
    }
}
