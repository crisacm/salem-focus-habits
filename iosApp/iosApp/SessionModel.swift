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
import WidgetKit

/// Owns the Kotlin session and the snapshots the screens draw. The session is opened once.
@MainActor
@Observable
final class SessionModel {
    private let sessionQueueKey = DispatchSpecificKey<UInt8>()
    private let sessionQueue: DispatchQueue
    private nonisolated(unsafe) var session: SalemSession?

    private(set) var list: ListSnapshot?
    private(set) var settings: SettingsSnapshot?
    var loadError: String?
    var showIntro = false
    var message: String?

    var editor: EditorSnapshot?
    var detailId: Int64?
    var showSettings = false
    var showAbout = false
    var dayCount = 60

    private var loadStarted = false
    private var sessionReady = false
    private var pendingRefresh = false
    private var lifecycleObservers: [NSObjectProtocol] = []

    init() {
        let queue = DispatchQueue(label: "io.github.crisacm.salem.session")
        queue.setSpecific(key: sessionQueueKey, value: 1)
        sessionQueue = queue
    }

    func load() {
        guard !loadStarted, loadError == nil else { return }
        loadStarted = true
        let body = L("default_reminder_question", "Did you?")
        let yes = L("yes", "Yes")
        let no = L("no", "No")
        sessionQueue.async { [weak self] in
            guard let self else { return }
            do {
                let opened = try SalemSessionFactory.shared.open(
                    defaultBody: body,
                    yes: yes,
                    no: no,
                    onHabitsChanged: { WidgetCenter.shared.reloadAllTimelines() }
                )
                self.session = opened
                opened.addListener { [weak self] in
                    DispatchQueue.main.async {
                        Task { @MainActor in
                            guard let self else { return }
                            if self.sessionReady {
                                self.reload()
                            } else {
                                self.pendingRefresh = true
                            }
                        }
                    }
                }
                let intro = opened.startup()
                var pending: Int64?
                if opened.pendingHabitId >= 0 {
                    pending = opened.pendingHabitId
                    opened.pendingHabitId = -1
                }
                DispatchQueue.main.async {
                    self.sessionReady = true
                    self.showIntro = intro
                    if let pending { self.detailId = pending }
                    self.registerLifecycleObservers()
                    self.reload()
                    if self.pendingRefresh {
                        self.pendingRefresh = false
                        self.reload()
                    }
                }
            } catch {
                DispatchQueue.main.async { self.loadError = String(describing: error) }
            }
        }
    }

    private func registerLifecycleObservers() {
        guard lifecycleObservers.isEmpty else { return }
        lifecycleObservers.append(
            NotificationCenter.default.addObserver(
                forName: UIApplication.willEnterForegroundNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                Task { @MainActor in self?.appForeground() }
            }
        )
        lifecycleObservers.append(
            NotificationCenter.default.addObserver(
                forName: UIApplication.didEnterBackgroundNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                Task { @MainActor in self?.appBackground() }
            }
        )
    }

    private func withSession<T>(_ body: (SalemSession) -> T) -> T? {
        if DispatchQueue.getSpecific(key: sessionQueueKey) != nil {
            guard let session else { return nil }
            return body(session)
        }
        var result: T?
        sessionQueue.sync {
            guard let session else { return }
            result = body(session)
        }
        return result
    }

    /// Like `withSession`, but when the session exists the closure may return `nil` without conflating that with a missing session.
    private func withSessionOptional<T>(_ body: (SalemSession) -> T?) -> T? {
        if DispatchQueue.getSpecific(key: sessionQueueKey) != nil {
            guard let session else { return nil }
            return body(session)
        }
        var missingSession = true
        var value: T?
        sessionQueue.sync {
            guard let session else { return }
            missingSession = false
            value = body(session)
        }
        guard !missingSession else { return nil }
        return value
    }

    func reload() {
        let isDark = UITraitCollection.current.userInterfaceStyle == .dark
        let days = dayCount
        let outcome: (ListSnapshot, SettingsSnapshot, Int64?)? = withSession { session in
            session.systemIsDark = isDark
            let snapshot = session.listSnapshot(dayCount: Int32(days))
            let settings = session.settingsSnapshot()
            var pending: Int64?
            if session.pendingHabitId >= 0 {
                pending = session.pendingHabitId
                session.pendingHabitId = -1
            }
            return (snapshot, settings, pending)
        }
        guard let outcome else { return }
        list = outcome.0
        settings = outcome.1
        if let pending = outcome.2 { detailId = pending }
    }

    func appForeground() {
        withSession { $0.foreground() }
        reload()
    }

    func appBackground() {
        withSession { $0.background() }
    }

    func blankEditor(type: Int32) -> EditorSnapshot? {
        withSessionOptional { $0.blankEditor(type: type) }
    }

    func cyclePrimaryOrder() {
        withSession { $0.cyclePrimaryOrder() }
        reload()
    }

    func setShowArchived(show: Bool) {
        withSession { $0.setShowArchived(show: show) }
        reload()
    }

    func setShowCompleted(show: Bool) {
        withSession { $0.setShowCompleted(show: show) }
        reload()
    }

    func archive(ids: [KotlinLong]) {
        withSession { $0.archive(ids: ids) }
        reload()
    }

    func unarchive(ids: [KotlinLong]) {
        withSession { $0.unarchive(ids: ids) }
        reload()
    }

    func delete(ids: [KotlinLong]) {
        withSession { $0.delete(ids: ids) }
        reload()
    }

    func toggle(_ id: Int64, day: Int32) {
        withSession { $0.toggle(id: id, day: day) }
    }

    func setEntry(_ id: Int64, day: Int32, value: Int32, notes: String) {
        withSession { $0.setEntry(id: id, day: day, value: value, notes: notes) }
    }

    func detail(id: Int64) -> DetailSnapshot? {
        withSessionOptional { $0.detail(id: id) }
    }

    func detailCharts(id: Int64) -> DetailCharts? {
        withSessionOptional { $0.detailCharts(id: id) }
    }

    func editorSnapshot(id: Int64) -> EditorSnapshot? {
        withSessionOptional { $0.editorSnapshot(id: id) }
    }

    func history(id: Int64, days: Int32) -> [HistoryDay] {
        let list = withSession { $0.history(id: id, days: days) }
        guard let list else { return [] }
        return list.compactMap { $0 as? HistoryDay }
    }

    func chartOps(
        id: Int64,
        card: Int32,
        width: Double,
        height: Double,
        offset: Int32,
        labels: [String]
    ) -> [DrawOp] {
        withSession {
            $0.chartOps(
                id: id,
                card: card,
                width: width,
                height: height,
                offset: offset,
                labels: labels
            )
        } ?? []
    }

    func chartHeight(id: Int64, card: Int32) -> Double {
        withSession { $0.chartHeight(id: id, card: card) } ?? 0
    }

    func chartTap(
        id: Int64,
        card: Int32,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        offset: Int32
    ) -> Int32 {
        withSession {
            $0.chartTap(
                id: id,
                card: card,
                x: x,
                y: y,
                width: width,
                height: height,
                offset: offset
            )
        } ?? -1
    }

    func setScoreSpinner(position: Int32) {
        withSession { $0.setScoreSpinner(position: position) }
        reload()
    }

    func setBarSpinner(numerical: Bool, position: Int32) {
        withSession { $0.setBarSpinner(numerical: numerical, position: position) }
        reload()
    }

    func saveEditor(form: EditorSnapshot) -> Int64 {
        withSession { $0.saveEditor(form: form) } ?? -1
    }

    func hasReminders() -> Bool {
        withSession { $0.hasReminders() } ?? false
    }

    func rescheduleReminders() {
        withSession { $0.rescheduleReminders() }
    }

    func setSwitch(key: Int32, value: Bool) {
        withSession { $0.setSwitch(key: key, value: value) }
        reload()
    }

    func setFirstWeekday(day: Int32) {
        withSession { $0.setFirstWeekday(day: day) }
        reload()
    }

    func setTheme(theme: Int32) {
        withSession { $0.setTheme(theme: theme) }
        reload()
    }

    func repairDatabase() {
        withSession { $0.repairDatabase() }
        reload()
    }

    func importFile(path: String) -> Int32 {
        withSession { (try? $0.importFile(path: path)) ?? 2 } ?? 2
    }

    func databasePath() -> String? {
        withSessionOptional { $0.databasePath() }
    }

    func exportCsvBase64() -> String? {
        withSessionOptional { session in
            try? session.exportCsvBase64()
        }
    }
}
