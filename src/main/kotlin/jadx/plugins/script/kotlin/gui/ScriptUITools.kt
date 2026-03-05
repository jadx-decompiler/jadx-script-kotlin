package jadx.plugins.script.kotlin.gui

import jadx.core.utils.exceptions.JadxRuntimeException
import jadx.core.utils.files.FileUtils
import jadx.gui.ui.MainWindow
import jadx.gui.ui.filedialog.FileDialogWrapper
import jadx.gui.ui.filedialog.FileOpenMode
import jadx.gui.utils.NLS
import jadx.plugins.script.kotlin.JadxScriptKotlinPlugin
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JFileChooser

internal fun addNewInputScript(mainWindow: MainWindow) {
	val project = mainWindow.project
	val baseScriptName = project.name.takeIf { it.isNotBlank() }?.substringBeforeLast('.') ?: "script"
	val baseDir = project.workingDir ?: mainWindow.settings.lastSaveFilePath ?: Paths.get(".")
	val fileDialog = FileDialogWrapper(mainWindow, FileOpenMode.CUSTOM_SAVE)
	fileDialog.title = NLS.str("file.save")
	fileDialog.selectedFile = baseDir.resolve("$baseScriptName.jadx.kts")
	fileDialog.fileExtList = listOf("jadx.kts")
	fileDialog.selectionMode = JFileChooser.FILES_ONLY
	val paths = fileDialog.show()
	if (paths.size != 1) {
		return
	}
	val scriptFile = paths[0]
	val newScriptContent = loadResourceAsString("files/new-script.jadx.kts")
	FileUtils.writeFile(scriptFile, newScriptContent)

	project.filePaths.add(scriptFile)
	project.save()
	mainWindow.reopen()
	focusScriptNodeAfterLoad(scriptFile, mainWindow)
}

private fun focusScriptNodeAfterLoad(scriptFile: Path, mainWindow: MainWindow) {
	val nodeName = scriptFile.fileName.toString().removeSuffix(".jadx.kts")
	mainWindow.addLoadListener { loaded ->
		if (loaded) {
			val scriptsRoot = mainWindow.treeRoot.followStaticPath("JInputs", "JInputScripts")
			scriptsRoot.searchNode { it.name == nodeName }?.let {
				mainWindow.selectNodeInTree(it)
				mainWindow.tabsController.codeJump(it)
			}
		}
		loaded // remove listener after load
	}
}

private fun loadResourceAsString(resourceName: String): String = JadxScriptKotlinPlugin::class.java.classLoader
	.getResourceAsStream(resourceName)
	?.use { it.bufferedReader(Charsets.UTF_8).readText() }
	?: throw JadxRuntimeException("Resource not found: $resourceName")
