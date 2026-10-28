// Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
//
// This file is part of Loop Habit Tracker.
//
// Loop Habit Tracker is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Loop Habit Tracker is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
//
// You should have received a copy of the GNU General Public License along
// with this program. If not, see <http://www.gnu.org/licenses/>.
//

import SwiftUI
import UIKit

/// A colour that follows the light and dark appearance of the screen it is drawn on.
func dynamicColor(light: UInt32, dark: UInt32) -> Color {
    Color(UIColor { trait in trait.userInterfaceStyle == .dark ? UIColor(rgb: dark) : UIColor(rgb: light) })
}

extension UIColor {
    convenience init(rgb: UInt32) {
        self.init(
            red: CGFloat((rgb >> 16) & 0xff) / 255,
            green: CGFloat((rgb >> 8) & 0xff) / 255,
            blue: CGFloat(rgb & 0xff) / 255,
            alpha: 1
        )
    }
}

/// The colours of one habit on the list: the accent of its ring and check, the glyph on it and the track of the ring.
struct HabitTone {
    let accent: Color
    let onAccent: Color
    var track: Color { accent.opacity(0.2) }
}

private struct Family {
    let light: UInt32
    let dark: UInt32
}

// The four families of the design (blue, pink, orange and purple habits), as drawn in the mockups
private let families: [Int: Family] = [
    11: Family(light: 0x0A62D6, dark: 0x64A8FF),
    15: Family(light: 0xC8164A, dark: 0xFF7A99),
    2: Family(light: 0xB85200, dark: 0xFFA23D),
    13: Family(light: 0x8E3FC9, dark: 0xC48BFF)
]

private let onAccentLight: UInt32 = 0xFFFFFF
private let onAccentDark: UInt32 = 0x0B0B0F

/// The tone of a habit whose colour is the palette colour [palette]. The four colours of the design have the exact colours
/// of the mockups; any other colour gets an accent of the same lightness made from its hue.
func habitTone(palette: Int, colorArgb: Int64) -> HabitTone {
    let onAccent = dynamicColor(light: onAccentLight, dark: onAccentDark)
    if let family = families[palette] {
        return HabitTone(accent: dynamicColor(light: family.light, dark: family.dark), onAccent: onAccent)
    }
    let value = UInt32(truncatingIfNeeded: colorArgb)
    let red = Double((value >> 16) & 0xff) / 255
    let green = Double((value >> 8) & 0xff) / 255
    let blue = Double(value & 0xff) / 255
    let (seedChroma, hue) = oklch(red: red, green: green, blue: blue)
    let light = toneRgb(hue: hue, chroma: seedChroma * 0.9, tone: 41)
    let dark = toneRgb(hue: hue, chroma: 0.10, tone: 80)
    return HabitTone(accent: dynamicColor(light: light, dark: dark), onAccent: onAccent)
}

/// The grey of the pill of a numerical habit that has not reached its target.
let pillGrey = dynamicColor(light: 0xE9E9EB, dark: 0x3D3D41)

// MARK: - Icons

/// The pool of symbols that habits take their icon from; it only grows at its end so the icon of a habit never changes.
let habitSymbols = [
    "sun.max", "fork.knife", "pencil", "clock", "leaf", "waveform.path.ecg", "book.closed", "globe",
    "figure.mind.and.body", "music.note", "phone", "heart", "star", "flame", "drop", "moon",
    "bed.double", "figure.walk", "dumbbell", "bicycle", "cup.and.saucer", "pawprint", "lightbulb",
    "cart", "paintbrush", "camera", "gamecontroller", "house"
]

func habitSymbol(id: Int64) -> String {
    habitSymbols[Int(abs(id) % Int64(habitSymbols.count))]
}

// MARK: - Views

/// The icon of a habit inside a ring that shows its score.
struct ScoreRing: View {
    let score: Int
    let symbol: String
    let tone: HabitTone
    var size: CGFloat = 37

    var body: some View {
        let line = size * 3 / 37
        ZStack {
            Circle().strokeBorder(tone.track, lineWidth: line)
            Circle()
                .inset(by: line / 2)
                .trim(from: 0, to: CGFloat(min(max(score, 0), 100)) / 100)
                .stroke(tone.accent, style: StrokeStyle(lineWidth: line, lineCap: .round))
                .rotationEffect(.degrees(-90))
            Image(systemName: symbol)
                .font(.system(size: size * 17 / 37))
                .foregroundStyle(tone.accent)
        }
        .frame(width: size, height: size)
        .accessibilityHidden(true)
    }
}

/// The round mark of a yes or no habit: filled with a check when done, an empty circle when not.
struct HabitCheck: View {
    let tone: HabitTone
    let done: Bool
    var mark: String?

    var body: some View {
        ZStack {
            if done {
                Circle().fill(tone.accent)
                Image(systemName: "checkmark")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(tone.onAccent)
            } else {
                Circle().strokeBorder(Color(.systemGray), lineWidth: 1.5)
                if let mark {
                    Text(mark).font(.system(size: 13, weight: .semibold)).foregroundStyle(.secondary)
                }
            }
        }
        .frame(width: 28, height: 28)
    }
}

/// The amount of a numerical habit: filled with its colour when the target is reached, grey when not.
struct ValuePill: View {
    let text: String
    let tone: HabitTone
    let reached: Bool

    var body: some View {
        Text(text)
            .font(.system(size: 17, weight: .semibold))
            .lineLimit(1)
            .foregroundStyle(reached ? tone.onAccent : Color.primary)
            .padding(.horizontal, 16)
            .frame(height: 34)
            .background(reached ? tone.accent : pillGrey, in: Capsule())
    }
}

// MARK: - Oklab

private func toLinear(_ c: Double) -> Double {
    c <= 0.04045 ? c / 12.92 : pow((c + 0.055) / 1.055, 2.4)
}

private func toSrgb(_ linear: Double) -> Double {
    let c = min(max(linear, 0), 1)
    return c <= 0.0031308 ? 12.92 * c : 1.055 * pow(c, 1 / 2.4) - 0.055
}

/// The chroma and the hue (in degrees) of a colour in Oklab.
private func oklch(red: Double, green: Double, blue: Double) -> (Double, Double) {
    let r = toLinear(red), g = toLinear(green), b = toLinear(blue)
    let l = cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
    let m = cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
    let s = cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)
    let a = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s
    let bb = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
    var hue = atan2(bb, a) * 180 / Double.pi
    if hue < 0 { hue += 360 }
    return (hypot(a, bb), hue)
}

private func luminanceOfTone(_ tone: Double) -> Double {
    let f = (tone + 16) / 116
    let cubed = f * f * f
    return cubed > 216.0 / 24389 ? cubed : (116 * f - 16) / (24389.0 / 27)
}

private func linearRgb(_ lightness: Double, _ a: Double, _ b: Double) -> [Double] {
    let l = pow(lightness + 0.3963377774 * a + 0.2158037573 * b, 3)
    let m = pow(lightness - 0.1055613458 * a - 0.0638541728 * b, 3)
    let s = pow(lightness - 0.0894841775 * a - 1.2914855480 * b, 3)
    return [
        4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
        -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
    ]
}

/// The colour with the given hue (degrees), chroma and tone (0 to 100, as the lightness of Material 3) as 0xRRGGBB. If the
/// chroma does not exist at that tone it is lowered until the colour fits in sRGB.
private func toneRgb(hue: Double, chroma: Double, tone: Double) -> UInt32 {
    let target = luminanceOfTone(tone)
    let radians = hue * Double.pi / 180
    var c = chroma
    while true {
        var low = 0.0
        var high = 1.0
        for _ in 0..<40 {
            let mid = (low + high) / 2
            let rgb = linearRgb(mid, c * cos(radians), c * sin(radians))
            let luminance = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
            if luminance < target { low = mid } else { high = mid }
        }
        let rgb = linearRgb(low, c * cos(radians), c * sin(radians))
        if c <= 0.0001 || rgb.allSatisfy({ $0 >= -0.0001 && $0 <= 1.0001 }) {
            let r = UInt32((toSrgb(rgb[0]) * 255).rounded())
            let g = UInt32((toSrgb(rgb[1]) * 255).rounded())
            let b = UInt32((toSrgb(rgb[2]) * 255).rounded())
            return (r << 16) | (g << 8) | b
        }
        c -= 0.002
    }
}
