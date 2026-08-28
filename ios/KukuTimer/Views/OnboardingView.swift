import SwiftUI
import FamilyControls
import UserNotifications

public struct OnboardingView: View {
    public var onComplete: () -> Void
    
    @State private var showTutorial: Bool = true
    @State private var currentStep: Int = 1
    
    @State private var screenTimeGranted: Bool = false
    @State private var notificationGranted: Bool = false
    @State private var isPickerPresented: Bool = false
    @State private var selection = FamilyActivitySelection()
    
    @EnvironmentObject private var timerManager: TimerStateManager
    
    public var body: some View {
        if showTutorial {
            TutorialView(
                onFinish: { showTutorial = false },
                onSkip: { showTutorial = false }
            )
        } else {
            ZStack {
                Color.zenBeigeBackground.ignoresSafeArea()
                BonsaiWatermarkView(alpha: 0.08)
                
                VStack(spacing: 20) {
                    // Header
                    VStack(spacing: 4) {
                        Text("白百合 • 導き")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.zenShuIro)
                            .tracking(4)
                        
                        Text("Начало пути")
                            .font(.system(size: 24, weight: .light))
                            .foregroundColor(.zenInkPrimary)
                            .tracking(1)
                        
                        Text("Поэтапная настройка для осознанного использования")
                            .font(.system(size: 12))
                            .foregroundColor(.zenInkSecondary)
                            .multilineTextAlignment(.center)
                        
                        // Progress Step Indicators
                        HStack(spacing: 8) {
                            StepBadge(step: 1, current: currentStep, done: screenTimeGranted)
                            StepLine(done: screenTimeGranted)
                            StepBadge(step: 2, current: currentStep, done: notificationGranted)
                            StepLine(done: notificationGranted)
                            StepBadge(step: 3, current: currentStep, done: !selection.applicationTokens.isEmpty)
                        }
                        .padding(.top, 14)
                    }
                    .padding(.top, 16)
                    
                    // Step Card
                    VStack(spacing: 16) {
                        switch currentStep {
                        case 1:
                            Text("⏳")
                                .font(.system(size: 44))
                            Text("Этап 1: Доступ к Экранному Времени")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.zenInkPrimary)
                                .multilineTextAlignment(.center)
                            Text("Необходимо для того, чтобы iOS могла применить экран ожидания к выбранным приложениям.")
                                .font(.system(size: 13))
                                .foregroundColor(.zenInkSecondary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(3)
                            
                            Spacer()
                            
                            if !screenTimeGranted {
                                Button(action: requestScreenTime) {
                                    Text("Разрешить Экранное Время")
                                        .zenButtonStyle(bg: .zenShuIro)
                                }
                            } else {
                                SuccessBadge(text: "✓ Доступ предоставлен")
                            }
                            
                        case 2:
                            Text("🔔")
                                .font(.system(size: 44))
                            Text("Этап 2: Оповещение о готовности")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.zenInkPrimary)
                                .multilineTextAlignment(.center)
                            Text("Система уведомит вас ровно через 10 минут, когда «рис сварится», чтобы вы успели войти в приложение в 2-минутное окно.")
                                .font(.system(size: 13))
                                .foregroundColor(.zenInkSecondary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(3)
                            
                            Spacer()
                            
                            if !notificationGranted {
                                Button(action: requestNotifications) {
                                    Text("Включить уведомления")
                                        .zenButtonStyle(bg: .zenShuIro)
                                }
                            } else {
                                SuccessBadge(text: "✓ Уведомления включены")
                            }
                            
                        case 3:
                            Text("📱")
                                .font(.system(size: 44))
                            Text("Этап 3: Выбор приложений")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.zenInkPrimary)
                                .multilineTextAlignment(.center)
                            Text("Выберите приложения, для которых вы хотите включить 10-минутную осознанную паузу.")
                                .font(.system(size: 13))
                                .foregroundColor(.zenInkSecondary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(3)
                            
                            Spacer()
                            
                            Button(action: { isPickerPresented = true }) {
                                Text(selection.applicationTokens.isEmpty ? "Выбрать приложения..." : "Изменить выбор (\(selection.applicationTokens.count) прилож.)")
                                    .zenButtonStyle(bg: .zenKinGold)
                            }
                            .familyActivityPicker(isPresented: $isPickerPresented, selection: $selection)
                            .onChange(of: selection) { newSelection in
                                timerManager.saveSelection(newSelection)
                            }
                            
                        default:
                            EmptyView()
                        }
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.zenBeigeSurface)
                    .cornerRadius(22)
                    .overlay(
                        RoundedRectangle(cornerRadius: 22)
                            .stroke(Color.zenBeigeBorder, lineWidth: 1)
                    )
                    .padding(.horizontal, 20)
                    
                    // Bottom Navigation
                    VStack(spacing: 10) {
                        if currentStep < 3 {
                            Button(action: {
                                withAnimation { currentStep += 1 }
                            }) {
                                Text("Следующий этап →")
                                    .zenButtonStyle(bg: .zenInkPrimary)
                            }
                        } else {
                            Button(action: onComplete) {
                                Text("🍙 Начать использование Kuku Timer")
                                    .zenButtonStyle(bg: .zenShuIro)
                            }
                        }
                        
                        if currentStep > 1 {
                            Button(action: {
                                withAnimation { currentStep -= 1 }
                            }) {
                                Text("← Назад")
                                    .font(.system(size: 13))
                                    .foregroundColor(.zenInkSecondary)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 16)
                }
            }
            .onAppear {
                checkExistingPermissions()
            }
        }
    }
    
    private func checkExistingPermissions() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                self.notificationGranted = (settings.authorizationStatus == .authorized)
            }
        }
    }
    
    private func requestScreenTime() {
        Task {
            do {
                try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
                DispatchQueue.main.async {
                    self.screenTimeGranted = true
                    withAnimation { self.currentStep = 2 }
                }
            } catch {
                print("Screen Time auth error: \(error.localizedDescription)")
            }
        }
    }
    
    private func requestNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async {
                self.notificationGranted = granted
                if granted {
                    withAnimation { self.currentStep = 3 }
                }
            }
        }
    }
}

// MARK: - Step Helper Components

struct StepBadge: View {
    let step: Int
    let current: Int
    let done: Bool
    
    var body: some View {
        ZStack {
            Circle()
                .fill(done ? Color.zenMatsuGreen : (current == step ? Color.zenShuIro : Color.zenBeigeCard))
                .frame(width: 28, height: 28)
                .overlay(
                    Circle().stroke(current == step ? Color.zenShuIro : Color.zenBeigeBorder, lineWidth: 1)
                )
            
            Text(done ? "✓" : "\(step)")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(done || current == step ? .zenBeigeSurface : .zenInkTertiary)
        }
    }
}

struct StepLine: View {
    let done: Bool
    
    var body: some View {
        Rectangle()
            .fill(done ? Color.zenMatsuGreen : Color.zenBeigeBorder)
            .frame(width: 20, height: 2)
    }
}

struct SuccessBadge: View {
    let text: String
    
    var body: some View {
        Text(text)
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(.zenMatsuGreen)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.zenMatsuGreenLight)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.zenMatsuGreen.opacity(0.4), lineWidth: 1)
            )
    }
}
