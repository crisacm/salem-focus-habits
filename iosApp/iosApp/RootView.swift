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

struct RootView: View {
    @State private var model = SessionModel()

    var body: some View {
        NavigationStack {
            ListScreen(model: model)
                .navigationDestination(item: $model.detailId) { id in
                    DetailScreen(model: model, habitId: id)
                }
        }
        .sheet(item: $model.editor) { form in
            EditorScreen(model: model, form: form)
        }
        .sheet(isPresented: $model.showSettings) { SettingsScreen(model: model) }
        .sheet(isPresented: $model.showAbout) { AboutScreen(model: model) }
        .fullScreenCover(isPresented: $model.showIntro) { IntroScreen { model.showIntro = false } }
        .alert(L("message", "Message"), isPresented: messageShown) {
            Button(L("ok", "OK"), role: .cancel) { model.message = nil }
        } message: {
            Text(model.message ?? "")
        }
        .onAppear { model.load() }
        .preferredColorScheme(scheme)
    }

    private var scheme: ColorScheme? {
        switch model.settings?.theme {
        case 1: return .dark
        case 2: return .light
        default: return nil
        }
    }

    private var messageShown: Binding<Bool> {
        Binding(get: { model.message != nil }, set: { if !$0 { model.message = nil } })
    }
}

extension EditorSnapshot: @retroactive Identifiable {}
extension Int64: @retroactive Identifiable { public var id: Int64 { self } }
