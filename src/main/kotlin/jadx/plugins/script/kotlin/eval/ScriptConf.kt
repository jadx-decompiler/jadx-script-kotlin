package jadx.plugins.script.kotlin.eval

import jadx.plugins.script.kotlin.runtime.JadxScriptData
import jadx.plugins.script.kotlin.runtime.JadxScriptTemplate
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.compilationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.defaultIdentifier
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.displayName
import kotlin.script.experimental.api.fileExtension
import kotlin.script.experimental.api.filePathPattern
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.isStandalone
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.JvmGetScriptingClass
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

/**
 * Compiler configuration defined in template annotation and might be used by IntelliJ IDEA for script parsing
 */
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object DefCompileConf : ScriptCompilationConfiguration(buildCompileConf(null, buildHostConf(null)))

internal fun buildHostConf(scriptData: JadxScriptData?) = ScriptingHostConfiguration {
	jvm {
		scriptData?.let {
			baseClassLoader(scriptData.scriptClassLoader)
			getScriptingClass(ScriptingClassResolver(scriptData.scriptClassLoader))
			if (scriptData.enableCache) {
				compilationCache(ScriptCache().build(scriptData.pluginContext))
			}
		} ?: run {
			getScriptingClass(JvmGetScriptingClass())
		}
	}
}

internal fun buildCompileConf(scriptData: JadxScriptData?, scriptingHostConf: ScriptingHostConfiguration) = ScriptCompilationConfiguration {
	hostConfiguration.put(scriptingHostConf)

	displayName.put("Jadx script")
	defaultIdentifier.put("JadxScript")

	fileExtension.put("jadx.kts")
	filePathPattern.put(".*\\.jadx\\.kts")

	implicitReceivers.put(listOf(KotlinType(JadxScriptTemplate::class)))

	jvm {
		dependenciesFromClassloader(
			classLoader = scriptData?.scriptClassLoader ?: Thread.currentThread().contextClassLoader,
			wholeClasspath = true,
		)
	}

	val defImports = listOf(
		JadxScriptTemplate::class,
		DependsOn::class,
		Repository::class,
	)
	defaultImports.put(defImports.map { it.qualifiedName!! })

	refineConfiguration {
		onAnnotations(DependsOn::class, Repository::class, handler = ScriptDependenciesResolver(scriptData))
	}

	ide {
		acceptedLocations(ScriptAcceptedLocation.Everywhere)
	}

	isStandalone(true)
}

internal fun buildEvalConf(
	scriptData: JadxScriptData,
	compileConf: ScriptCompilationConfiguration,
): ScriptEvaluationConfiguration = ScriptEvaluationConfiguration {
	hostConfiguration.put(compileConf[hostConfiguration]!!)
	compilationConfiguration.put(compileConf)
	constructorArgs(JadxScriptTemplate(scriptData))
}
