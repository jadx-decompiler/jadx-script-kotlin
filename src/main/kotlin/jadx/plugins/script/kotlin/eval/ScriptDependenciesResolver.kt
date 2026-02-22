package jadx.plugins.script.kotlin.eval

import jadx.plugins.script.kotlin.runtime.JadxScriptData
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.collectedAnnotations
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.api.with
import kotlin.script.experimental.dependencies.CompoundDependenciesResolver
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.dependencies.resolveFromScriptSourceAnnotations
import kotlin.script.experimental.jvm.updateClasspath

class ScriptDependenciesResolver(private val scriptData: JadxScriptData?) : RefineScriptCompilationConfigurationHandler {
	private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

	override fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
		val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)
			?.takeIf { it.isNotEmpty() }
			?: return context.compilationConfiguration.asSuccess()
		return runBlocking {
			resolver.resolveFromScriptSourceAnnotations(annotations)
		}.onSuccess { files: List<File> ->
			scriptData?.scriptClassLoader?.addDeps(files)
			context.compilationConfiguration.with {
				updateClasspath(files)
			}.asSuccess()
		}
	}
}
