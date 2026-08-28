import DeviceActivity
import ManagedSettings
import Foundation

// MARK: - Device Activity Monitor Extension
// Handles automated re-shielding and scheduled intervals on iOS

class DeviceActivityMonitorExtension: DeviceActivityMonitor {
    private let store = ManagedSettingsStore()
    
    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
        // Ensure shields are in place when schedule starts
        let selection = TimerStateManager.shared.loadSelection()
        TimerStateManager.shared.applyShields(from: selection)
    }
    
    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)
    }
    
    override func eventDidReachThreshold(
        _ event: DeviceActivityEvent.Name,
        activity: DeviceActivityName
    ) {
        super.eventDidReachThreshold(event, activity: activity)
        // When session time expires, re-shield
        let selection = TimerStateManager.shared.loadSelection()
        TimerStateManager.shared.applyShields(from: selection)
    }
}
