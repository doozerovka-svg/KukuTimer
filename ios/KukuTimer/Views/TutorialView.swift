import SwiftUI

public struct TutorialSlideModel: Identifiable {
    public let id = UUID()
    public let kanji: String
    public let subtitle: String
    public let title: String
    public let description: String
    public let takeaway: String
    public let illustrationType: Int
}

public struct TutorialView: View {
    public var onFinish: () -> Void
    public var onSkip: () -> Void
    public var isModal: Bool = false
    
    @State private var currentSlide: Int = 0
    
    private let slides: [TutorialSlideModel] = [
        TutorialSlideModel(
            kanji: "一炊の夢",
            subtitle: "ФИЛОСОФИЯ И ПСИХОЛОГИЯ",
            title: "Пауза в котел риса",
            description: "В японском фольклоре и дзен-притчах время, пока варится котел с рисом — это шанс на спасение и озарение.\n\nВместо жестких запретов Kuku Timer вводит осознанное отложенное вознаграждение: импульсивное желание рассеивается за 10 минут ожидания.",
            takeaway: "💡 90% импульсов зайти в соцсети исчезают за 10 минут спокойной паузы.",
            illustrationType: 0
        ),
        TutorialSlideModel(
            kanji: "選択と封印",
            subtitle: "ШАГ 1: ВЫБОР ПРИЛОЖЕНИЙ",
            title: "Выбираем цифровые соблазны",
            description: "В списке приложений выберите те, к которым у вас есть навязчивая привычка тянуться (TikTok, Instagram, игры, браузер или само приложение).\n\nОни будут взяты под защиту Screen Time и Kuku Timer.",
            takeaway: "📱 Вы сами определяете свои правила и уровень цифрового детокса.",
            illustrationType: 1
        ),
        TutorialSlideModel(
            kanji: "十息一炊",
            subtitle: "ШАГ 2: ПЕРЕХВАТ И ОЖИДАНИЕ",
            title: "10 минут: «Рис закипает»",
            description: "Когда вы попытаетесь открыть заблокированное приложение, iOS покажет экран со спокойным напоминанием.\n\nНажмите «Поставить вариться рис», положите телефон экраном вниз и вернитесь в реальный мир.",
            takeaway: "⏳ Время не сбрасывается при выходе — 10 минут отсчитываются честно.",
            illustrationType: 2
        ),
        TutorialSlideModel(
            kanji: "開門得道",
            subtitle: "ШАГ 3: ОКНО ВОЗМОЖНОСТЕЙ",
            title: "2 минуты на осознанный вход",
            description: "Ровно через 10 минут котел с рисом сварится — вы получите уведомление.\n\nУ вас есть 2 минуты: если вы войдете сейчас — приложение разблокируется. Если пропустите — таймер перезапустится.",
            takeaway: "✨ Если приложение вам действительно нужно — вы зайдете. Если нет — вы спасли свое время.",
            illustrationType: 3
        )
    ]
    
    public var body: some View {
        ZStack {
            Color.zenBeigeBackground.ignoresSafeArea()
            BonsaiWatermarkView(alpha: 0.06)
            
            VStack(spacing: 16) {
                // Top Bar
                HStack {
                    HStack(spacing: 6) {
                        Text("ククターマー")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.zenShuIro)
                            .tracking(3)
                        Text("• 指南")
                            .font(.system(size: 13))
                            .foregroundColor(.zenKinGold)
                    }
                    Spacer()
                    Button(action: onSkip) {
                        Text(isModal ? "Закрыть ✕" : "Пропустить →")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.zenInkSecondary)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 12)
                
                // Slide Content Card
                let slide = slides[currentSlide]
                VStack(spacing: 14) {
                    VStack(spacing: 4) {
                        Text(slide.kanji)
                            .font(.system(size: 16, weight: .bold, design: .serif))
                            .foregroundColor(.zenShuIro)
                            .tracking(6)
                        Text(slide.subtitle)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.zenInkTertiary)
                            .tracking(2)
                        Text(slide.title)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(.zenInkPrimary)
                            .multilineTextAlignment(.center)
                    }
                    
                    // Illustration Canvas
                    TutorialInfographicCanvas(type: slide.illustrationType)
                        .frame(width: 140, height: 140)
                    
                    // Description
                    Text(slide.description)
                        .font(.system(size: 13))
                        .foregroundColor(.zenInkSecondary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(3)
                        .padding(.horizontal, 8)
                    
                    Spacer(minLength: 0)
                    
                    // Takeaway Banner
                    Text(slide.takeaway)
                        .font(.system(size: 11.5, weight: .medium))
                        .foregroundColor(.zenInkPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity)
                        .background(Color.zenKinGoldLight)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.zenKinGold.opacity(0.4), lineWidth: 1)
                        )
                }
                .padding(20)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.zenBeigeSurface)
                .cornerRadius(22)
                .overlay(
                    RoundedRectangle(cornerRadius: 22)
                        .stroke(Color.zenBeigeBorder, lineWidth: 1)
                )
                .padding(.horizontal, 20)
                
                // Bottom Page Indicators & Action Button
                VStack(spacing: 14) {
                    HStack(spacing: 8) {
                        ForEach(0..<slides.count, id: \.self) { index in
                            Capsule()
                                .fill(index == currentSlide ? Color.zenShuIro : Color.zenBeigeCard)
                                .frame(width: index == currentSlide ? 22 : 10, height: 8)
                                .overlay(
                                    Capsule().stroke(index == currentSlide ? Color.zenShuIro : Color.zenBeigeBorder, lineWidth: 1)
                                )
                                .onTapGesture {
                                    withAnimation { currentSlide = index }
                                }
                        }
                    }
                    
                    HStack(spacing: 12) {
                        if currentSlide > 0 {
                            Button(action: {
                                withAnimation { currentSlide -= 1 }
                            }) {
                                Text("← Назад")
                                    .font(.system(size: 14))
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
                        
                        Button(action: {
                            if currentSlide < slides.count - 1 {
                                withAnimation { currentSlide += 1 }
                            } else {
                                onFinish()
                            }
                        }) {
                            Text(currentSlide < slides.count - 1 ? "Далее →" : "Понятно, к настройке 🍙")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.zenBeigeSurface)
                                .frame(maxWidth: .infinity)
                                .frame(height: 48)
                                .background(currentSlide == slides.count - 1 ? Color.zenShuIro : Color.zenInkPrimary)
                                .cornerRadius(14)
                        }
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.bottom, 16)
            }
        }
    }
}

// MARK: - Slide Vector Infographics

struct TutorialInfographicCanvas: View {
    let type: Int
    
    var body: some View {
        Canvas { context, size in
            let cx = size.width / 2.0
            let cy = size.height / 2.0
            
            switch type {
            case 0:
                // Philosophy: Enso + Rice Bowl
                let ensoRadius: CGFloat = 45
                let ensoRect = CGRect(x: cx - ensoRadius, y: cy - ensoRadius, width: ensoRadius * 2, height: ensoRadius * 2)
                context.stroke(Path(ellipseIn: ensoRect), with: .color(.zenBeigeBorder), lineWidth: 2)
                
                var arcPath = Path()
                arcPath.addArc(center: CGPoint(x: cx, y: cy), radius: ensoRadius, startAngle: .degrees(-30), endAngle: .degrees(250), clockwise: false)
                context.stroke(arcPath, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                
                // Rice Bowl
                var bowl = Path()
                bowl.move(to: CGPoint(x: cx - 22, y: cy + 18))
                bowl.addLine(to: CGPoint(x: cx + 22, y: cy + 18))
                bowl.addQuadCurve(to: CGPoint(x: cx + 32, y: cy - 10), control: CGPoint(x: cx + 32, y: cy + 6))
                bowl.addLine(to: CGPoint(x: cx - 32, y: cy - 10))
                bowl.addQuadCurve(to: CGPoint(x: cx - 22, y: cy + 18), control: CGPoint(x: cx - 32, y: cy + 6))
                bowl.closeSubpath()
                context.stroke(bowl, with: .color(.zenInkPrimary), lineWidth: 2)
                
                var mound = Path()
                mound.move(to: CGPoint(x: cx - 28, y: cy - 10))
                mound.addQuadCurve(to: CGPoint(x: cx + 28, y: cy - 10), control: CGPoint(x: cx, y: cy - 30))
                mound.closeSubpath()
                context.fill(mound, with: .color(.zenKinGold))
                
            case 1:
                // App selection phone
                let pw: CGFloat = 55
                let ph: CGFloat = 80
                let phoneRect = CGRect(x: cx - pw/2, y: cy - ph/2, width: pw, height: ph)
                context.fill(Path(roundedRect: phoneRect, cornerRadius: 10), with: .color(.zenBeigeSurface))
                context.stroke(Path(roundedRect: phoneRect, cornerRadius: 10), with: .color(.zenInkPrimary), lineWidth: 2)
                
                for i in 0..<3 {
                    let rowY = cy - 20 + CGFloat(i * 20)
                    let iconRect = CGRect(x: cx - pw/2 + 6, y: rowY - 5, width: 10, height: 10)
                    context.fill(Path(roundedRect: iconRect, cornerRadius: 2), with: .color(i == 1 ? .zenShuIroLight : .zenBeigeCard))
                    
                    var line = Path()
                    line.move(to: CGPoint(x: cx - pw/2 + 20, y: rowY))
                    line.addLine(to: CGPoint(x: cx + 5, y: rowY))
                    context.stroke(line, with: .color(i == 1 ? .zenInkPrimary : .zenInkTertiary), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                    
                    let toggleRect = CGRect(x: cx + pw/2 - 14, y: rowY - 3, width: 10, height: 6)
                    context.fill(Path(roundedRect: toggleRect, cornerRadius: 3), with: .color(i == 1 ? .zenShuIro : .zenBeigeBorder))
                }
                
            case 2:
                // 10 min dial
                let dialRadius: CGFloat = 45
                let dialRect = CGRect(x: cx - dialRadius, y: cy - dialRadius, width: dialRadius * 2, height: dialRadius * 2)
                context.stroke(Path(ellipseIn: dialRect), with: .color(.zenBeigeBorder), lineWidth: 2)
                
                var arc = Path()
                arc.addArc(center: CGPoint(x: cx, y: cy), radius: dialRadius, startAngle: .degrees(-90), endAngle: .degrees(180), clockwise: false)
                context.stroke(arc, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 3.5, lineCap: .round))
                
                var hand1 = Path()
                hand1.move(to: CGPoint(x: cx, y: cy))
                hand1.addLine(to: CGPoint(x: cx, y: cy - 18))
                context.stroke(hand1, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                
                var hand2 = Path()
                hand2.move(to: CGPoint(x: cx, y: cy))
                hand2.addLine(to: CGPoint(x: cx + 12, y: cy))
                context.stroke(hand2, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 2, lineCap: .round))
                
            case 3:
                // Torii gate & gold shine
                let haloRect = CGRect(x: cx - 45, y: cy - 45, width: 90, height: 90)
                context.fill(Path(ellipseIn: haloRect), with: .color(.zenKinGoldLight))
                context.stroke(Path(ellipseIn: haloRect), with: .color(.zenKinGold), lineWidth: 2)
                
                let tw: CGFloat = 36
                let th: CGFloat = 36
                
                var beam1 = Path()
                beam1.move(to: CGPoint(x: cx - tw/2 - 6, y: cy - th/2))
                beam1.addLine(to: CGPoint(x: cx + tw/2 + 6, y: cy - th/2))
                context.stroke(beam1, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 3.5, lineCap: .round))
                
                var beam2 = Path()
                beam2.move(to: CGPoint(x: cx - tw/2, y: cy - th/2 + 8))
                beam2.addLine(to: CGPoint(x: cx + tw/2, y: cy - th/2 + 8))
                context.stroke(beam2, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 2.5, lineCap: .round))
                
                var p1 = Path()
                p1.move(to: CGPoint(x: cx - tw/2 + 5, y: cy - th/2))
                p1.addLine(to: CGPoint(x: cx - tw/2 + 5, y: cy + th/2 + 4))
                context.stroke(p1, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                
                var p2 = Path()
                p2.move(to: CGPoint(x: cx + tw/2 - 5, y: cy - th/2))
                p2.addLine(to: CGPoint(x: cx + tw/2 - 5, y: cy + th/2 + 4))
                context.stroke(p2, with: .color(.zenShuIro), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                
            default:
                break
            }
        }
    }
}
