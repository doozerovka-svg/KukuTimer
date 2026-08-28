import SwiftUI
import Combine

public struct ZenTimerView: View {
    @EnvironmentObject private var timerManager: TimerStateManager
    @Environment(\.dismiss) private var dismiss
    
    @State private var remainingSeconds: TimeInterval = 0
    @State private var isWindowOpen: Bool = false
    
    private let timer = Timer.publish(every: 0.5, on: .main, in: .common).autoconnect()
    
    public var body: some View {
        ZStack {
            Color.zenBeigeBackground.ignoresSafeArea()
            RiceBowlWatermarkView(alpha: 0.08)
            
            // Top Vermilion Ribbon
            VStack {
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [.clear, .zenShuIro, .zenKinGold, .clear],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(height: 4)
                Spacer()
            }
            .ignoresSafeArea()
            
            VStack(spacing: 24) {
                // Header Calligraphy & Philosophy
                VStack(spacing: 6) {
                    Text("一炊の夢")
                        .font(.system(size: 14, weight: .bold, design: .serif))
                        .foregroundColor(.zenShuIro)
                        .tracking(5)
                    
                    Text("KUKU TIMER")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.zenInkSecondary)
                        .tracking(4)
                    
                    Text(isWindowOpen ? "Котел закипел. Рис готов!" : "Котел с рисом закипает...")
                        .font(.system(size: 22, weight: .light))
                        .foregroundColor(.zenInkPrimary)
                        .padding(.top, 4)
                    
                    Text(isWindowOpen ? "«Врата открыты — осознанность подтверждена»" : "«Человеку даётся шанс на спасение, пока варится рис»")
                        .font(.system(size: 12, design: .serif))
                        .foregroundColor(.zenKinGold)
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 24)
                
                Spacer()
                
                // Central Zen Dial with 12 Orbiting Rice Grains
                RiceGrainDialView(isReady: isWindowOpen) {
                    VStack(spacing: 6) {
                        Image(systemName: "timer")
                            .font(.system(size: 32))
                            .foregroundColor(isWindowOpen ? .zenKinGold : .zenShuIro)
                        
                        if !isWindowOpen {
                            let mins = Int(remainingSeconds) / 60
                            let secs = Int(remainingSeconds) % 60
                            Text(String(format: "%02d:%02d", mins, secs))
                                .font(.system(size: 42, weight: .light, design: .monospaced))
                                .foregroundColor(.zenInkPrimary)
                        } else {
                            Text("2 МИН НА ВХОД")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.zenKinGold)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(Color.zenKinGoldLight)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.zenKinGold.opacity(0.6), lineWidth: 1)
                                )
                        }
                    }
                }
                
                Spacer()
                
                // Bottom Guidance & Actions
                VStack(spacing: 12) {
                    // Guidance Card
                    Text(isWindowOpen ?
                         "Окно доступа открыто! Нажмите кнопку ниже, чтобы разблокировать приложения на текущий сеанс." :
                         "Ограничение активно. Сделайте вдох и вернитесь к делам. Когда рис сварится, вы получите уведомление.")
                        .font(.system(size: 12))
                        .foregroundColor(.zenInkSecondary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(3)
                        .padding(14)
                        .frame(maxWidth: .infinity)
                        .background(Color.zenBeigeSurface)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.zenBeigeBorder, lineWidth: 1)
                        )
                    
                    if isWindowOpen {
                        Button(action: unlockSession) {
                            Text("🌸 Войти в приложение")
                                .zenButtonStyle(bg: .zenShuIro)
                        }
                    }
                    
                    Button(action: { dismiss() }) {
                        Text("Вернуться назад")
                            .font(.system(size: 13))
                            .foregroundColor(.zenInkSecondary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(Color.zenBeigeSurface)
                            .cornerRadius(14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(Color.zenBeigeBorder, lineWidth: 1)
                            )
                    }
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 20)
            }
        }
        .onReceive(timer) { _ in
            updateTimer()
        }
        .onAppear {
            updateTimer()
        }
    }
    
    private func updateTimer() {
        let status = timerManager.status
        if status == .cooking {
            remainingSeconds = timerManager.remainingCookSeconds
            isWindowOpen = false
        } else if status == .ready {
            remainingSeconds = timerManager.remainingWindowSeconds
            isWindowOpen = true
        } else {
            remainingSeconds = 0
            isWindowOpen = false
        }
    }
    
    private func unlockSession() {
        timerManager.grantAccess()
        dismiss()
    }
}
