package jadx.plugins.script.kotlin.gui

import jadx.api.plugins.gui.JadxGuiContext
import jadx.gui.plugins.context.GuiPluginContext
import jadx.gui.plugins.context.ITreeInputCategory
import jadx.gui.settings.data.ITabStatePersist
import jadx.gui.treemodel.JNode
import jadx.plugins.script.kotlin.runtime.JadxScriptPluginData
import java.nio.file.Path

internal fun registerInputCategory(pluginData: JadxScriptPluginData, guiContext: JadxGuiContext) {
	val internalContext = guiContext as GuiPluginContext
	val inputCategory = InputScriptsBuilder(pluginData)
	internalContext.registerTreeInputCategory(inputCategory)
	internalContext.registerTabStatePersistAdapter(InputScriptTabStatePersist(inputCategory))
}

class InputScriptsBuilder(private val pluginData: JadxScriptPluginData) : ITreeInputCategory {
	var scriptsRootNode: JInputScripts? = null

	override fun filesFilter(file: Path): Boolean = pluginData.getByScriptFileName(file.fileName.toString()) != null

	override fun buildInputNode(files: List<Path>): JNode = JInputScripts(pluginData).also { scriptsRootNode = it }
}

class InputScriptTabStatePersist(private val scriptsBuilder: InputScriptsBuilder) : ITabStatePersist {
	override fun getNodeClass() = JInputScript::class.java

	override fun save(node: JNode): String = node.name

	override fun load(nodeName: String): JNode? = scriptsBuilder.scriptsRootNode?.searchNode { it.name.equals(nodeName) }
}
