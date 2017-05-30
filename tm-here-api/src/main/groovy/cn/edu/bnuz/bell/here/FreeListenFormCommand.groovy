package cn.edu.bnuz.bell.here

import groovy.transform.CompileStatic

@CompileStatic
class FreeListenFormCommand {
    Long id
    String reason
    String checkerId

    List<String> addedItems
    List<String> removedItems
}
