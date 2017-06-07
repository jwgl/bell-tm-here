# 点名
```
/teachers
    /{teacherId}
        /timeslots 教师指定学期课表
            /{timeslotId}
                /weeks/{week}
                    /rollcalls 时段所有选课学生+本次考勤数据(点名/请假/免听)
                        /{rollcallId} 点名记录
```

# 考勤统计

```
/teachers
    /{teacherId}
        /courseClasses 教师指定学期教学班（主讲教师）
            /{courseClassId} 教学班信息、教学班学生及考勤统计

/attendances 学生考勤统计（管理）
    /{studentId} 学生考勤详情（管理）
    /adminClasses 行政班列表，考勤人数统计，用于导航（管理）

/adminClasses
    /{adminClassId}
        /attendances 行政班学生考勤统计列表（管理）

/students
    /{studentId}
        /attendances 学生个人考勤详情

/departments/
    /{departmentId}
        /courseClassTeachers 指定学院的主讲教师

/courseClasses
    /{courseClassId} 教学班信息、教学班学生及考勤统计
        /students
            /{studentId} get: 教学班学生个人考勤信息; patch: 取消考试资格(op='DQ')
```