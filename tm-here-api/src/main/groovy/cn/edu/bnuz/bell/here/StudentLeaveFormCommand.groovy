package cn.edu.bnuz.bell.here

class StudentLeaveFormCommand {
    Long id
    Integer type
    String reason

    List<LeaveItem> addedItems
    List<Integer> removedItems

    class LeaveItem {
        Long id
        Integer week
        Integer dayOfWeek
        String taskScheduleId
    }
}
