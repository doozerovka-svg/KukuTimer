import SwiftUI
import FamilyControls
import UserNotifications

@main
struct KukuTimerApp: App {
    @StateObject private var timerManager = TimerStateManager.shared
    @AppStorage("has_completed_onboarding") private var hasCompletedOnboarding: Bool = false
    
    init() {
        requestAuthorizations()
    }
    
    var body: some Scene {
        WindowGroup {
            if !hasCompletedOnboarding {
                OnboardingView(onComplete: {
                    hasCompletedOnboarding = true
                })
                .environmentObject(timerManager)
            } else {
                ContentView()
                    .environmentObject(timerManager)
                    .onOpenURL { url in
                        handleDeepLink(url)
                    }
            }
        }
    }
    
    private func requestAuthorizations() {
        // Request Screen Time authorization
        Task {
            do {
                try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
            } catch {
                print("[KukuTimer] FamilyControls auth failed: \(error.localizedDescription)")
            }
        }
        
        // Request Notifications authorization
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                print("[KukuTimer] Notification auth failed: \(error.localizedDescription)")
            }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        if url.scheme == "kukutimer" {
            // Check status and unlock if in 2-minute window
            if timerManager.status == .ready {
                timerManager.grantAccess()
            }
        }
    }
}
