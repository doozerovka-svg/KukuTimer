import SwiftUI

// MARK: - 1. Japanese Bonsai Tree Watermark (盆栽ウォーターマーク)

public struct BonsaiWatermarkView: View {
    public var alpha: Double = 0.07
    
    public init(alpha: Double = 0.07) {
        self.alpha = alpha
    }
    
    public var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let inkColor = Color.zenInkPrimary.opacity(alpha)
            let greenColor = Color.zenMatsuGreen.opacity(alpha * 1.3)
            
            let originX = w * 0.65
            let originY = h * 0.82
            let scale = w * 0.0022
            
            // 1. Pot
            var potPath = Path()
            potPath.move(to: CGPoint(x: originX - 70 * scale, y: originY + 15 * scale))
            potPath.addLine(to: CGPoint(x: originX + 70 * scale, y: originY + 15 * scale))
            potPath.addLine(to: CGPoint(x: originX + 55 * scale, y: originY + 38 * scale))
            potPath.addLine(to: CGPoint(x: originX - 55 * scale, y: originY + 38 * scale))
            potPath.closeSubpath()
            context.fill(potPath, with: .color(inkColor))
            context.stroke(potPath, with: .color(inkColor), lineWidth: 1.5)
            
            // Pot feet
            let foot1 = CGRect(x: originX - 50 * scale, y: originY + 38 * scale, width: 12 * scale, height: 5 * scale)
            let foot2 = CGRect(x: originX + 38 * scale, y: originY + 38 * scale, width: 12 * scale, height: 5 * scale)
            context.fill(Path(foot1), with: .color(inkColor))
            context.fill(Path(foot2), with: .color(inkColor))
            
            // 2. Trunk (Gnarled Bonsai curves)
            var trunkPath = Path()
            trunkPath.move(to: CGPoint(x: originX - 18 * scale, y: originY + 14 * scale))
            trunkPath.addCurve(
                to: CGPoint(x: originX - 10 * scale, y: originY - 120 * scale),
                control1: CGPoint(x: originX - 35 * scale, y: originY - 30 * scale),
                control2: CGPoint(x: originX + 25 * scale, y: originY - 70 * scale)
            )
            trunkPath.addCurve(
                to: CGPoint(x: originX + 5 * scale, y: originY - 180 * scale),
                control1: CGPoint(x: originX - 20 * scale, y: originY - 145 * scale),
                control2: CGPoint(x: originX - 5 * scale, y: originY - 165 * scale)
            )
            trunkPath.addCurve(
                to: CGPoint(x: originX + 75 * scale, y: originY - 125 * scale),
                control1: CGPoint(x: originX + 30 * scale, y: originY - 150 * scale),
                control2: CGPoint(x: originX + 60 * scale, y: originY - 130 * scale)
            )
            trunkPath.addLine(to: CGPoint(x: originX + 70 * scale, y: originY - 118 * scale))
            trunkPath.addCurve(
                to: CGPoint(x: originX + 8 * scale, y: originY - 110 * scale),
                control1: CGPoint(x: originX + 45 * scale, y: originY - 125 * scale),
                control2: CGPoint(x: originX + 15 * scale, y: originY - 140 * scale)
            )
            trunkPath.addCurve(
                to: CGPoint(x: originX + 14 * scale, y: originY + 14 * scale),
                control1: CGPoint(x: originX + 38 * scale, y: originY - 60 * scale),
                control2: CGPoint(x: originX - 12 * scale, y: originY - 20 * scale)
            )
            trunkPath.closeSubpath()
            context.fill(trunkPath, with: .color(inkColor))
            
            // 3. Pine Foliage Clouds (松の葉雲)
            drawPineCloud(context: context, cx: originX + 5 * scale, cy: originY - 190 * scale, rx: 55 * scale, ry: 28 * scale, color: greenColor)
            drawPineCloud(context: context, cx: originX + 80 * scale, cy: originY - 130 * scale, rx: 48 * scale, ry: 24 * scale, color: greenColor)
            drawPineCloud(context: context, cx: originX - 45 * scale, cy: originY - 135 * scale, rx: 50 * scale, ry: 25 * scale, color: greenColor)
            drawPineCloud(context: context, cx: originX - 65 * scale, cy: originY - 80 * scale, rx: 42 * scale, ry: 22 * scale, color: greenColor)
            drawPineCloud(context: context, cx: originX + 40 * scale, cy: originY - 75 * scale, rx: 38 * scale, ry: 20 * scale, color: greenColor)
        }
        .allowsHitTesting(false)
    }
    
    private func drawPineCloud(context: GraphicsContext, cx: CGFloat, cy: CGFloat, rx: CGFloat, ry: CGFloat, color: Color) {
        let r1 = CGRect(x: cx - rx, y: cy - ry, width: rx * 2, height: ry * 2)
        let r2 = CGRect(x: cx - rx * 0.7, y: cy - ry * 1.25, width: rx * 1.4, height: ry * 1.6)
        context.fill(Path(ellipseIn: r1), with: .color(color))
        context.fill(Path(ellipseIn: r2), with: .color(color))
    }
}

// MARK: - 2. Japanese Rice Bowl Watermark (茶碗 / 丼)

public struct RiceBowlWatermarkView: View {
    public var alpha: Double = 0.08
    @State private var steamAnim: CGFloat = 0
    
    public init(alpha: Double = 0.08) {
        self.alpha = alpha
    }
    
    public var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let inkColor = Color.zenInkPrimary.opacity(alpha)
            let riceColor = Color.zenKinGold.opacity(alpha * 1.5)
            
            let cx = w * 0.5
            let cy = h * 0.52
            let scale = w * 0.0028
            
            // 1. Base Ring
            let baseRect = CGRect(x: cx - 35 * scale, y: cy + 95 * scale, width: 70 * scale, height: 12 * scale)
            context.fill(Path(roundedRect: baseRect, cornerRadius: 4 * scale), with: .color(inkColor))
            
            // 2. Bowl Ceramic Body
            var bowlPath = Path()
            bowlPath.move(to: CGPoint(x: cx - 110 * scale, y: cy))
            bowlPath.addCurve(
                to: CGPoint(x: cx - 35 * scale, y: cy + 95 * scale),
                control1: CGPoint(x: cx - 105 * scale, y: cy + 65 * scale),
                control2: CGPoint(x: cx - 60 * scale, y: cy + 95 * scale)
            )
            bowlPath.addLine(to: CGPoint(x: cx + 35 * scale, y: cy + 95 * scale))
            bowlPath.addCurve(
                to: CGPoint(x: cx + 110 * scale, y: cy),
                control1: CGPoint(x: cx + 60 * scale, y: cy + 95 * scale),
                control2: CGPoint(x: cx + 105 * scale, y: cy + 65 * scale)
            )
            bowlPath.addCurve(
                to: CGPoint(x: cx - 110 * scale, y: cy),
                control1: CGPoint(x: cx + 70 * scale, y: cy - 10 * scale),
                control2: CGPoint(x: cx - 70 * scale, y: cy - 10 * scale)
            )
            bowlPath.closeSubpath()
            context.stroke(bowlPath, with: .color(inkColor), lineWidth: 2 * scale)
            
            // 3. Steaming Mound of Rice (ふっくらご飯)
            var riceMoundPath = Path()
            riceMoundPath.move(to: CGPoint(x: cx - 100 * scale, y: cy - 2 * scale))
            riceMoundPath.addCurve(
                to: CGPoint(x: cx, y: cy - 75 * scale),
                control1: CGPoint(x: cx - 85 * scale, y: cy - 55 * scale),
                control2: CGPoint(x: cx - 30 * scale, y: cy - 75 * scale)
            )
            riceMoundPath.addCurve(
                to: CGPoint(x: cx + 100 * scale, y: cy - 2 * scale),
                control1: CGPoint(x: cx + 30 * scale, y: cy - 75 * scale),
                control2: CGPoint(x: cx + 85 * scale, y: cy - 55 * scale)
            )
            riceMoundPath.closeSubpath()
            context.fill(riceMoundPath, with: .color(riceColor))
            context.stroke(riceMoundPath, with: .color(inkColor), lineWidth: 1.5 * scale)
            
            // 4. Steam curves
            drawSteamCurve(context: context, startX: cx - 35 * scale, startY: cy - 85 * scale - steamAnim, height: 45 * scale, color: inkColor)
            drawSteamCurve(context: context, startX: cx, startY: cy - 95 * scale - steamAnim * 1.2, height: 55 * scale, color: inkColor)
            drawSteamCurve(context: context, startX: cx + 35 * scale, startY: cy - 85 * scale - steamAnim, height: 45 * scale, color: inkColor)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 4.0).repeatForever(autoreverses: true)) {
                steamAnim = 20
            }
        }
        .allowsHitTesting(false)
    }
    
    private func drawSteamCurve(context: GraphicsContext, startX: CGFloat, startY: CGFloat, height: CGFloat, color: Color) {
        var path = Path()
        path.move(to: CGPoint(x: startX, y: startY))
        path.addCurve(
            to: CGPoint(x: startX, y: startY - height),
            control1: CGPoint(x: startX - 12, y: startY - height * 0.35),
            control2: CGPoint(x: startX + 12, y: startY - height * 0.7)
        )
        context.stroke(path, with: .color(color), style: StrokeStyle(lineWidth: 2, lineCap: .round))
    }
}

// MARK: - 3. Zen Rice Grain Orbiting Dial (米粒ダイヤル)

public struct RiceGrainDialView<Content: View>: View {
    public var isReady: BooleanLiteralType
    public let content: Content
    
    @State private var rotationAngle: Double = 0
    @State private var breathingScale: CGFloat = 1.0
    
    public init(isReady: Bool = false, @ViewBuilder content: () -> Content) {
        self.isReady = isReady
        self.content = content()
    }
    
    public var body: some View {
        ZStack {
            Canvas { context, size in
                let radius = (min(size.width, size.height) / 2.0) - 24
                let center = CGPoint(x: size.width / 2.0, y: size.height / 2.0)
                
                // Outer subtle border
                let outerRect = CGRect(
                    x: center.x - (radius * breathingScale + 12),
                    y: center.y - (radius * breathingScale + 12),
                    width: (radius * breathingScale + 12) * 2,
                    height: (radius * breathingScale + 12) * 2
                )
                context.stroke(Path(ellipseIn: outerRect), with: .color(.zenBeigeBorder), lineWidth: 1)
                
                // Track
                let trackRect = CGRect(
                    x: center.x - (radius * breathingScale),
                    y: center.y - (radius * breathingScale),
                    width: (radius * breathingScale) * 2,
                    height: (radius * breathingScale) * 2
                )
                let trackColor = isReady ? Color.zenKinGold.opacity(0.4) : Color.zenShuIro.opacity(0.25)
                context.stroke(Path(ellipseIn: trackRect), with: .color(trackColor), lineWidth: 2)
                
                // 12 Rice Grains orbiting around the dial
                let grainCount = 12
                for i in 0..<grainCount {
                    let angleDeg = rotationAngle + Double(i) * (360.0 / Double(grainCount))
                    let angleRad = angleDeg * .pi / 180.0
                    let gx = center.x + (radius * breathingScale * CGFloat(cos(angleRad)))
                    let gy = center.y + (radius * breathingScale * CGFloat(sin(angleRad)))
                    
                    let fillColor: Color = isReady ? .zenKinGold : (i % 2 == 0 ? .zenShuIro : .zenRiceGrain)
                    let borderColor: Color = isReady ? .zenKinGold : (i % 2 == 0 ? .zenShuIro : .zenRiceGrainBorder)
                    
                    drawRiceGrain(
                        context: context,
                        center: CGPoint(x: gx, y: gy),
                        angleDeg: angleDeg + 45,
                        length: 14,
                        width: 7,
                        fillColor: fillColor,
                        borderColor: borderColor
                    )
                }
            }
            .frame(width: 250, height: 250)
            
            // Center content
            content
        }
        .onAppear {
            withAnimation(.linear(duration: 20).repeatForever(autoreverses: false)) {
                rotationAngle = 360
            }
            withAnimation(.easeInOut(duration: 3.5).repeatForever(autoreverses: true)) {
                breathingScale = 1.04
            }
        }
    }
    
    private func drawRiceGrain(
        context: GraphicsContext,
        center: CGPoint,
        angleDeg: Double,
        length: CGFloat,
        width: CGFloat,
        fillColor: Color,
        borderColor: Color
    ) {
        let rad = angleDeg * .pi / 180.0
        let cosA = CGFloat(cos(rad))
        let sinA = CGFloat(sin(rad))
        
        let halfL = length / 2.0
        let halfW = width / 2.0
        
        let p1 = CGPoint(x: center.x - halfL * cosA, y: center.y - halfL * sinA)
        let p2 = CGPoint(x: center.x + halfL * cosA, y: center.y + halfL * sinA)
        
        let cp1 = CGPoint(x: center.x - halfW * sinA, y: center.y + halfW * cosA)
        let cp2 = CGPoint(x: center.x + halfW * sinA, y: center.y - halfW * cosA)
        
        var path = Path()
        path.move(to: p1)
        path.addQuadCurve(to: p2, control: cp1)
        path.addQuadCurve(to: p1, control: cp2)
        path.closeSubpath()
        
        context.fill(path, with: .color(fillColor))
        context.stroke(path, with: .color(borderColor), lineWidth: 1.2)
    }
}
