package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.operation.CourseClass
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class DepartmentCourseClassService {

    /**
     * 获取指定学期开课单位的主讲教师
     * @param termId 学期
     * @param departmentId 开课单位ID
     */
    def getCourseClassTeachers(Integer termId, String departmentId) {
        CourseClass.executeQuery '''
select distinct new map (
  teacher.id as id,
  teacher.name as name
)
from CourseClass courseClass
join courseClass.teacher teacher
where courseClass.department.id = :departmentId
  and courseClass.term.id = :termId
order by teacher.name
''', [termId: termId, departmentId: departmentId]
    }
}
