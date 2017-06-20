package cn.edu.bnuz.bell.here

import cn.edu.bnuz.bell.profile.UserSettingService
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class TeacherSettingService {
    static final String ROLLCALL_SETTINGS_KEY = 'rollcall.settings'
    static final String ROLLCALL_VIEW_KEY = 'rollcall.view'

    UserSettingService userSettingService

    def getRollcallSettings(String teacherId) {
        userSettingService.get(teacherId, ROLLCALL_SETTINGS_KEY, [:])
    }

    def getRollcallView(String teacherId) {
        userSettingService.get(teacherId, ROLLCALL_VIEW_KEY, 'detail')
    }

    def setRollcallSettings(String teacherId, Map value) {
        userSettingService.set(teacherId, ROLLCALL_SETTINGS_KEY, value)
    }

    def setRollcallView(String teacherId, String value) {
        userSettingService.set(teacherId, ROLLCALL_VIEW_KEY, value)
    }
}
