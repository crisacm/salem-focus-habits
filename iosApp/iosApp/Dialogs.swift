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

// MARK: - Habit type

struct HabitTypeDialog: View {
    let onSelected: (Int32) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Button {
                    onSelected(0)
                    dismiss()
                } label: {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(L("yes_or_no", "Yes or No")).font(.headline)
                        Text(L("yes_or_no_example", "e.g. Did you exercise today?")).font(.subheadline).foregroundStyle(.secondary)
                    }
                }
                Button {
                    onSelected(1)
                    dismiss()
                } label: {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(L("measurable", "Measurable")).font(.headline)
                        Text(L("measurable_example", "e.g. How many miles did you run today? How many pages did you read?"))
                            .font(.subheadline).foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle(L("type", "Type"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
            }
        }
        .presentationDetents([.medium, .large])
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
    let habitColor: Color
    let notes: String
    let value: Int32
    let skipEnabled: Bool
    let unknownEnabled: Bool
    let onChoose: (Int32, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var draftNotes: String

    init(
        habitColor: Color,
        notes: String,
        value: Int32,
        skipEnabled: Bool,
        unknownEnabled: Bool,
        onChoose: @escaping (Int32, String) -> Void
    ) {
        self.habitColor = habitColor
        self.notes = notes
        self.value = value
        self.skipEnabled = skipEnabled
        self.unknownEnabled = unknownEnabled
        self.onChoose = onChoose
        _draftNotes = State(initialValue: notes)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                TextField(L("notes", "Notes"), text: $draftNotes, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)
                HStack {
                    popupButton(L("yes", "Yes"), color: habitColor) {
                        choose(Int32(EntryValue.yesManual))
                    }
                    popupButton(L("no", "No"), color: .secondary) {
                        choose(Int32(EntryValue.no))
                    }
                    if skipEnabled {
                        popupButton(L("skip_day", "Skip"), color: habitColor) {
                            choose(Int32(EntryValue.skip))
                        }
                    }
                    if unknownEnabled {
                        popupButton(L("unknown", "Unknown"), color: .secondary) {
                            choose(Int32(EntryValue.unknown))
                        }
                    }
                }
                .padding(.horizontal)
            }
            .padding(.vertical)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
            }
        }
        .presentationDetents([.medium])
    }

    private func choose(_ newValue: Int32) {
        onChoose(newValue, draftNotes.trimmingCharacters(in: .whitespacesAndNewlines))
        dismiss()
    }

    private func popupButton(_ title: String, color: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).font(.caption).foregroundStyle(color).frame(maxWidth: .infinity)
        }
        .buttonStyle(.bordered)
    }
}

struct NumberPopup: View {
    let notes: String
    let number: Double
    let skipEnabled: Bool
    let unknownEnabled: Bool
    let onSave: (Int32, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var draftNotes: String
    @State private var amount: String

    init(
        notes: String,
        number: Double,
        skipEnabled: Bool,
        unknownEnabled: Bool,
        onSave: @escaping (Int32, String) -> Void
    ) {
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
        NavigationStack {
            VStack(spacing: 16) {
                TextField(L("notes", "Notes"), text: $draftNotes, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)
                TextField(L("value", "Value"), text: $amount)
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.center)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)
                HStack {
                    Button(L("save", "Save")) { saveNumber() }
                        .buttonStyle(.borderedProminent)
                    if skipEnabled {
                        Button(L("skip_day", "Skip")) { saveValue(Int32(EntryValue.skip)) }
                    }
                    if unknownEnabled {
                        Button(L("unknown", "Unknown")) { saveValue(Int32(EntryValue.unknown)) }
                    }
                }
                .padding(.horizontal)
            }
            .padding(.vertical)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("cancel", "Cancel")) { dismiss() } }
            }
        }
        .presentationDetents([.medium])
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
            HStack {
                Button(L("cancel", "Cancel")) { dismiss() }
                Button(L("delete", "Delete"), role: .destructive) {
                    onDelete()
                    dismiss()
                }
            }
        }
        .padding(24)
        .presentationDetents([.height(200)])
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
