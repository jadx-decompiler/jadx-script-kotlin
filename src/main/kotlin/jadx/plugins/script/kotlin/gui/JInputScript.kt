package jadx.plugins.script.kotlin.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import jadx.api.ICodeInfo
import jadx.api.impl.SimpleCodeInfo
import jadx.core.utils.exceptions.JadxRuntimeException
import jadx.core.utils.files.FileUtils
import jadx.gui.treemodel.JClass
import jadx.gui.treemodel.JEditableNode
import jadx.gui.ui.MainWindow
import jadx.gui.ui.panel.ContentPanel
import jadx.gui.ui.tab.TabbedPane
import jadx.gui.utils.NLS
import jadx.gui.utils.UiUtils
import jadx.gui.utils.ui.SimpleMenuItem
import jadx.plugins.script.kotlin.runtime.JadxScriptData
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JPopupMenu

private val log = KotlinLogging.logger {}

class JInputScript(
	val scriptData: JadxScriptData,
) : JEditableNode() {
	companion object {
		private val SCRIPT_ICON: ImageIcon = UiUtils.openSvgIcon("nodes/kotlin_script")
	}

	val scriptPath: Path = Path.of(scriptData.scriptFile.path)

	override fun hasContent(): Boolean = true

	override fun getContentPanel(tabbedPane: TabbedPane): ContentPanel = ScriptContentPanel(scriptData, tabbedPane, this)

	override fun getCodeInfo(): ICodeInfo {
		try {
			return SimpleCodeInfo(FileUtils.readFile(scriptPath))
		} catch (e: Exception) {
			throw JadxRuntimeException("Failed to read script file: " + scriptPath.toAbsolutePath(), e)
		}
	}

	override fun save(newContent: String?) {
		try {
			FileUtils.writeFile(scriptPath, newContent)
			log.debug { "Script saved: ${scriptPath.toAbsolutePath()}" }
		} catch (e: Exception) {
			throw JadxRuntimeException("Failed to write script file: " + scriptPath.toAbsolutePath(), e)
		}
	}

	override fun onTreePopupMenu(mainWindow: MainWindow): JPopupMenu {
		val menu = JPopupMenu()
		menu.add(SimpleMenuItem(NLS.str("popup.add_scripts")) { mainWindow.addFiles() })
		menu.add(SimpleMenuItem(NLS.str("popup.new_script")) { addNewInputScript(mainWindow) })
		menu.add(SimpleMenuItem(NLS.str("popup.remove")) { mainWindow.removeInput(scriptPath) })
		menu.add(SimpleMenuItem(NLS.str("popup.rename")) { mainWindow.renameInput(scriptPath) })
		return menu
	}

	override fun getSyntaxName(): String = SyntaxConstants.SYNTAX_STYLE_KOTLIN

	override fun getJParent(): JClass? = null

	override fun getIcon(): Icon = SCRIPT_ICON

	override fun getName(): String = scriptData.scriptName

	override fun makeString(): String = name

	override fun getTooltip(): String = scriptPath.normalize().toAbsolutePath().toString()
}
