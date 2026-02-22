@file:JvmName("ScriptRuntime")
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package jadx.plugins.script.kotlin.runtime

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import jadx.api.JavaClass
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.events.IJadxEvents
import jadx.api.plugins.pass.JadxPass
import jadx.plugins.script.kotlin.eval.DefCompileConf
import jadx.plugins.script.kotlin.eval.ScriptClassLoader
import jadx.plugins.script.kotlin.runtime.data.Debug
import jadx.plugins.script.kotlin.runtime.data.Decompile
import jadx.plugins.script.kotlin.runtime.data.Gui
import jadx.plugins.script.kotlin.runtime.data.JadxScriptAllOptions
import jadx.plugins.script.kotlin.runtime.data.JadxScriptOptions
import jadx.plugins.script.kotlin.runtime.data.Rename
import jadx.plugins.script.kotlin.runtime.data.Replace
import jadx.plugins.script.kotlin.runtime.data.Search
import jadx.plugins.script.kotlin.runtime.data.Stages
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.Closeable
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
	displayName = "Jadx Script",
	fileExtension = "jadx.kts",
	filePathPattern = ".*\\.jadx\\.kts",
	compilationConfiguration = DefCompileConf::class,
)
open class JadxScriptTemplate(
	scriptData: JadxScriptData,
) {
	val scriptName = scriptData.scriptName
	val log = scriptData.log

	private val scriptInstance = JadxScriptInstance(scriptData, log)

	fun getJadxInstance() = scriptInstance

	fun println(message: Any?) {
		log.info { message }
	}

	fun print(message: Any?) {
		log.info { message }
	}
}

class JadxScriptPluginData(val scriptsData: List<JadxScriptData>) : Closeable {
	val map = scriptsData.groupBy { scriptData -> scriptData.scriptFile.name }

	fun getByScriptFileName(scriptFileName: String) = map[scriptFileName]

	override fun close() {
		scriptsData.forEach {
			try {
				it.close()
			} catch (_: Exception) {
				// ignore
			}
		}
	}
}

class JadxScriptData(
	val jadxInstance: JadxDecompiler,
	val pluginContext: JadxPluginContext,
	val options: JadxScriptAllOptions,
	val scriptFile: File,
	val enableCache: Boolean = true,
	baseClassLoader: ClassLoader = Thread.currentThread().contextClassLoader,
) : Closeable {
	companion object {
		const val JADX_SCRIPT_LOG_PREFIX = "JadxScript:"
	}

	val scriptName = scriptFile.name.removeSuffix(".jadx.kts")
	val log = KotlinLogging.logger("$JADX_SCRIPT_LOG_PREFIX$scriptName")
	val scriptClassLoader = ScriptClassLoader(scriptName, baseClassLoader)
	val afterLoad = mutableListOf<() -> Unit>()
	var error: Boolean = false

	override fun close() {
		scriptClassLoader.close()
	}
}

class JadxScriptInstance(
	private val scriptData: JadxScriptData,
	val log: KLogger,
) {
	private val decompiler = scriptData.jadxInstance

	val options: JadxScriptOptions by lazy { JadxScriptOptions(this, scriptData.options) }
	val rename: Rename by lazy { Rename(this) }
	val stages: Stages by lazy { Stages(this) }
	val replace: Replace by lazy { Replace(this) }
	val decompile: Decompile by lazy { Decompile(this) }
	val search: Search by lazy { Search(this) }
	val gui: Gui by lazy { Gui(this, scriptData.pluginContext.guiContext) }
	val debug: Debug by lazy { Debug(this) }

	val events: IJadxEvents
		get() = scriptData.pluginContext.events()

	val args: JadxArgs
		get() = decompiler.args

	val classes: List<JavaClass>
		get() = decompiler.classes

	val scriptFile get() = scriptData.scriptFile

	val scriptName get() = scriptData.scriptName

	fun afterLoad(block: () -> Unit) {
		scriptData.afterLoad.add(block)
	}

	fun addPass(pass: JadxPass) {
		scriptData.pluginContext.addPass(pass)
	}

	val internalDecompiler: JadxDecompiler
		@Internal get() = decompiler
}
