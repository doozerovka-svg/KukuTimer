import Foundation
import ManagedSettings
import UserNotifications
import FamilyControls

public enum TimerStatus: String, Codable, CaseIterable {
    case idle       // Apps are shielded, waiting for user to attempt access
    case cooking    // 10-minute mindful pause is active ("Rice is boiling")
    case ready      // 2-minute window of opportunity is open ("Rice is cooked")
    case expired    // Missed the 2-minute window, reset to idle
}

public final class TimerStateManager: ObservableObject {
    public static let shared = TimerStateManager()
    public static let appGroupID = "group.com.doozerovka.kukutimer"
    public static let notificationCategoryIdentifier = "KUKU_TIMER_WINDOW"
    
    private let defaults: UserDefaults?
    private let store = ManagedSettingsStore()
    
    // Keys
    private let statusKey = "kuku_timer_status"
    private let cookStartTimeKey = "kuku_cook_start_time"
    private let targetAppTokenKey = "kuku_target_app_token"
    private let selectedAppsKey = "kuku_selected_apps_selection"
    private let cookDurationSecondsKey = "kuku_cook_duration_seconds"
    private let windowDurationSecondsKey = "kuku_window_duration_seconds"
    
    // Defaults (in seconds)
    public let defaultCookDuration: TimeInterval = 600   // 10 minutes
    public let defaultWindowDuration: TimeInterval = 120 // 2 minutes
    
    public init() {
        self.defaults = UserDefaults(suiteName: TimerStateManager.appGroupID)
    }
    
    // MARK: - Properties
    
    public var cookDuration: TimeInterval {
        get {
            let val = defaults?.double(forKey: cookDurationSecondsKey) ?? 0
            return val > 0 ? val : defaultCookDuration
        }
        set {
            defaults?.set(newValue, forKey: cookDurationSecondsKey)
        }
    }
    
    public var windowDuration: TimeInterval {
        get {
            let val = defaults?.double(forKey: windowDurationSecondsKey) ?? 0
            return val > 0 ? val : defaultWindowDuration
        }
        set {
            defaults?.set(newValue, forKey: windowDurationSecondsKey)
        }
    }
    
    public var cookStartTime: Date? {
        get { defaults?.object(forKey: cookStartTimeKey) as? Date }
        set { defaults?.set(newValue, forKey: cookStartTimeKey) }
    }
    
    public var status: TimerStatus {
        get {
            guard let rawValue = defaults?.string(forKey: statusKey),
                  let state = TimerStatus(rawValue: rawValue) else {
                return .idle
            }
            
            guard let startTime = cookStartTime else {
                return .idle
            }
            
            let elapsed = Date().timeIntervalSince(startTime)
            let totalCooking = cookDuration
            let totalWindow = totalCooking + windowDuration
            
            if elapsed < totalCooking {
                return .cooking
            } else if elapsed < totalWindow {
                return .ready
            } else {
                return .idle // Expired -> reset to idle
            }
        }
        set {
            defaults?.set(newValue.rawValue, forKey: statusKey)
        }
    }
    
    // MARK: - Time Calculations
    
    /// Seconds remaining until rice is cooked (during .cooking state)
    public var remainingCookSeconds: TimeInterval {
        guard let startTime = cookStartTime else { return 0 }
        let elapsed = Date().timeIntervalSince(startTime)
        let remaining = cookDuration - elapsed
        return max(0, remaining)
    }
    
    /// Seconds remaining in the 2-minute entry window (during .ready state)
    public var remainingWindowSeconds: TimeInterval {
        guard let startTime = cookStartTime else { return 0 }
        let elapsed = Date().timeIntervalSince(startTime)
        let windowEnd = cookDuration + windowDuration
        let remaining = windowEnd - elapsed
        return max(0, remaining)
    }
    
    // MARK: - Timer Actions
    
    /// Starts the 10-minute cooking cycle
    public func startCooking() {
        cookStartTime = Date()
        status = .cooking
        scheduleReadyNotification()
    }
    
    /// Resets state back to idle
    public func reset() {
        cookStartTime = nil
        status = .idle
        cancelNotifications()
    }
    
    /// Grants access: Temporarily unshields apps for the user session
    public func grantAccess() {
        store.shield.applications = nil
        store.shield.applicationCategories = nil
    }
    
    /// Applies restrictions to the specified selection of apps and categories
    public func applyShields(from selection: FamilyActivitySelection) {
        store.shield.applications = selection.applicationTokens.isEmpty ? nil : selection.applicationTokens
        store.shield.applicationCategories = selection.categoryTokens.isEmpty ? nil : ShieldSettings.ActivityCategoryPolicy.specific(selection.categoryTokens)
    }
    
    /// Saves the selected apps selection to App Group
    public func saveSelection(_ selection: FamilyActivitySelection) {
        if let encoded = try? PropertyListEncoder().encode(selection) {
            defaults?.set(encoded, forKey: selectedAppsKey)
        }
        applyShields(from: selection)
    }
    
    /// Loads the stored selection of apps
    public func loadSelection() -> FamilyActivitySelection {
        guard let data = defaults?.data(forKey: selectedAppsKey),
              let selection = try? PropertyListDecoder().decode(FamilyActivitySelection.self, from: data) else {
            return FamilyActivitySelection()
        }
        return selection
    }
    
    // MARK: - Notification Management
    
    public func scheduleReadyNotification() {
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()
        
        let content = UNMutableNotificationContent()
        content.title = "🍚 Котел закипел. Рис готов!"
        content.subtitle = "Kuku Timer • 一炊の夢"
        content.body = "Врата открыты! У вас есть \(Int(windowDuration / 60)) минуты для осознанного входа."
        content.sound = .default
        content.badge = 1
        content.categoryIdentifier = TimerStateManager.notificationCategoryIdentifier
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: cookDuration, repeats: false)
        let request = UNNotificationRequest(
            identifier: "kuku_rice_ready_notification",
            content: content,
            trigger: trigger
        )
        
        center.add(request) { error in
            if let error = error {
                print("[KukuTimer] Failed to schedule notification: \(error.localizedDescription)")
            }
        }
    }
    
    public func cancelNotifications() {
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }
}
