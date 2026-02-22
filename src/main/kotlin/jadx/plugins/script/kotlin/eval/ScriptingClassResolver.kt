package jadx.plugins.script.kotlin.eval

import kotlin.reflect.KClass
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.GetScriptingClassByClassLoader

class ScriptingClassResolver(private val scriptClassLoader: ScriptClassLoader) : GetScriptingClassByClassLoader {
	private fun loadCls(classType: KotlinType): KClass<*> = scriptClassLoader.loadClass(classType.typeName).kotlin

	override fun invoke(
		classType: KotlinType,
		contextClass: KClass<*>,
		hostConfiguration: ScriptingHostConfiguration,
	): KClass<*> = loadCls(classType)

	override fun invoke(
		classType: KotlinType,
		contextClassLoader: ClassLoader?,
		hostConfiguration: ScriptingHostConfiguration,
	): KClass<*> = loadCls(classType)
}
