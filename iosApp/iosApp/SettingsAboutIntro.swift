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
import UniformTypeIdentifiers

struct SettingsScreen: View {
    @Bindable var model: SessionModel
    @State private var importer = false
    @State private var share: URL?

    var body: some View {
        NavigationStack {
            Form {
                if model.settings != nil {
                    Toggle(L("pref_toggle_title", "Toggle with short press"), isOn: bind(0))
                    Toggle(L("pref_midnight_delay_title", "Extend day past midnight"), isOn: bind(1))
                    Toggle(L("pref_skip_title", "Enable skip days"), isOn: bind(2))
                    Toggle(L("pref_unknown_title", "Show question marks"), isOn: bind(3))
                    Toggle(L("reverse_days", "Reverse order of days"), isOn: bind(4))
                    Toggle(L("use_pure_black", "Use pure black"), isOn: bind(5))
                    Toggle(L("pref_animations_title", "Disable animations"), isOn: bind(6))
                    Toggle(L("sticky_notifications", "Sticky notifications"), isOn: bind(7))
                    Picker(L("first_weekday", "First day of the week"), selection: weekday()) {
                        ForEach(0..<7, id: \.self) { day in
                            Text(weekdayName(day)).tag(Int32(day))
                        }
                    }
                    Picker(L("theme", "Theme"), selection: theme()) {
                        Text(L("theme_automatic", "Automatic")).tag(Int32(0))
                        Text(L("theme_dark", "Dark")).tag(Int32(1))
                        Text(L("theme_light", "Light")).tag(Int32(2))
                    }
                    Picker(L("language", "Language"), selection: language()) {
                        Text(L("language_system", "System default")).tag("")
                        Text("English").tag("en")
                        Text("Espa\u{00F1}ol").tag("es")
                    }
                    Button(L("import_data", "Import data")) { importer = true }
                    Button(L("export_full_backup", "Export full backup")) { exportDatabase() }
                    Button(L("export_to_csv", "Export as CSV")) { exportCsv() }
                    Button(L("repair_database", "Repair database")) { model.repairDatabase() }
                    Button(L("customize_notification", "Notifications")) {
                        if let url = URL(string: UIApplication.openNotificationSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    }
                }
            }
            .navigationTitle(L("settings", "Settings"))
            .toolbar { Button(L("done", "Done")) { model.showSettings = false } }
        }
        .fileImporter(isPresented: $importer, allowedContentTypes: [.data, .commaSeparatedText]) { result in
            if case let .success(url) = result {
                let access = url.startAccessingSecurityScopedResource()
                let code = model.importFile(path: url.path)
                if access { url.stopAccessingSecurityScopedResource() }
                model.message = code == 0 ? L("import_success", "Imported") : L("import_failed", "Could not import")
                model.reload()
            }
        }
        .sheet(item: $share) { url in
            ShareSheet(url: url)
        }
    }

    private func bind(_ key: Int32) -> Binding<Bool> {
        Binding(
            get: {
                guard let settings = model.settings else { return false }
                switch key {
                case 0: return settings.shortToggle
                case 1: return settings.midnightDelay
                case 2: return settings.skip
                case 3: return settings.unknown
                case 4: return settings.reverseDays
                case 5: return settings.pureBlack
                case 6: return settings.disableAnimation
                case 7: return settings.stickyNotifications
                default: return false
                }
            },
            set: { model.setSwitch(key: key, value: $0) }
        )
    }

    private func weekday() -> Binding<Int32> {
        Binding(
            get: { model.settings?.firstWeekday ?? 0 },
            set: { model.setFirstWeekday(day: $0) }
        )
    }

    private func theme() -> Binding<Int32> {
        Binding(
            get: { model.settings?.theme ?? 0 },
            set: { model.setTheme(theme: $0) }
        )
    }

    private func language() -> Binding<String> {
        Binding(
            get: { model.settings?.language ?? "" },
            set: { model.setLanguage(language: $0) }
        )
    }

    private func weekdayName(_ day: Int) -> String {
        let symbols = appCalendar().weekdaySymbols
        return symbols[min(day, symbols.count - 1)]
    }

    private func exportDatabase() {
        guard let path = model.databasePath(), !path.isEmpty else { return }
        let dest = FileManager.default.temporaryDirectory.appendingPathComponent("Salem.db")
        try? FileManager.default.removeItem(at: dest)
        if (try? FileManager.default.copyItem(at: URL(fileURLWithPath: path), to: dest)) != nil {
            share = dest
        }
    }

    private func exportCsv() {
        guard let encoded = model.exportCsvBase64(),
              let data = Data(base64Encoded: encoded) else { return }
        let dest = FileManager.default.temporaryDirectory.appendingPathComponent("Salem.csv.zip")
        try? data.write(to: dest)
        share = dest
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    let url: URL
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: [url], applicationActivities: nil)
    }
    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}

extension URL: @retroactive Identifiable {
    public var id: String { path }
}

struct AboutScreen: View {
    @Bindable var model: SessionModel
    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("Salem").font(.title2)
                    Text(L("about_description", "A habit tracker. Logic in Kotlin, this screen in SwiftUI."))
                }
                Section(L("links", "Links")) {
                    Link("GitHub", destination: URL(string: "https://github.com/crisacm/salem-focus-habits")!)
                }
                Section(L("credits", "Credits")) {
                    Text("Loop Habit Tracker by Álinson Santos Xavier")
                    Text("GPLv3")
                }
            }
            .navigationTitle(L("about", "About"))
            .toolbar { Button(L("done", "Done")) { model.showAbout = false } }
        }
    }
}

struct IntroScreen: View {
    let dismiss: () -> Void
    @State private var page = 0
    private let titles = ["intro_title_1", "intro_title_2", "intro_title_3", "intro_title_4"]
    private let bodies = ["intro_description_1", "intro_description_2", "intro_description_3", "intro_description_4"]
    private let fallbacks = [
        ("Welcome", "Track your habits, one day at a time."),
        ("Create habits", "Yes or no, or a number with a target."),
        ("See your progress", "Scores, streaks and history stay on this device."),
        ("That's it", "No account. Your data stays with you.")
    ]

    var body: some View {
        VStack {
            TabView(selection: $page) {
                ForEach(0..<4, id: \.self) { index in
                    VStack(spacing: 16) {
                        Text(L(titles[index], fallbacks[index].0)).font(.title)
                        Text(L(bodies[index], fallbacks[index].1))
                            .multilineTextAlignment(.center)
                            .foregroundStyle(.secondary)
                    }
                    .padding()
                    .tag(index)
                }
            }
            .tabViewStyle(.page)
            Button(page == 3 ? L("done", "Done") : L("next", "Next")) {
                if page == 3 { dismiss() } else { page += 1 }
            }
            .buttonStyle(.borderedProminent)
            .padding()
        }
    }
}
