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

extension View {
    /// A row of a card of the editor: 56 pt high (or [height]) with no vertical insets, as in the design.
    func formRow(height: CGFloat = 56) -> some View {
        frame(minHeight: height)
            .listRowInsets(EdgeInsets(top: 0, leading: 17.5, bottom: 0, trailing: 17.5))
    }
}

/// The argb of a colour of the palette of habits.
func paletteArgb(_ index: Int) -> Int64 {
    var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
    UIColor(editorPalette(min(max(index, 0), 19))).getRed(&red, green: &green, blue: &blue, alpha: &alpha)
    let r = Int64((red * 255).rounded())
    let g = Int64((green * 255).rounded())
    let b = Int64((blue * 255).rounded())
    return Int64(0xFF000000) | (r << 16) | (g << 8) | b
}

/// The button that closes the editor: the close button of the system in iOS 26, a round cross before.
struct EditorCloseButton: View {
    let action: () -> Void

    var body: some View {
        #if compiler(>=6.2)
        if #available(iOS 26.0, *) {
            Button(role: .close, action: action)
        } else {
            legacy
        }
        #else
        legacy
        #endif
    }

    private var legacy: some View {
        Button(action: action) {
            Image(systemName: "xmark")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color.primary)
        }
        .accessibilityLabel(L("cancel", "Cancel"))
    }
}

/// The four colours of the design as round swatches; a colour of the palette that is not one of them is shown as a fifth.
struct ColorSwatchRow: View {
    let selected: Int
    let onPick: (Int) -> Void

    private static let main = [11, 15, 2, 13]
    private static let names = [11: "Blue", 15: "Pink", 2: "Orange", 13: "Purple"]

    var body: some View {
        let palettes = Self.main.contains(selected) ? Self.main : Self.main + [selected]
        HStack(spacing: 0) {
            ForEach(palettes, id: \.self) { index in
                let tone = habitTone(palette: index, colorArgb: paletteArgb(index))
                Button { onPick(index) } label: {
                    ZStack {
                        Circle().fill(tone.accent).frame(width: 26, height: 26)
                        if index == selected {
                            Circle().strokeBorder(tone.accent, lineWidth: 2).frame(width: 35, height: 35)
                            Image(systemName: "checkmark")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundStyle(tone.onAccent)
                        }
                    }
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Self.names[index] ?? L("color", "Color"))
                .accessibilityAddTraits(index == selected ? .isSelected : [])
            }
        }
    }
}

/// The days of the week of the reminder as round toggles, starting on Monday. `days` has a bit for each day, as the
/// reminder keeps them: Saturday is bit 0, Sunday bit 1, Monday bit 2 and so on up to Friday.
struct WeekdayCircles: View {
    let days: Int
    let tone: HabitTone
    let onChange: (Int) -> Void

    // The bit of each day from Monday to Sunday, and its position in the symbols of the calendar (Sunday first)
    private static let bits = [2, 3, 4, 5, 6, 0, 1]
    private static let symbolIndex = [1, 2, 3, 4, 5, 6, 0]

    var body: some View {
        let letters = Calendar.current.veryShortWeekdaySymbols
        let names = Calendar.current.weekdaySymbols
        HStack(spacing: 0) {
            ForEach(0..<7, id: \.self) { position in
                if position > 0 { Spacer(minLength: 0) }
                let bit = Self.bits[position]
                let symbol = Self.symbolIndex[position]
                let on = (days & (1 << bit)) != 0
                Button { onChange(days ^ (1 << bit)) } label: {
                    ZStack {
                        Circle().fill(on ? tone.accent : tone.track)
                        Text(letters.indices.contains(symbol) ? letters[symbol] : "")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(on ? tone.onAccent : Color.secondary)
                    }
                    .frame(width: 38, height: 38)
                    .contentShape(Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(names.indices.contains(symbol) ? names[symbol] : "")
                .accessibilityAddTraits(on ? .isSelected : [])
            }
        }
    }
}
