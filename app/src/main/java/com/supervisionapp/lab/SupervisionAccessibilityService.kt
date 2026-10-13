package com.supervisionapp.lab

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class SupervisionAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally unused in Phase 0 diagnostics mode.
    }

    override fun onInterrupt() {
        // Intentionally unused in Phase 0 diagnostics mode.
    }
}
