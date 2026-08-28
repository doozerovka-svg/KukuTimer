import ManagedSettings
import UserNotifications
import Foundation

// MARK: - Shield Action Extension
// Intercepts button taps directly on the system Screen Time Shield

class ShieldActionExtension: ShieldActionDelegate {
    
    override func handle(
        action: ShieldAction,
        for application: ApplicationToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handleAction(action: action, token: application, completionHandler: completionHandler)
    }
    
    override func handle(
        action: ShieldAction,
        for category: ActivityCategoryToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handleCategoryAction(action: action, completionHandler: completionHandler)
    }
    
    override func handle(
        action: ShieldAction,
        for webDomain: WebDomainToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        completionHandler(.close)
    }
    
    // MARK: - Core Action Handler
    
    private func handleAction(
        action: ShieldAction,
        token: ApplicationToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        let timerManager = TimerStateManager.shared
        
        switch action {
        case .primaryButtonPressed:
            let currentStatus = timerManager.status
            
            switch currentStatus {
            case .idle, .expired:
                // Start 10-minute mindful pause & schedule exact notification
                timerManager.startCooking()
                completionHandler(.none) // Keep the shield active to prevent access
                
            case .cooking:
                // Still waiting for rice to cook
                completionHandler(.none)
                
            case .ready:
                // 2-minute window is active: Unlock target app!
                let store = ManagedSettingsStore()
                store.shield.applications?.remove(token)
                completionHandler(.defer) // Allow the app to open
            }
            
        case .secondaryButtonPressed:
            // "Отложить телефон" / Close action
            completionHandler(.close)
            
        @unknown default:
            completionHandler(.close)
        }
    }
    
    private func handleCategoryAction(
        action: ShieldAction,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        switch action {
        case .primaryButtonPressed:
            let timerManager = TimerStateManager.shared
            if timerManager.status == .idle || timerManager.status == .expired {
                timerManager.startCooking()
            }
            completionHandler(.none)
        case .secondaryButtonPressed:
            completionHandler(.close)
        @unknown default:
            completionHandler(.close)
        }
    }
}
