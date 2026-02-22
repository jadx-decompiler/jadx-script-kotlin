package jadx.plugins.script.kotlin

import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import jadx.api.plugins.JadxPluginInfoBuilder
import jadx.plugins.script.kotlin.eval.ScriptEval
import jadx.plugins.script.kotlin.gui.registerJadxScriptInputCategory
import jadx.plugins.script.kotlin.gui.setupJadxScriptOptionsUI
import jadx.plugins.script.kotlin.passes.JadxScriptAfterLoadPass
import jadx.plugins.script.kotlin.runtime.JadxScriptPluginData
import jadx.plugins.script.kotlin.runtime.data.JadxScriptAllOptions

class JadxScriptKotlinPlugin : JadxPlugin {
	private var pluginData: JadxScriptPluginData? = null

	override fun getPluginInfo(): JadxPluginInfo = JadxPluginInfoBuilder
		.pluginId("jadx-script-kotlin")
		.name("Jadx Script (Kotlin)")
		.description("Scripting support for jadx using Kotlin")
		.homepage("https://github.com/jadx-decompiler/jadx-script-kotlin")
		.requiredJadxVersion("1.5.4, r2596")
		.provides("jadx-script") // conflict with bundled plugin from older jadx versions
		.build()

	override fun init(context: JadxPluginContext) {
		val scriptOptions = JadxScriptAllOptions()
		context.registerOptions(scriptOptions)
		pluginData = ScriptEval().process(context, scriptOptions)
		pluginData?.let {
			context.addPass(JadxScriptAfterLoadPass(it.scriptsData))
			context.guiContext?.let { guiContext ->
				setupJadxScriptOptionsUI(guiContext, scriptOptions)
				registerJadxScriptInputCategory(it, guiContext)
			}
		}
	}

	override fun unload() {
		pluginData?.close()
	}
}
