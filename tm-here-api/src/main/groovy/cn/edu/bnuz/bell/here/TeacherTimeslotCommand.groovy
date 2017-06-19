package cn.edu.bnuz.bell.here

class TeacherTimeslotCommand {
    Integer termId
    String teacherId
    Integer week
    Integer dayOfWeek
    Integer startSection
    Integer totalSection

    def setTimeslot(Integer timeslot) {
        this.dayOfWeek = timeslot.intdiv(10000)
        this.startSection = (timeslot % 10000).intdiv(100)
        this.totalSection = timeslot % 100
    }

    public <T> T asType(Class<T> clazz) {
        if (clazz == Map) {
            return [
                    termId      : termId,
                    teacherId   : teacherId,
                    week        : week,
                    dayOfWeek   : dayOfWeek,
                    startSection: startSection,
                    totalSection: totalSection,
            ]
        } else {
            super.asType(clazz)
        }
    }
}
