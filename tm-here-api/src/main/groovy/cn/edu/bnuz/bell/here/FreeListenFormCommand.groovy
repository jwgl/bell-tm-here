package cn.edu.bnuz.bell.here

class FreeListenFormCommand {
    Long id
    String reason
    String checkerId

    List<LeaveItem> addedItems
    List<Integer> removedItems

    class LeaveItem {
        Long id
        String taskScheduleId
    }
}
