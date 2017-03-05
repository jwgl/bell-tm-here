package cn.edu.bnuz.bell.here

class RollcallCommand {
    Integer termId
    String teacherId
    Integer week
    Integer dayOfWeek
    Integer startSection

    public <T> T asType(Class<T> clazz) {
        if (clazz == Map) {
            return [
                    termId      : termId,
                    teacherId   : teacherId,
                    week        : week,
                    dayOfWeek   : dayOfWeek,
                    startSection: startSection
            ]
        } else {
            super.asType(clazz)
        }
    }
}
