package cn.edu.bnuz.bell.here

import groovy.transform.ToString

@ToString
class RollcallCreateCommand {
    Integer termId
    Integer week
    UUID taskScheduleId
    String studentId
    Integer type
}
