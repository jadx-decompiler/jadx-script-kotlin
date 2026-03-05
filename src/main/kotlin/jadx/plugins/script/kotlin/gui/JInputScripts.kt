package jadx.plugins.script.kotlin.gui

import jadx.gui.treemodel.JClass
import jadx.gui.treemodel.JNode
import jadx.gui.ui.MainWindow
import jadx.gui.utils.NLS
import jadx.gui.utils.UiUtils
import jadx.gui.utils.ui.SimpleMenuItem
import jadx.plugins.script.kotlin.runtime.JadxScriptPluginData
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JPopupMenu

private val INPUT_SCRIPTS_ICON: ImageIcon = UiUtils.openSvgIcon("nodes/scriptsModel")

class JInputScripts(pluginData: JadxScriptPluginData) : JNode() {

	init {
		pluginData.scriptsData.forEach { add(JInputScript(it)) }
	}

	override fun onTreePopupMenu(mainWindow: MainWindow) = JPopupMenu().apply {
		add(SimpleMenuItem(NLS.str("popup.add_scripts")) { mainWindow.addFiles() })
		add(SimpleMenuItem(NLS.str("popup.new_script")) { addNewInputScript(mainWindow) })
	}

	override fun getJParent(): JClass? = null

	override fun getIcon(): Icon = INPUT_SCRIPTS_ICON

	override fun getID(): String = "JInputScripts"

	override fun makeString(): String = NLS.str("tree.input_scripts")
}
