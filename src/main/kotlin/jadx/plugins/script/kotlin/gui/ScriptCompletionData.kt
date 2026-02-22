package jadx.plugins.script.kotlin.gui

import org.fife.ui.autocomplete.Completion
import org.fife.ui.autocomplete.CompletionProvider
import javax.swing.Icon
import javax.swing.text.JTextComponent

class ScriptCompletionData(
	private val provider: CompletionProvider,
	private val relevance: Int,
	private val input: String,
	private val code: String,
	private val replacePos: Int,
	private val icon: Icon,
	private val toolTip: String,
	private val summary: String,
) : Completion {

	override fun getInputText(): String = input

	override fun getProvider(): CompletionProvider = provider

	override fun getAlreadyEntered(comp: JTextComponent?): String? = provider.getAlreadyEnteredText(comp)

	override fun getRelevance(): Int = relevance

	override fun getReplacementText(): String = code.substring(0, replacePos) + input

	override fun getIcon(): Icon = icon

	override fun getSummary(): String = summary

	override fun getToolTipText(): String = toolTip

	override fun compareTo(other: Completion): Int = relevance.compareTo(other.relevance)

	override fun toString(): String = input
}
