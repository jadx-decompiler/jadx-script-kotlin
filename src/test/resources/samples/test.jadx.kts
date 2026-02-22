import jadx.api.CommentsLevel
import jadx.core.dex.nodes.MethodNode
import jadx.plugins.script.kotlin.runtime.data.ScriptOrderedDecompilePass

val jadx = getJadxInstance()
jadx.args.commentsLevel = CommentsLevel.ERROR
jadx.args.isDeobfuscationOn = false
jadx.args.renameFlags = emptySet()

jadx.rename.all { name ->
	when (name) {
		"HelloWorld" -> "HelloJadx"
		else -> null
	}
}

jadx.addPass(object : ScriptOrderedDecompilePass(
	jadx,
	"TestPass",
	runAfter = listOf("FinishTypeInference"),
) {
	override fun visit(mth: MethodNode) {
		mth.addCodeComment("Method shortId: ${mth.methodInfo.shortId}")
	}
})

jadx.afterLoad {
	println("Loaded classes: ${jadx.classes.size}")
	jadx.classes.forEach {
		println("Class '${it.name}':\n${it.code}")
	}
}
