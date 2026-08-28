import ManagedSettings
import ManagedSettingsUI
import UIKit

// MARK: - Shield Configuration Extension
// Provides dynamic Zen Japanese aesthetics for iOS Screen Time Shield screens

class ShieldConfigurationExtension: ShieldConfigurationDataSource {
    
    override func configuration(shielding application: Application) -> ShieldConfiguration {
        return createZenShieldConfiguration()
    }
    
    override func configuration(shielding application: Application, in category: ActivityCategory) -> ShieldConfiguration {
        return createZenShieldConfiguration()
    }
    
    override func configuration(shielding webDomain: WebDomain) -> ShieldConfiguration {
        return createZenShieldConfiguration()
    }
    
    override func configuration(shielding webDomain: WebDomain, in category: ActivityCategory) -> ShieldConfiguration {
        return createZenShieldConfiguration()
    }
    
    private func createZenShieldConfiguration() -> ShieldConfiguration {
        let status = TimerStateManager.shared.status
        
        let sumiColor = UIColor(red: 0x24/255.0, green: 0x22/255.0, blue: 0x1F/255.0, alpha: 1.0)
        let shuIroColor = UIColor(red: 0xC7/255.0, green: 0x48/255.0, blue: 0x43/255.0, alpha: 1.0)
        let kinGoldColor = UIColor(red: 0xC5/255.0, green: 0x98/255.0, blue: 0x47/255.0, alpha: 1.0)
        let matsuGreenColor = UIColor(red: 0x4A/255.0, green: 0x6B/255.0, blue: 0x54/255.0, alpha: 1.0)
        let kinariWhite = UIColor(red: 0xFD/255.0, green: 0xFC/255.0, blue: 0xF7/255.0, alpha: 1.0)
        
        switch status {
        case .idle:
            return ShieldConfiguration(
                backgroundBlurStyle: .systemMaterialDark,
                backgroundColor: sumiColor.withAlphaComponent(0.85),
                icon: UIImage(systemName: "timer"),
                title: ShieldConfiguration.VisualContent(
                    text: "一炊の夢 • Kuku Timer",
                    color: kinariWhite
                ),
                subtitle: ShieldConfiguration.VisualContent(
                    text: "«Человеку даётся шанс на спасение, пока варится рис».\nСделайте глубокий вдох и начните 10-минутную осознанную паузу.",
                    color: kinGoldColor
                ),
                primaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "🍚 Поставить рис вариться (10 мин)",
                    color: kinariWhite
                ),
                primaryButtonBackgroundColor: shuIroColor,
                secondaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "Отложить телефон",
                    color: UIColor.lightGray
                )
            )
            
        case .cooking:
            let remainingSecs = Int(TimerStateManager.shared.remainingCookSeconds)
            let mins = remainingSecs / 60
            let secs = remainingSecs % 60
            let timeStr = String(format: "%02d:%02d", mins, secs)
            
            return ShieldConfiguration(
                backgroundBlurStyle: .systemMaterialDark,
                backgroundColor: sumiColor.withAlphaComponent(0.88),
                icon: UIImage(systemName: "flame.fill"),
                title: ShieldConfiguration.VisualContent(
                    text: "Котел с рисом закипает...",
                    color: kinariWhite
                ),
                subtitle: ShieldConfiguration.VisualContent(
                    text: "Осталось ожидать: \(timeStr)\nКогда рис сварится, вы получите уведомление.",
                    color: kinGoldColor
                ),
                primaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "⏳ Идет ожидание (\(timeStr))",
                    color: kinariWhite
                ),
                primaryButtonBackgroundColor: sumiColor,
                secondaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "Закрыть",
                    color: UIColor.lightGray
                )
            )
            
        case .ready:
            return ShieldConfiguration(
                backgroundBlurStyle: .systemMaterialDark,
                backgroundColor: sumiColor.withAlphaComponent(0.85),
                icon: UIImage(systemName: "sparkles"),
                title: ShieldConfiguration.VisualContent(
                    text: "Котел закипел. Рис готов!",
                    color: kinGoldColor
                ),
                subtitle: ShieldConfiguration.VisualContent(
                    text: "Врата открыты на 2 минуты.\nОсознанность подтверждена — приятного использования!",
                    color: kinariWhite
                ),
                primaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "🌸 Войти в приложение",
                    color: kinariWhite
                ),
                primaryButtonBackgroundColor: matsuGreenColor,
                secondaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "Не входить",
                    color: UIColor.lightGray
                )
            )
            
        case .expired:
            return ShieldConfiguration(
                backgroundBlurStyle: .systemMaterialDark,
                backgroundColor: sumiColor.withAlphaComponent(0.85),
                icon: UIImage(systemName: "moon.fill"),
                title: ShieldConfiguration.VisualContent(
                    text: "Окно возможностей остыло",
                    color: kinariWhite
                ),
                subtitle: ShieldConfiguration.VisualContent(
                    text: "2 минуты истекли. Чтобы войти, потребуется снова поставить рис вариться.",
                    color: kinGoldColor
                ),
                primaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "🍚 Начать заново (10 мин)",
                    color: kinariWhite
                ),
                primaryButtonBackgroundColor: shuIroColor,
                secondaryButtonLabel: ShieldConfiguration.VisualContent(
                    text: "Закрыть",
                    color: UIColor.lightGray
                )
            )
        }
    }
}
