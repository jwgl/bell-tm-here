package cn.edu.bnuz.bell.here

/**
 * Created by yanglin on 2017/5/17.
 */
class TeacherTimeslotCommand {
    Integer termId
    String teacherId
    Integer week
    Integer dayOfWeek
    Integer startSection

    def setTimeslot(Integer timeslot) {
        this.dayOfWeek = timeslot.intdiv(100)
        this.startSection = timeslot % 100
    }

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
