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
    case type, color, frequency, weekdays, time
    var id: Self { self }
}

struct EditorScreen: View {
    @Bindable var model: SessionModel
    @State var form: EditorSnapshot
    @State private var attempted = false
    @State private var picker: EditorPicker?
    private let blankMessage = L("validation_cannot_be_blank", "Cannot be blank")

    var body: some View {
        NavigationStack {
            Form {
                Button { picker = .type } label: {
                    LabeledContent(L("type", "Type"), value: form.type == 1 ? L("numerical", "Numerical") : L("yes_or_no", "Yes or No"))
                }
                TextField(L("name", "Name"), text: name)
                if nameShowsBlankError { Text(blankMessage).foregroundStyle(.red).font(.caption) }
                TextField(L("question", "Question"), text: question)
                TextField(L("notes", "Notes"), text: notes)
                Button { picker = .frequency } label: {
                    LabeledContent(
                        L("frequency", "Frequency"),
                        value: frequencySummaryText(numerator: Int(form.freqNum), denominator: Int(form.freqDen))
                    )
                }
                if form.type == 1 {
                    TextField(L("target", "Target"), text: target)
                        .keyboardType(.decimalPad)
                    if targetShowsBlankError { Text(blankMessage).foregroundStyle(.red).font(.caption) }
                    TextField(L("unit", "Unit"), text: unit)
                    Picker(L("target", "Target"), selection: targetType) {
                        Text(L("at_least", "At least")).tag(Int32(0))
                        Text(L("at_most", "At most")).tag(Int32(1))
                    }
                }
                Button { picker = .color } label: {
                    LabeledContent(L("color", "Color")) {
                        Circle().fill(editorPalette(Int(form.color))).frame(width: 24, height: 24)
                    }
                }
                Toggle(L("reminder", "Reminder"), isOn: reminderOn)
                if form.reminderHour >= 0 {
                    Button { picker = .time } label: {
                        LabeledContent(L("reminder", "Reminder"), value: reminderTimeText)
                    }
                    Button { picker = .weekdays } label: {
                        LabeledContent(L("select_weekdays", "Select weekdays"), value: weekdaySummary)
                    }
                }
            }
            .navigationTitle(form.id < 0 ? L("create_habit", "Create habit") : L("edit", "Edit"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L("cancel", "Cancel")) { model.editor = nil }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("save", "Save")) { save() }
                }
            }
            .sheet(item: $picker) { active in
                switch active {
                case .type:
                    HabitTypeDialog { form = copy(type: $0) }
                case .color:
                    ColorPickerDialog(selected: form.color) { form = copy(color: $0) }
                case .frequency:
                    FrequencyPickerDialog(numerator: Int(form.freqNum), denominator: Int(form.freqDen)) { n, d in
                        form = copy(freqNum: Int32(n), freqDen: Int32(d))
                    }
                case .weekdays:
                    WeekdayPickerDialog(selectedDays: form.reminderDays) { form = copy(reminderDays: $0) }
                case .time:
                    TimePickerDialog(hour: Int(form.reminderHour), minute: Int(form.reminderMin)) { h, m in
                        form = copy(reminderHour: Int32(h), reminderMin: Int32(m))
                    }
                }
            }
        }
    }

    private var reminderOn: Binding<Bool> {
        Binding(
            get: { form.reminderHour >= 0 },
            set: { on in
                form = copy(reminderHour: on ? 8 : -1, reminderMin: on ? 0 : -1, reminderDays: on ? form.reminderDays : 0)
            }
        )
    }

    private var reminderTimeText: String {
        let hour = Int(form.reminderHour)
        let minute = Int(form.reminderMin)
        var parts = DateComponents()
        parts.hour = hour
        parts.minute = minute
        let date = Calendar.current.date(from: parts) ?? Date()
        return DateFormatter.localizedString(from: date, dateStyle: .none, timeStyle: .short)
    }

    private var weekdaySummary: String {
        let mask = Int(form.reminderDays)
        let names = reminderWeekdayNames().enumerated().compactMap { index, name in
            (mask & (1 << index)) != 0 ? name : nil
        }
        return names.isEmpty ? L("none", "None") : names.joined(separator: ", ")
    }

    private func save() {
        attempted = true
        let id = model.saveEditor(form: form)
        if id == -1 || id == -3 { return }
        model.editor = nil
        if id >= 0 {
            model.reload()
            if model.hasReminders() { requestNotifications(model) }
        }
    }

    private var nameShowsBlankError: Bool {
        attempted && form.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    private var targetShowsBlankError: Bool {
        guard attempted, form.type == 1 else { return false }
        let text = form.targetText.trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: ".")
        guard let value = Double(text) else { return true }
        return !value.isFinite
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
