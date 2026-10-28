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
import UserNotifications

private enum EditorPicker: Identifiable {
    case color, frequency, time
    var id: Self { self }
}

struct EditorScreen: View {
    @Bindable var model: SessionModel
    @State var form: EditorSnapshot
    @State private var picker: EditorPicker?
    /// The numbers the frequency dialog starts from.
    @State private var frequencySeed: (Int, Int) = (1, 1)
    @FocusState private var nameFocused: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker("", selection: typeBinding) {
                        Text(L("yes_no_option", "Yes / No")).tag(Int32(0))
                        Text(L("measurable", "Measurable")).tag(Int32(1))
                    }
                    .pickerStyle(.segmented)
                    .labelsHidden()
                    .controlSize(.large)
                    .frame(height: 44)
                }
                .listRowInsets(EdgeInsets())
                .listRowBackground(Color.clear)

                Section {
                    TextField(L("name", "Name"), text: name)
                        .focused($nameFocused)
                        .formRow()
                    TextField(L("question", "Question"), text: question)
                        .formRow()
                }
                if form.type == 1 {
                    Section {
                        TextField(L("unit", "Unit"), text: unit)
                            .formRow()
                        TextField(L("target", "Target"), text: target)
                            .keyboardType(.decimalPad)
                            .formRow()
                        Picker(L("target_type", "Target type"), selection: targetType) {
                            Text(L("target_type_at_least", "At least")).tag(Int32(0))
                            Text(L("target_type_at_most", "At most")).tag(Int32(1))
                        }
                        .pickerStyle(.menu)
                        .formRow()
                    }
                }
                Section {
                    HStack(spacing: 0) {
                        Button { picker = .color } label: {
                            Text(L("color", "Color")).foregroundStyle(Color.primary)
                        }
                        .buttonStyle(.plain)
                        Spacer(minLength: 8)
                        ColorSwatchRow(selected: Int(form.color)) { form = copy(color: Int32($0)) }
                            .padding(.trailing, -9.5)
                    }
                    .formRow()
                }
                Section {
                    frequencyMenu.formRow()
                }
                Section {
                    reminderRow.formRow(height: 64)
                    if form.reminderHour >= 0 {
                        Button { picker = .time } label: {
                            HStack {
                                Text(L("reminder_time", "Time")).foregroundStyle(Color.primary)
                                Spacer()
                                Text(reminderTimeText).foregroundStyle(.secondary)
                            }
                        }
                        .buttonStyle(.plain)
                        .formRow()
                        WeekdayCircles(days: Int(form.reminderDays), tone: tone) { form = copy(reminderDays: Int32($0)) }
                            .frame(height: 57)
                            .listRowInsets(EdgeInsets(top: 0, leading: 15, bottom: 0, trailing: 15))
                    }
                }
                Section {
                    ZStack(alignment: .topLeading) {
                        TextEditor(text: notes)
                            .scrollContentBackground(.hidden)
                            .padding(.top, 8)
                            .padding(.leading, -5)
                        if form.notes.isEmpty {
                            Text(L("notes", "Notes"))
                                .foregroundStyle(Color(.placeholderText))
                                .padding(.top, 16)
                                .allowsHitTesting(false)
                        }
                    }
                    .frame(height: 120)
                    .listRowInsets(EdgeInsets(top: 0, leading: 17.5, bottom: 0, trailing: 17.5))
                }
            }
            .listSectionSpacing(20)
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(form.id < 0 ? L("new_habit", "New Habit") : L("edit_habit", "Edit habit"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    EditorCloseButton { model.editor = nil }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("save", "Save")) { save() }
                        .fontWeight(.semibold)
                        .disabled(!isValid)
                }
            }
            .sheet(item: $picker) { active in
                switch active {
                case .color:
                    ColorPickerDialog(selected: form.color) { form = copy(color: $0) }
                case .frequency:
                    FrequencyPickerDialog(numerator: frequencySeed.0, denominator: frequencySeed.1) { n, d in
                        form = copy(freqNum: Int32(n), freqDen: Int32(d))
                    }
                case .time:
                    TimePickerDialog(hour: Int(form.reminderHour), minute: Int(form.reminderMin)) { h, m in
                        form = copy(reminderHour: Int32(h), reminderMin: Int32(m))
                    }
                }
            }
            .onAppear { if form.id < 0 { nameFocused = true } }
        }
    }

    private var tone: HabitTone {
        habitTone(palette: Int(form.color), colorArgb: paletteArgb(Int(form.color)))
    }

    private var frequencyMenu: some View {
        Menu {
            Button(L("every_day", "Every day")) { form = copy(freqNum: 1, freqDen: 1) }
            Button(L("per_week", "Per week")) {
                frequencySeed = form.freqDen == 7 ? (Int(form.freqNum), 7) : (3, 7)
                picker = .frequency
            }
            Button(L("custom", "Custom")) {
                frequencySeed = (Int(form.freqNum), Int(form.freqDen))
                picker = .frequency
            }
        } label: {
            HStack {
                Text(L("frequency", "Frequency")).foregroundStyle(Color.primary)
                Spacer()
                Text(frequencySummaryText(numerator: Int(form.freqNum), denominator: Int(form.freqDen)))
                    .foregroundStyle(.secondary)
                Image(systemName: "chevron.up.chevron.down")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.secondary)
            }
            .contentShape(Rectangle())
        }
    }

    private var reminderRow: some View {
        Toggle(isOn: reminderOn) {
            VStack(alignment: .leading, spacing: 2) {
                Text(L("reminder", "Reminder"))
                Text(form.reminderHour >= 0 ? reminderTimeText : L("reminder_off", "Off"))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var typeBinding: Binding<Int32> {
        Binding(get: { form.type }, set: { changeType($0) })
    }

    /// A numerical habit only has a frequency per day, week or month, so another one becomes every day.
    private func changeType(_ newType: Int32) {
        guard newType != form.type else { return }
        if newType == 1 {
            let denominator = [1, 7, 30].contains(Int(form.freqDen)) ? form.freqDen : 1
            form = copy(type: newType, freqNum: 1, freqDen: denominator)
        } else {
            form = copy(type: newType)
        }
    }

    private var reminderOn: Binding<Bool> {
        Binding(
            get: { form.reminderHour >= 0 },
            set: { on in
                form = copy(
                    reminderHour: on ? 8 : -1,
                    reminderMin: on ? 0 : -1,
                    reminderDays: form.reminderDays == 0 ? 127 : form.reminderDays
                )
            }
        )
    }

    private var reminderTimeText: String {
        var parts = DateComponents()
        parts.hour = Int(form.reminderHour)
        parts.minute = Int(form.reminderMin)
        let date = Calendar.current.date(from: parts) ?? Date()
        return DateFormatter.localizedString(from: date, dateStyle: .none, timeStyle: .short)
    }

    private func save() {
        let id = model.saveEditor(form: form)
        if id == -1 || id == -3 { return }
        model.editor = nil
        if id >= 0 {
            model.reload()
            if model.hasReminders() { requestNotifications(model) }
        }
    }

    /// A habit needs a name, and a numerical one a target that is a number.
    private var isValid: Bool {
        guard !form.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return false }
        guard form.type == 1 else { return true }
        let text = form.targetText.trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: ".")
        guard let value = Double(text) else { return false }
        return value.isFinite
    }

    private var name: Binding<String> { Binding(get: { form.name }, set: { form = copy(name: $0) }) }
    private var question: Binding<String> { Binding(get: { form.question }, set: { form = copy(question: $0) }) }
    private var notes: Binding<String> { Binding(get: { form.notes }, set: { form = copy(notes: $0) }) }
    private var unit: Binding<String> { Binding(get: { form.unit }, set: { form = copy(unit: $0) }) }
    private var target: Binding<String> { Binding(get: { form.targetText }, set: { form = copy(targetText: $0) }) }
    private var targetType: Binding<Int32> { Binding(get: { form.targetType }, set: { form = copy(targetType: $0) }) }

    private func copy(
        type: Int32? = nil,
        name: String? = nil,
        question: String? = nil,
        notes: String? = nil,
        unit: String? = nil,
        targetText: String? = nil,
        targetType: Int32? = nil,
        color: Int32? = nil,
        freqNum: Int32? = nil,
        freqDen: Int32? = nil,
        reminderHour: Int32? = nil,
        reminderMin: Int32? = nil,
        reminderDays: Int32? = nil
    ) -> EditorSnapshot {
        EditorSnapshot(
            id: form.id,
            type: type ?? form.type,
            name: name ?? form.name,
            question: question ?? form.question,
            notes: notes ?? form.notes,
            unit: unit ?? form.unit,
            targetText: targetText ?? form.targetText,
            targetType: targetType ?? form.targetType,
            color: color ?? form.color,
            freqNum: freqNum ?? form.freqNum,
            freqDen: freqDen ?? form.freqDen,
            reminderHour: reminderHour ?? form.reminderHour,
            reminderMin: reminderMin ?? form.reminderMin,
            reminderDays: reminderDays ?? form.reminderDays
        )
    }
}

func requestNotifications(_ model: SessionModel) {
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
        if granted {
            DispatchQueue.main.async { model.rescheduleReminders() }
        }
    }
}
