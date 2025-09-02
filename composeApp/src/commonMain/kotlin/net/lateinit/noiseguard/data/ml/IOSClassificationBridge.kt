package net.lateinit.noiseguard.data.ml

object IOSClassificationBridge {
    private var enabledFlag: Boolean = false
    private var listener: ((List<String>) -> Unit)? = null

    fun setEnabled(value: Boolean) { enabledFlag = value }
    fun isEnabled(): Boolean = enabledFlag

    fun setListener(l: (List<String>) -> Unit) { listener = l }
    fun clearListener() { listener = null }

    // Called from Swift to deliver labels
    fun publishLabels(labels: List<String>) {
        listener?.invoke(labels)
    }
}

// Convenience top-level functions for Swift bridging
fun setIOSClassificationEnabled(value: Boolean) = IOSClassificationBridge.setEnabled(value)
fun isIOSClassificationEnabled(): Boolean = IOSClassificationBridge.isEnabled()
fun setIOSClassificationListener(l: (List<String>) -> Unit) = IOSClassificationBridge.setListener(l)
fun clearIOSClassificationListener() = IOSClassificationBridge.clearListener()
fun publishIOSLabels(labels: List<String>) = IOSClassificationBridge.publishLabels(labels)
