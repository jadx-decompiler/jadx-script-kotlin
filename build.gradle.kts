import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale

plugins {
	id("java-library")

	kotlin("jvm") version "2.3.20"

	id("se.patrikerdes.use-latest-versions") version "0.2.19"
	id("com.github.ben-manes.versions") version "0.53.0"
	id("com.diffplug.spotless") version "8.4.0"
}

group = "io.github.jadx-decompiler"
version = System.getenv("JADX_SCRIPT_KOTLIN_PLUGIN_VERSION") ?: "dev"

dependencies {
	val jadxVersion = "1.5.5"
	val isJadxSnapshot = jadxVersion.endsWith("-SNAPSHOT")
	compileOnly("io.github.skylot:jadx-core:$jadxVersion") { isChanging = isJadxSnapshot }
	compileOnly("io.github.skylot:jadx-gui:$jadxVersion") { isChanging = isJadxSnapshot }

	implementation(kotlin("scripting-common"))
	implementation(kotlin("scripting-jvm"))
	implementation(kotlin("scripting-jvm-host"))
	implementation(kotlin("scripting-ide-services"))
	implementation(kotlin("scripting-compiler-embeddable"))
	implementation(kotlin("compiler-embeddable"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

	// allow to use maven dependencies in scripts
	implementation(kotlin("scripting-dependencies"))
	implementation(kotlin("scripting-dependencies-maven"))

	// autocomplete support in editor
	compileOnly("com.fifesoft:autocomplete:3.3.2")
	compileOnly("com.fifesoft:rsyntaxtextarea:3.6.0")

	// use KtLint for format and check jadx scripts
	implementation("com.pinterest.ktlint:ktlint-rule-engine:1.8.0")
	implementation("com.pinterest.ktlint:ktlint-ruleset-standard:1.8.0")

	compileOnly("io.github.oshai:kotlin-logging-jvm:7.0.13")
	compileOnly("org.slf4j:slf4j-api:2.0.17")

	// register jadx script for IDE support
	kotlinScriptDef(project)

	testImplementation("io.github.skylot:jadx-dex-input:$jadxVersion") {
		isChanging = isJadxSnapshot
	}
	testImplementation("io.github.skylot:jadx-smali-input:$jadxVersion") {
		isChanging = isJadxSnapshot
	}

	testImplementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
	testImplementation("ch.qos.logback:logback-classic:1.5.22")
	testImplementation("org.assertj:assertj-core:3.27.6")
	testImplementation("io.mockk:mockk:1.14.9")

	testImplementation("org.junit.jupiter:junit-jupiter:5.13.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

repositories {
	mavenCentral()
	maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
	google()
}

kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(11))
	}
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)
	}
}

tasks {
	register<Zip>("dist") {
		group = "jadx-plugin"
		dependsOn(jar)

		from(jar)
		from(project.configurations.runtimeClasspath)

		archiveBaseName = project.name
		destinationDirectory = layout.buildDirectory.dir("dist")
	}

	withType(Test::class) {
		useJUnitPlatform()
	}
}

configure<SpotlessExtension> {
	kotlin {
		ktlint().editorConfigOverride(mapOf("indent_style" to "tab"))
		commonFormatOptions()
	}
	kotlinGradle {
		ktlint()
		commonFormatOptions()
	}
	format("misc") {
		target("**/*.gradle", "**/*.xml", "**/.gitignore", "**/.properties")
		targetExclude(".gradle/**", ".idea/**", "*/build/**")
		commonFormatOptions()
	}
}

fun FormatExtension.commonFormatOptions() {
	lineEndings = LineEnding.UNIX
	encoding = Charsets.UTF_8
	trimTrailingWhitespace()
	endWithNewline()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
	rejectVersionIf {
		// disallow release candidates as upgradable versions from stable versions
		isNonStable(candidate.version) && !isNonStable(currentVersion)
	}
}

fun isNonStable(version: String): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
	val regex = "^[0-9,.v-]+(-r)?$".toRegex()
	val isStable = stableKeyword || regex.matches(version)
	return isStable.not()
}
