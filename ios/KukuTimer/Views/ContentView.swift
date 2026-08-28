import SwiftUI
import FamilyControls
import ManagedSettings

public struct ContentView: View {
    @EnvironmentObject private var timerManager: TimerStateManager
    
    @State private var isPickerPresented: Bool = false
    @State private var selection = FamilyActivitySelection()
    @State private var showTutorialModal: Bool = false
    @State private var showTimerModal: Bool = false
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.zenBeigeBackground.ignoresSafeArea()
                BonsaiWatermarkView(alpha: 0.07)
                
                ScrollView {
                    VStack(spacing: 20) {
                        // Header
                        VStack(spacing: 4) {
                            Text("ククターマー")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(.zenShuIro)
                                .tracking(4)
                            
                            Text("Kuku Timer")
                                .font(.system(size: 28, weight: .light))
                                .foregroundColor(.zenInkPrimary)
                            
                            Text("Цифровой детокс через осознанное ожидание")
                                .font(.system(size: 12))
                                .foregroundColor(.zenInkSecondary)
                        }
                        .padding(.top, 10)
                        
                        // Status Card
                        StatusCard(
                            status: timerManager.status,
                            onTapTimer: { showTimerModal = true },
                            onStartTestCooking: {
                                timerManager.startCooking()
                                showTimerModal = true
                            }
                        )
                        
                        // App Restriction Card
                        VStack(alignment: .leading, spacing: 14) {
                            HStack {
                                Text("🌾 Защищенные приложения")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.zenInkPrimary)
                                Spacer()
                                Text("\(selection.applicationTokens.count) прилож.")
                                    .font(.system(size: 12, weight: .medium))
                                    .foregroundColor(.zenShuIro)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color.zenShuIroLight)
                                    .cornerRadius(8)
                            }
                            
                            Text("При попытке запуска этих приложений система включит 10-минутную паузу на варку риса.")
                                .font(.system(size: 12.5))
                                .foregroundColor(.zenInkSecondary)
                                .lineSpacing(2)
                            
                            Button(action: { isPickerPresented = true }) {
                                HStack {
                                    Image(systemName: "slider.horizontal.3")
                                    Text(selection.applicationTokens.isEmpty ? "Выбрать приложения" : "Настроить список")
                                }
                                .zenButtonStyle(bg: .zenShuIro, height: 46)
                            }
                            .familyActivityPicker(isPresented: $isPickerPresented, selection: $selection)
                            .onChange(of: selection) { newSelection in
                                timerManager.saveSelection(newSelection)
                            }
                        }
                        .padding(18)
                        .background(Color.zenBeigeSurface)
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.zenBeigeBorder, lineWidth: 1)
                        )
                        
                        // Philosophy & Guidance Card
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                Text("🍵 Мудрость дзен")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.zenInkPrimary)
                                Spacer()
                                Button(action: { showTutorialModal = true }) {
                                    Text("Обучение →")
                                        .font(.system(size: 12, weight: .medium))
                                        .foregroundColor(.zenKinGold)
                                }
                            }
                            
                            Text("«Человеку даётся шанс на спасение, пока варится котел с рисом». Если желание не исчезнет за 10 минут — вы осознанно откроете приложение на 2 минуты.")
                                .font(.system(size: 12, design: .serif))
                                .foregroundColor(.zenInkSecondary)
                                .lineSpacing(3)
                        }
                        .padding(18)
                        .background(Color.zenBeigeSurface)
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.zenBeigeBorder, lineWidth: 1)
                        )
                        
                        // Re-shield / Reset actions
                        Button(action: {
                            timerManager.applyShields(from: selection)
                        }) {
                            HStack {
                                Image(systemName: "lock.shield")
                                Text("Обновить защиту Screen Time")
                            }
                            .font(.system(size: 13))
                            .foregroundColor(.zenInkSecondary)
                            .padding(.vertical, 8)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 30)
                }
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showTutorialModal) {
                TutorialView(
                    onFinish: { showTutorialModal = false },
                    onSkip: { showTutorialModal = false },
                    isModal: true
                )
            }
            .sheet(isPresented: $showTimerModal) {
                ZenTimerView()
                    .environmentObject(timerManager)
            }
            .onAppear {
                self.selection = timerManager.loadSelection()
            }
        }
    }
}

// MARK: - Status Card Component

struct StatusCard: View {
    let status: TimerStatus
    let onTapTimer: () -> Void
    let onStartTestCooking: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Circle()
                    .fill(statusColor)
                    .frame(width: 10, height: 10)
                
                Text(statusTitle)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.zenInkPrimary)
                
                Spacer()
                
                if status == .cooking || status == .ready {
                    Button(action: onTapTimer) {
                        Text("Открыть таймер →")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(.zenShuIro)
                    }
                }
            }
            
            Text(statusDescription)
                .font(.system(size: 12))
                .foregroundColor(.zenInkSecondary)
                .lineSpacing(2)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            if status == .idle {
                Button(action: onStartTestCooking) {
                    HStack {
                        Image(systemName: "flame")
                        Text("Поставить рис вариться (Начать тест)")
                    }
                    .zenButtonStyle(bg: .zenKinGold, height: 42)
                }
            }
        }
        .padding(18)
        .background(Color.zenBeigeSurface)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.zenBeigeBorder, lineWidth: 1)
        )
    }
    
    private var statusColor: Color {
        switch status {
        case .idle: return .zenMatsuGreen
        case .cooking: return .zenShuIro
        case .ready: return .zenKinGold
        case .expired: return .zenInkTertiary
        }
    }
    
    private var statusTitle: String {
        switch status {
        case .idle: return "Защита активна (Покой)"
        case .cooking: return "Рис варится (10-минутная пауза)"
        case .ready: return "Рис готов! (Окно 2 минуты)"
        case .expired: return "Окно истекло"
        }
    }
    
    private var statusDescription: String {
        switch status {
        case .idle:
            return "Приложения заблокированы. При попытке открытия вы увидите экран ожидания Kuku Timer."
        case .cooking:
            return "Таймер отсчитывает 10 минут. Вы получите уведомление, когда окно откроется."
        case .ready:
            return "Врата открыты на 2 минуты. Откройте таймер для подтверждения осознанного входа."
        case .expired:
            return "Время окна возможностей истекло. Потребуется новая 10-минутная пауза."
        }
    }
}
