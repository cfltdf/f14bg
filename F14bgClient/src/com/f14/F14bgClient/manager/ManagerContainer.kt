package com.f14.F14bgClient.manager

/**
 * 业务类容器

 * @author F14eagle
 */
object ManagerContainer {
    var connectionManager: ConnectionManager
    var propertiesManager: PropertiesManager
    var pathManager: PathManager
    var shellManager: ShellManager
    var codeManager: CodeManager
    var fileManager: FileManager
    var resourceManager: ResourceManager
    var actionManager: ActionManager
    var updateManager: UpdateManager
    var notifyManager: NotifyManager

    init {
        connectionManager = ConnectionManager()
        propertiesManager = PropertiesManager()
        pathManager = PathManager()
        shellManager = ShellManager()
        codeManager = CodeManager()
        fileManager = FileManager()
        resourceManager = ResourceManager()
        actionManager = ActionManager()
        updateManager = UpdateManager()
        notifyManager = NotifyManager()
    }

}
