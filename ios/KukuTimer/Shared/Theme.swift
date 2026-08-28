import SwiftUI

// MARK: - Traditional Japanese Zen Color Palette (日本の伝統色)

public extension Color {
    // Backgrounds & Surfaces
    /// 生成り (Kinari) - Soft warm beige rice paper (0xFFF7F5EE)
    static let zenBeigeBackground = Color(red: 0xF7 / 255.0, green: 0xF5 / 255.0, blue: 0xEE / 255.0)
    
    /// 白百合 (Shirayuri) - Warm white card surface (0xFFFDFCF7)
    static let zenBeigeSurface = Color(red: 0xFD / 255.0, green: 0xFC / 255.0, blue: 0xF7 / 255.0)
    
    /// 和紙 (Washi) - Textured beige secondary surface (0xFFF2EEE2)
    static let zenBeigeCard = Color(red: 0xF2 / 255.0, green: 0xEE / 255.0, blue: 0xE2 / 255.0)
    
    /// 砂利 (Jari) - Fine Zen garden stone border (0xFFE5DFC9)
    static let zenBeigeBorder = Color(red: 0xE5 / 255.0, green: 0xDF / 255.0, blue: 0xC9 / 255.0)
    
    /// Subtle divider (0xFFECE7D5)
    static let zenBeigeBorderSubtle = Color(red: 0xEC / 255.0, green: 0xE7 / 255.0, blue: 0xD5 / 255.0)
    
    // Inks (墨色)
    /// 墨色 (Sumi) - Deep sumi ink primary text (0xFF24221F)
    static let zenInkPrimary = Color(red: 0x24 / 255.0, green: 0x22 / 255.0, blue: 0x1F / 255.0)
    
    /// 灰墨 (Haizumi) - Soft charcoal secondary text (0xFF757067)
    static let zenInkSecondary = Color(red: 0x75 / 255.0, green: 0x70 / 255.0, blue: 0x67 / 255.0)
    
    /// 薄墨 (Usuzumi) - Light stone gray tertiary text (0xFFA6A094)
    static let zenInkTertiary = Color(red: 0xA6 / 255.0, green: 0xA0 / 255.0, blue: 0x94 / 255.0)
    
    // Accents
    /// 朱色 (Shu-iro) - Traditional Japanese vermilion (0xFFC74843)
    static let zenShuIro = Color(red: 0xC7 / 255.0, green: 0x48 / 255.0, blue: 0x43 / 255.0)
    
    /// 桜朱 (Sakura-shu) - Soft vermilion tint for badges (0xFFFBEBEA)
    static let zenShuIroLight = Color(red: 0xFB / 255.0, green: 0xEB / 255.0, blue: 0xEA / 255.0)
    
    /// 金箔 (Kinpaku) - Warm matte gold (0xFFC59847)
    static let zenKinGold = Color(red: 0xC5 / 255.0, green: 0x98 / 255.0, blue: 0x47 / 255.0)
    
    /// 黄金水 (Kogane) - Soft gold container (0xFFFAF3E6)
    static let zenKinGoldLight = Color(red: 0xFA / 255.0, green: 0xF3 / 255.0, blue: 0xE6 / 255.0)
    
    /// 松葉 (Matsuba) - Pine needle green for active status (0xFF4A6B54)
    static let zenMatsuGreen = Color(red: 0x4A / 255.0, green: 0x6B / 255.0, blue: 0x54 / 255.0)
    
    /// 松風 (Matsukaze) - Soft pine green container (0xFFEAF2EC)
    static let zenMatsuGreenLight = Color(red: 0xEA / 255.0, green: 0xF2 / 255.0, blue: 0xEC / 255.0)
    
    /// 米粒 (Kome) - Rice grain accent (0xFFEDE6D6)
    static let zenRiceGrain = Color(red: 0xED / 255.0, green: 0xE6 / 255.0, blue: 0xD6 / 255.0)
    
    /// Rice grain outline (0xFFDCD2BE)
    static let zenRiceGrainBorder = Color(red: 0xDC / 255.0, green: 0xD2 / 255.0, blue: 0xBE / 255.0)
}

public struct ZenButtonModifier: ViewModifier {
    public var backgroundColor: Color = .zenShuIro
    public var foregroundColor: Color = .zenBeigeSurface
    public var height: CGFloat = 50
    
    public func body(content: Content) -> some View {
        content
            .font(.system(size: 15, weight: .semibold))
            .foregroundColor(foregroundColor)
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .background(backgroundColor)
            .cornerRadius(14)
            .shadow(color: backgroundColor.opacity(0.2), radius: 6, x: 0, y: 3)
    }
}

public extension View {
    func zenButtonStyle(bg: Color = .zenShuIro, fg: Color = .zenBeigeSurface, height: CGFloat = 50) -> some View {
        self.modifier(ZenButtonModifier(backgroundColor: bg, foregroundColor: fg, height: height))
    }
}
