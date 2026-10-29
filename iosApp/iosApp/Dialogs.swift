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

import SalemKit
import SwiftUI

private func LF(_ key: String, _ fallback: String, _ args: CVarArg...) -> String {
    String(format: L(key, fallback), locale: Locale.current, arguments: args)
}

// MARK: - Sheet pieces

/// The close button of a sheet: the close button of the system in iOS 26, a round cross on a material before.
struct SheetCloseButton: View {
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
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(Color.primary)
                .frame(width: 44, height: 44)
                .background(.ultraThinMaterial, in: Circle())
        }
        .accessibilityLabel(L("cancel", "Cancel"))
    }
}

private struct SheetHeightKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = max(value, nextValue()) }
}

private struct FittedSheet: ViewModifier {
    @State private var height: CGFloat = 340

    func body(content: Content) -> some View {
        content
            .fixedSize(horizontal: false, vertical: true)
            .background(
                GeometryReader { proxy in
                    Color.clear.preference(key: SheetHeightKey.self, value: proxy.size.height)
                }
            )
            .onPreferenceChange(SheetHeightKey.self) { if $0 > 0 { height = $0 } }
            .presentationDetents([.height(height)])
            .presentationDragIndicator(.visible)
            .presentationBackground(Color(.systemGroupedBackground))
    }
}

extension View {
    /// Makes a sheet as tall as its content.
    func fittedSheet() -> some View { modifier(FittedSheet()) }
}

// MARK: - Habit type

struct HabitTypeDialog: View {
    let onSelected: (Int32) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Text(L("habit_type", "Habit Type"))
                    .font(.system(size: 17, weight: .semibold))
                HStack {
                    SheetCloseButton { dismiss() }
                    Spacer()
                }
            }
            .frame(height: 44)
            .padding(.top, 23)
            .padding(.horizontal, 16)
            VStack(spacing: 0) {
                row(
                    symbol: "checkmark",
                    title: L("yes_no_option", "Yes / No"),
                    text: L("yes_or_no_example", "e.g. Did you wake up early today? Did you exercise? Did you play chess?"),
                    type: 0
                )
                Divider().padding(.leading, 74)
                row(
                    symbol: "chart.bar.fill",
                    title: L("measurable", "Measurable"),
                    text: L("measurable_example", "e.g. How many miles did you run today? How many pages did you read?"),
                    type: 1
                )
            }
            .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 28, style: .continuous))
            .padding(.horizontal, 20)
            .padding(.top, 16)
            .padding(.bottom, 24)
        }
        .fittedSheet()
    }

    private func row(symbol: String, title: String, text: String, type: Int32) -> some View {
        Button {
            onSelected(type)
            dismiss()
        } label: {
            HStack(spacing: 14) {
                Image(systemName: symbol)
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(dynamicColor(light: 0x5856D6, dark: 0x8E8CFF))
                    .frame(width: 44, height: 44)
                    .background(
                        dynamicColor(light: 0xEBEAFA, dark: 0x3E3D5C),
                        in: RoundedRectangle(cornerRadius: 12, style: .continuous)
                    )
                VStack(alignment: .leading, spacing: 4) {
                    Text(title).font(.system(size: 17, weight: .semibold)).foregroundStyle(Color.primary)
                    Text(text)
                        .font(.system(size: 15))
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.leading)
                }
                Spacer(minLength: 0)
                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(Color(.tertiaryLabel))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 15)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Color

struct ColorPickerDialog: View {
    let selected: Int32
    let onPicked: (Int32) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 5), spacing: 12) {
                    ForEach(0..<20, id: \.self) { index in
                        Circle()
                            .fill(editorPalette(index))
                            .frame(width: 36, height: 36)
                            .overlay(Circle().stroke(Color.primary, lineWidth: selected == Int32(index) ? 2 : 0))
                            .onTapGesture {
                                onPicked(Int32(index))
                                dismiss()
                            }
                    }
                }
                .padding()
            }
            .navigationTitle(L("color", "Color"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
            }
        }
        .presentationDetents([.medium])
    }
}

func editorPalette(_ index: Int) -> Color {
    let hex = [
        0xD32F2F, 0xE64A19, 0xF57C00, 0xFF8F00, 0xF9A825,
        0xAFB42B, 0x7CB342, 0x388E3C, 0x00897B, 0x00ACC1,
        0x039BE5, 0x1976D2, 0x303F9F, 0x5E35B1, 0x8E24AA,
        0xD81B60, 0x5D4037, 0x303030, 0x757575, 0xAAAAAA
    ]
    return habitColor(Int64(hex[index]) | 0xFF000000)
}

// MARK: - Frequency

private enum FrequencyMode { case everyDay, everyXDays, timesPerWeek, timesPerMonth, timesInDays }

private struct FrequencyInput {
    var mode: FrequencyMode
    var everyXDays = "3"
    var perWeek = "3"
    var perMonth = "10"
    var x = "3"
    var y = "14"
}

private func frequencyFromInput(_ input: FrequencyInput) -> (Int, Int) {
    var numerator = 1
    var denominator = 1
    switch input.mode {
    case .everyDay: break
    case .everyXDays:
        if let d = Int(input.everyXDays) { denominator = d }
    case .timesPerWeek:
        if let n = Int(input.perWeek) { numerator = n; denominator = 7 }
    case .timesPerMonth:
        if let n = Int(input.perMonth) { numerator = n; denominator = 30 }
    case .timesInDays:
        if let n = Int(input.x), let d = Int(input.y) { numerator = n; denominator = d }
    }
    return (numerator >= denominator || numerator < 1) ? (1, 1) : (numerator, denominator)
}

private func initialFrequencyInput(numerator: Int, denominator: Int) -> FrequencyInput {
    if denominator == 30 || denominator == 31 {
        return FrequencyInput(mode: .timesPerMonth, perMonth: String(numerator))
    }
    if numerator == 1 && denominator == 1 { return FrequencyInput(mode: .everyDay) }
    if numerator == 1 { return FrequencyInput(mode: .everyXDays, everyXDays: String(denominator)) }
    if denominator == 7 { return FrequencyInput(mode: .timesPerWeek, perWeek: String(numerator)) }
    return FrequencyInput(mode: .timesInDays, x: String(numerator), y: String(denominator))
}

func frequencySummaryText(numerator: Int, denominator: Int) -> String {
    switch frequencyLabel(numerator: numerator, denominator: denominator) {
    case .everyMonth: return L("every_month", "Every month")
    case .timesPerMonth(let t): return LF("x_times_per_month", "%1$s times per month", t)
    case .everyDay: return L("every_day", "Every day")
    case .everyWeek: return L("every_week", "Every week")
    case .everyXDays(let d): return LF("every_x_days", "Every %1$s days", d)
    case .timesPerWeek(let t): return LF("x_times_per_week", "%1$s times per week", t)
    case .timesInDays(let t, let d): return LF("x_times_per_y_days", "%1$s times in %2$s days", t, d)
    }
}

private enum FrequencyLabelKind {
    case everyMonth, timesPerMonth(Int), everyDay, everyWeek, everyXDays(Int), timesPerWeek(Int), timesInDays(Int, Int)
}

private func frequencyLabel(numerator: Int, denominator: Int) -> FrequencyLabelKind {
    if numerator == 1 && (denominator == 30 || denominator == 31) { return .everyMonth }
    if denominator == 30 || denominator == 31 { return .timesPerMonth(numerator) }
    if numerator == 1 && denominator == 1 { return .everyDay }
    if numerator == 1 && denominator == 7 { return .everyWeek }
    if numerator == 1 && denominator > 1 { return .everyXDays(denominator) }
    if denominator == 7 { return .timesPerWeek(numerator) }
    return .timesInDays(numerator, denominator)
}

struct FrequencyPickerDialog: View {
    let numerator: Int
    let denominator: Int
    let onPicked: (Int, Int) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var input: FrequencyInput

    init(numerator: Int, denominator: Int, onPicked: @escaping (Int, Int) -> Void) {
        self.numerator = numerator
        self.denominator = denominator
        self.onPicked = onPicked
        _input = State(initialValue: initialFrequencyInput(numerator: numerator, denominator: denominator))
    }

    var body: some View {
        NavigationStack {
            Form {
                freqRow(.everyDay, label: L("every_day", "Every day"))
                freqNumberRow(
                    mode: .everyXDays,
                    format: L("every_x_days", "Every %1$s days"),
                    text: $input.everyXDays
                )
                freqNumberRow(
                    mode: .timesPerWeek,
                    format: L("x_times_per_week", "%1$s times per week"),
                    text: $input.perWeek
                )
                freqNumberRow(
                    mode: .timesPerMonth,
                    format: L("x_times_per_month", "%1$s times per month"),
                    text: $input.perMonth
                )
                timesInDaysRow
            }
            .navigationTitle(L("frequency", "Frequency"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("save", "Save")) {
                        let pair = frequencyFromInput(input)
                        onPicked(pair.0, pair.1)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.large])
    }

    private var timesInDaysRow: some View {
        HStack {
            RadioSelect(selected: input.mode == .timesInDays) { input.mode = .timesInDays }
            TextField("x", text: $input.x).keyboardType(.numberPad).frame(width: 40)
                .onTapGesture { input.mode = .timesInDays }
            Text(L("x_times_per_y_days", "%1$s times in %2$s days").replacingOccurrences(of: "%1$s", with: "").replacingOccurrences(of: "%2$s", with: ""))
            TextField("y", text: $input.y).keyboardType(.numberPad).frame(width: 40)
                .onTapGesture { input.mode = .timesInDays }
        }
    }

    private func freqRow(_ mode: FrequencyMode, label: String) -> some View {
        Button {
            input.mode = mode
        } label: {
            HStack {
                RadioSelect(selected: input.mode == mode) {}
                Text(label)
                Spacer()
            }
        }
        .buttonStyle(.plain)
    }

    private func freqNumberRow(mode: FrequencyMode, format: String, text: Binding<String>) -> some View {
        HStack {
            RadioSelect(selected: input.mode == mode) { input.mode = mode }
            TextField("n", text: text)
                .keyboardType(.numberPad)
                .frame(width: 44)
                .onTapGesture { input.mode = mode }
            Text(format.replacingOccurrences(of: "%1$s", with: "").trimmingCharacters(in: .whitespaces))
            Spacer()
        }
    }
}

private struct RadioSelect: View {
    let selected: Bool
    let onSelect: () -> Void

    var body: some View {
        Image(systemName: selected ? "largecircle.fill.circle" : "circle")
            .foregroundStyle(selected ? Color.accentColor : Color.secondary)
            .onTapGesture { onSelect() }
    }
}

// MARK: - Weekdays

func reminderWeekdayNames() -> [String] {
    let symbols = DateFormatter().shortWeekdaySymbols ?? []
    guard symbols.count == 7 else { return Array(repeating: "?", count: 7) }
    return [6, 0, 1, 2, 3, 4, 5].map { symbols[$0] }
}

struct WeekdayPickerDialog: View {
    let selectedDays: Int32
    let onPicked: (Int32) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var checked: [Bool]

    init(selectedDays: Int32, onPicked: @escaping (Int32) -> Void) {
        self.selectedDays = selectedDays
        self.onPicked = onPicked
        let mask = Int(selectedDays)
        _checked = State(initialValue: (0..<7).map { (mask & (1 << $0)) != 0 })
    }

    var body: some View {
        NavigationStack {
            List {
                ForEach(0..<7, id: \.self) { index in
                    Toggle(reminderWeekdayNames()[index], isOn: $checked[index])
                }
            }
            .navigationTitle(L("select_weekdays", "Select weekdays"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("ok", "OK")) {
                        var mask = 0
                        for index in 0..<7 where checked[index] { mask |= (1 << index) }
                        onPicked(Int32(mask))
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - Time

struct TimePickerDialog: View {
    let hour: Int
    let minute: Int
    let onPicked: (Int, Int) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var date: Date

    init(hour: Int, minute: Int, onPicked: @escaping (Int, Int) -> Void) {
        self.hour = hour
        self.minute = minute
        self.onPicked = onPicked
        var parts = DateComponents()
        parts.hour = hour
        parts.minute = minute
        let initial = Calendar.current.date(from: parts) ?? Date()
        _date = State(initialValue: initial)
    }

    var body: some View {
        NavigationStack {
            DatePicker("", selection: $date, displayedComponents: .hourAndMinute)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .navigationTitle(L("reminder", "Reminder"))
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
                    ToolbarItem(placement: .confirmationAction) {
                        Button(L("ok", "OK")) {
                            let parts = Calendar.current.dateComponents([.hour, .minute], from: date)
                            onPicked(parts.hour ?? 8, parts.minute ?? 0)
                            dismiss()
                        }
                    }
                }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - Entry popups

struct CheckmarkPopup: View {
    let title: String
    let day: Int32
    let skipEnabled: Bool
    let unknownEnabled: Bool
    let onChoose: (Int32, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var draftNotes: String

    init(
        title: String,
        day: Int32,
        notes: String,
        skipEnabled: Bool,
        unknownEnabled: Bool,
        onChoose: @escaping (Int32, String) -> Void
    ) {
        self.title = title
        self.day = day
        self.skipEnabled = skipEnabled
        self.unknownEnabled = unknownEnabled
        self.onChoose = onChoose
        _draftNotes = State(initialValue: notes)
    }

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                VStack(spacing: 2) {
                    Text(L("notes", "Notes")).font(.system(size: 17, weight: .semibold))
                    Text("\(title) \u{00B7} \(dateText)")
                        .font(.system(size: 13))
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                .padding(.horizontal, 76)
                HStack {
                    SheetCloseButton { dismiss() }
                    Spacer()
                }
            }
            .frame(height: 44)
            .padding(.top, 23)
            .padding(.horizontal, 16)
            TextField("", text: $draftNotes, prompt: Text(L("notes", "Notes")).foregroundStyle(Color.secondary), axis: .vertical)
                .font(.system(size: 17))
                .padding(.horizontal, 17)
                .padding(.top, 16)
                .frame(maxWidth: .infinity, minHeight: 148, maxHeight: 148, alignment: .topLeading)
                .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 28, style: .continuous))
                .padding(.horizontal, 20)
                .padding(.top, 16)
            HStack(spacing: 12) {
                pill(
                    symbol: "checkmark",
                    label: L("yes", "Yes"),
                    fill: dynamicColor(light: 0x5856D6, dark: 0x7D7AFF),
                    icon: dynamicColor(light: 0xFFFFFF, dark: 0x0B0B14)
                ) { choose(Int32(EntryValue.yesManual)) }
                pill(
                    symbol: "xmark",
                    label: L("no", "No"),
                    fill: dynamicColor(light: 0xDEDEE4, dark: 0x3D3D41),
                    icon: Color.primary
                ) { choose(Int32(EntryValue.no)) }
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
            if skipEnabled || unknownEnabled {
                HStack(spacing: 24) {
                    if skipEnabled {
                        Button(L("skip_day", "Skip")) { choose(Int32(EntryValue.skip)) }
                    }
                    if unknownEnabled {
                        Button(L("unknown", "Unknown")) { choose(Int32(EntryValue.unknown)) }
                    }
                }
                .font(.system(size: 15))
                .foregroundStyle(.secondary)
                .padding(.top, 12)
            }
            Spacer().frame(height: 24)
        }
        .fittedSheet()
    }

    private var dateText: String {
        let formatter = DateFormatter()
        formatter.setLocalizedDateFormatFromTemplate("EEEEMMMd")
        return formatter.string(from: date(fromDaysSince2000: Int(day)))
    }

    private func choose(_ newValue: Int32) {
        onChoose(newValue, draftNotes.trimmingCharacters(in: .whitespacesAndNewlines))
        dismiss()
    }

    private func pill(
        symbol: String,
        label: String,
        fill: Color,
        icon: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 22, weight: .semibold))
                .foregroundStyle(icon)
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(fill, in: Capsule())
                .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

struct NumberPopup: View {
    let title: String
    let day: Int32
    let unit: String
    let notes: String
    let number: Double
    let skipEnabled: Bool
    let unknownEnabled: Bool
    let onSave: (Int32, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var draftNotes: String
    @State private var amount: String
    @FocusState private var amountFocused: Bool

    init(
        title: String,
        day: Int32,
        unit: String,
        notes: String,
        number: Double,
        skipEnabled: Bool,
        unknownEnabled: Bool,
        onSave: @escaping (Int32, String) -> Void
    ) {
        self.title = title
        self.day = day
        self.unit = unit
        self.notes = notes
        self.number = number
        self.skipEnabled = skipEnabled
        self.unknownEnabled = unknownEnabled
        self.onSave = onSave
        _draftNotes = State(initialValue: notes)
        let text = number == 0 ? "" : (number == number.rounded() ? String(Int(number)) : String(number))
        _amount = State(initialValue: text)
    }

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                VStack(spacing: 2) {
                    Text(title).font(.system(size: 17, weight: .semibold)).lineLimit(1)
                    Text(dateText).font(.system(size: 13)).foregroundStyle(.secondary)
                }
                .padding(.horizontal, 76)
                HStack {
                    SheetCloseButton { dismiss() }
                    Spacer()
                    Button { saveNumber() } label: {
                        Text(L("save", "Save"))
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(dynamicColor(light: 0xFFFFFF, dark: 0x0B0B14))
                            .padding(.horizontal, 18)
                            .frame(height: 44)
                            .background(dynamicColor(light: 0x5856D6, dark: 0x7D7AFE), in: Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(height: 44)
            .padding(.top, 23)
            .padding(.horizontal, 16)
            HStack(spacing: 8) {
                TextField("", text: $amount, prompt: Text("0").foregroundStyle(Color.primary))
                    .keyboardType(.decimalPad)
                    .focused($amountFocused)
                    .font(.system(size: 17))
                if !unit.isEmpty {
                    Text(unit).font(.system(size: 17)).foregroundStyle(.secondary)
                }
            }
            .padding(.horizontal, 17)
            .frame(height: 56)
            .background(Color(.secondarySystemGroupedBackground), in: Capsule())
            .padding(.horizontal, 20)
            .padding(.top, 26)
            TextField("", text: $draftNotes, prompt: Text(L("notes", "Notes")).foregroundStyle(Color.secondary), axis: .vertical)
                .font(.system(size: 17))
                .padding(.horizontal, 17)
                .padding(.top, 16)
                .frame(maxWidth: .infinity, minHeight: 100, alignment: .topLeading)
                .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 28, style: .continuous))
                .padding(.horizontal, 20)
                .padding(.top, 16)
            if skipEnabled || unknownEnabled {
                HStack(spacing: 24) {
                    if skipEnabled {
                        Button(L("skip_day", "Skip")) { saveValue(Int32(EntryValue.skip)) }
                    }
                    if unknownEnabled {
                        Button(L("unknown", "Unknown")) { saveValue(Int32(EntryValue.unknown)) }
                    }
                }
                .font(.system(size: 15))
                .foregroundStyle(.secondary)
                .padding(.top, 12)
            }
            Spacer().frame(height: 24)
        }
        .fittedSheet()
        .task { amountFocused = true }
    }

    private var dateText: String {
        let formatter = DateFormatter()
        formatter.setLocalizedDateFormatFromTemplate("EEEEMMMd")
        return formatter.string(from: date(fromDaysSince2000: Int(day)))
    }

    private func saveNumber() {
        let parsed = Double(amount.replacingOccurrences(of: ",", with: ".")) ?? 0
        saveValue(Int32((parsed * 1000).rounded()))
    }

    private func saveValue(_ value: Int32) {
        onSave(value, draftNotes.trimmingCharacters(in: .whitespacesAndNewlines))
        dismiss()
    }
}

// MARK: - Delete

struct DeleteHabitsDialog: View {
    let count: Int
    let onDelete: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 20) {
            Text(title).font(.headline)
            Text(message).multilineTextAlignment(.center).foregroundStyle(.secondary)
            HStack(spacing: 16) {
                Button { dismiss() } label: {
                    Text(L("cancel", "Cancel")).frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                Button(role: .destructive) {
                    onDelete()
                    dismiss()
                } label: {
                    Text(L("delete", "Delete")).frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
            }
            .controlSize(.large)
        }
        .padding(24)
        .presentationDetents([.height(220)])
    }

    private var title: String {
        count == 1 ? L("delete_habit_title", "Delete habit?") : L("delete_habits_title", "Delete habits?")
    }

    private var message: String {
        count == 1
            ? L("delete_habit_message", "The habit will be permanently deleted. This action cannot be undone.")
            : L("delete_habits_message", "The habits will be permanently deleted. This action cannot be undone.")
    }
}

struct EntryPopupTarget: Identifiable {
    let id = UUID()
    let row: HabitRow
    let cell: DayCell
    let numerical: Bool
}
