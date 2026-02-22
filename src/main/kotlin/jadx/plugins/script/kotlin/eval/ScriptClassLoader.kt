package jadx.plugins.script.kotlin.eval

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.io.File
import java.net.URLClassLoader

private val log = KotlinLogging.logger {}

class ScriptClassLoader(
	private val scriptName: String,
	private val parent: ClassLoader,
) : ClassLoader("script:$scriptName", parent),
	Closeable {

	val deps = sortedSetOf<File>()
	var depsClassLoader: URLClassLoader? = null

	fun addDeps(files: List<File>) {
		if (files.isEmpty()) return
		deps.addAll(files)
		log.debug { "add script dependency: $files" }
		close()
		val urls = deps.map { it.toURI().toURL() }.toTypedArray()
		depsClassLoader = URLClassLoader("deps:$scriptName", urls, parent)
	}

	override fun findClass(name: String?): Class<*>? {
		depsClassLoader?.let {
			try {
				val cls = it.loadClass(name)
				log.debug { "found class: $cls in deps" }
				return cls
			} catch (_: ClassNotFoundException) {
				// ignore
			}
		}
		log.debug { "can't find class: $name in deps" }
		throw ClassNotFoundException(name)
	}

	override fun loadClass(name: String?, resolve: Boolean): Class<*>? {
		val cls = super.loadClass(name, resolve)
		log.debug { "loaded class: $name" }
		return cls
	}

	override fun close() {
		depsClassLoader?.let {
			try {
				it.close()
			} catch (_: Exception) {
				// ignore
			}
		}
	}
}
