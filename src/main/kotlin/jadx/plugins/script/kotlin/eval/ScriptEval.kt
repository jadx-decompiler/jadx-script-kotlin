package jadx.plugins.script.kotlin.eval

import jadx.api.plugins.JadxPluginContext
import jadx.plugins.script.kotlin.JadxScriptKotlinPlugin
import jadx.plugins.script.kotlin.runtime.JadxScriptData
import jadx.plugins.script.kotlin.runtime.JadxScriptPluginData
import jadx.plugins.script.kotlin.runtime.data.JadxScriptAllOptions
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ScriptEval {
	fun process(context: JadxPluginContext, scriptOptions: JadxScriptAllOptions): JadxScriptPluginData? {
		val jadx = context.decompiler
		val scripts = jadx.args.inputFiles.filter { f -> f.name.endsWith(".jadx.kts") }
		if (scripts.isEmpty()) {
			return null
		}
		val pluginClassLoader = JadxScriptKotlinPlugin::class.java.classLoader
		val scriptsData = mutableListOf<JadxScriptData>()
		for (scriptFile in scripts) {
			val scriptData = JadxScriptData(jadx, context, scriptOptions, scriptFile, baseClassLoader = pluginClassLoader)
			scriptsData += scriptData
			eval(scriptData)
		}
		return JadxScriptPluginData(scriptsData)
	}

	private fun eval(scriptData: JadxScriptData) {
		scriptData.log.debug { "Loading script: ${scriptData.scriptFile.absolutePath}" }
		val hostConf = buildHostConf(scriptData)
		val compileConf = buildCompileConf(scriptData, hostConf)
		val evalConf = buildEvalConf(scriptData, compileConf)
		val scriptingHost = BasicJvmScriptingHost(hostConf)
		val execTime = measureTimeMillis {
			val result = scriptingHost.eval(scriptData.scriptFile.toScriptSource(), compileConf, evalConf)
			processEvalResult(result, scriptData)
		}
		scriptData.log.debug { "Script '${scriptData.scriptName}' executed in ${execTime.toDuration(DurationUnit.MILLISECONDS)}" }
	}

	private fun processEvalResult(res: ResultWithDiagnostics<EvaluationResult>, scriptData: JadxScriptData) {
		val log = scriptData.log
		for (r in res.reports) {
			val msg = r.render(withSeverity = false)
			when (r.severity) {
				ScriptDiagnostic.Severity.FATAL, ScriptDiagnostic.Severity.ERROR -> log.error(r.exception) { "Script execution error: $msg" }
				ScriptDiagnostic.Severity.WARNING -> log.warn { "Script execution issue: $msg" }
				ScriptDiagnostic.Severity.INFO -> log.info { "Script report: $msg" }
				ScriptDiagnostic.Severity.DEBUG -> log.debug { "Script debug: $msg" }
			}
		}
		when (res) {
			is ResultWithDiagnostics.Success -> {
				when (val retVal = res.value.returnValue) {
					is ResultValue.Error -> log.error(retVal.error) { "Script execution error:" }
					is ResultValue.Value -> log.info { "Script execution result: $retVal" }
					is ResultValue.Unit -> {}
					ResultValue.NotEvaluated -> {}
				}
			}

			is ResultWithDiagnostics.Failure -> {
				scriptData.error = true
				log.error { "Script execution failed: ${scriptData.scriptName}" }
			}
		}
	}
}
