package dev.cachly.brain

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.util.Disposer
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

class CachlyStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "CachlyBrainWidget"
    override fun getDisplayName(): String = "Cachly Brain"
    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = CachlyStatusBarWidget(project)
}

class CachlyStatusBarWidget(private val project: Project) :
    StatusBarWidget,
    StatusBarWidget.TextPresentation {

    private var statusBar: StatusBar? = null
    private var currentText = "🧠 Brain: ..."
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var scheduledTask: ScheduledFuture<*>? = null

    override fun ID(): String = "CachlyBrainWidget"
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    override fun getTooltipText(): String = "Cachly Brain Health — click for details"
    override fun getText(): String = currentText
    override fun getAlignment(): Float = java.awt.Component.CENTER_ALIGNMENT

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        ShowBrainHealthAction().showPanel(project)
    }

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        // Trigger a recall on first install — tracks IDE session as a recall
        // Also re-triggers every hour for long-running IDE sessions
        scheduler.execute { CachlyApiClient.triggerRecall() }
        scheduler.scheduleAtFixedRate({ CachlyApiClient.triggerRecall() }, 1, 1, TimeUnit.HOURS)
        val interval = CachlySettings.getInstance().state.refreshIntervalSec.toLong()
        scheduledTask = scheduler.scheduleAtFixedRate({ refresh() }, 2, interval, TimeUnit.SECONDS)
    }

    private fun refresh() {
        try {
            val health = CachlyApiClient.fetchHealth()
            val text = when (health.status) {
                "not_configured" -> "🧠 Cachly: not configured"
                "unreachable" -> "🧠 Brain: offline"
                else -> "🧠 Brain: ${health.lessons} lessons"
            }
            SwingUtilities.invokeLater {
                currentText = text
                statusBar?.updateWidget(ID())
            }
        } catch (_: Exception) {
            SwingUtilities.invokeLater {
                currentText = "🧠 Brain: error"
                statusBar?.updateWidget(ID())
            }
        }
    }

    override fun dispose() {
        scheduledTask?.cancel(false)
        scheduler.shutdown()
    }
}
