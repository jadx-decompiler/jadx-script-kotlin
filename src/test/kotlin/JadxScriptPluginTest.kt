package jadx.plugins.script

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.github.oshai.kotlinlogging.KotlinLogging
import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import java.io.File
import java.util.function.Consumer
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val log = KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JadxScriptPluginTest {

	@BeforeAll
	fun disableCache() {
		System.setProperty("JADX_SCRIPT_CACHE_ENABLE", "false")
	}

	@AfterAll
	fun clear() {
		System.clearProperty("JADX_SCRIPT_CACHE_ENABLE")
	}

	@Test
	fun integrationTest() {
		val args = JadxArgs()
		args.inputFiles.run {
			add(getSampleFile("hello.smali"))
			add(getSampleFile("test.jadx.kts"))
			add(getSampleFile("test-deps.jadx.kts"))
		}
		val elapsed = measureTimeMillis {
			JadxDecompiler(args).use { jadx ->
				jadx.load()
				assertThat(jadx.classes)
					.hasSize(1)
					.allMatch { it.name == "HelloJadx" }
					.allMatch { it.code.contains("// Method shortId: ") }
			}
		}
		println("Elapsed time: ${elapsed.toDuration(DurationUnit.MILLISECONDS)}")
	}

	@Test
	fun examplesTest() {
		val args = JadxArgs()
		args.inputFiles.run {
			add(getSampleFile("hello.smali"))
			addAll(collectExampleScripts())
		}
		validateLogs { event ->
			if (event.level.toInt() == Level.ERROR_INT) {
				fail(event.message)
			}
		}
		val elapsed = measureTimeMillis {
			JadxDecompiler(args).use { jadx ->
				jadx.load()
				assertThat(jadx.classes).hasSize(1)
			}
		}
		println("Elapsed time: ${elapsed.toDuration(DurationUnit.MILLISECONDS)}")
	}

	private fun getSampleFile(file: String) = File(
		javaClass.classLoader.getResource("samples/$file")?.file
			?: fail { "Failed to load sample: $file" },
	)

	private fun collectExampleScripts() = File("examples")
		.walkTopDown()
		.filter { it.isFile && it.extension == "kts" }
		.onEachIndexed { i, f -> log.info { "adding example script ($i): $f" } }
		.toList()

	private fun validateLogs(validate: Consumer<ILoggingEvent>) {
		val appender = object : AppenderBase<ILoggingEvent>() {
			override fun append(event: ILoggingEvent) {
				validate.accept(event)
			}
		}
		appender.start()
		val rootLogger = LoggerFactory.getLogger("ROOT") as Logger
		rootLogger.addAppender(appender)
	}
}
