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

/// The colours of the charts of a habit: the accent, the softer colour of the bars that are not highlighted and the area under a line.
struct ChartPalette {
    let accent: Color
    var muted: Color { accent.opacity(0.6) }
    var area: Color { accent.opacity(0.12) }
    var line: Color { Color(.systemGray5) }
}

/// The first day of each month that shows in the range, with its short name.
private func monthMarks(from start: Int32, to end: Int32) -> [(day: Int32, text: String)] {
    let calendar = Calendar(identifier: .gregorian)
    let formatter = appDateFormatter()
    formatter.setLocalizedDateFormatFromTemplate("MMM")
    var marks: [(Int32, String)] = []
    var components = calendar.dateComponents([.year, .month], from: date(fromDaysSince2000: Int(start)))
    components.day = 1
    guard var month = calendar.date(from: components) else { return [] }
    while dayNumber(of: month) <= end {
        let day = dayNumber(of: month)
        marks.append((max(day, start), formatter.string(from: month)))
        guard let next = calendar.date(byAdding: .month, value: 1, to: month) else { break }
        month = next
    }
    return marks
}

private func smoothPath(_ points: [CGPoint]) -> Path {
    var path = Path()
    guard points.count > 1 else { return path }
    path.move(to: points[0])
    for index in 0..<(points.count - 1) {
        let p0 = points[max(index - 1, 0)]
        let p1 = points[index]
        let p2 = points[index + 1]
        let p3 = points[min(index + 2, points.count - 1)]
        path.addCurve(
            to: p2,
            control1: CGPoint(x: p1.x + (p2.x - p0.x) / 6, y: p1.y + (p2.y - p0.y) / 6),
            control2: CGPoint(x: p2.x - (p3.x - p1.x) / 6, y: p2.y - (p3.y - p1.y) / 6)
        )
    }
    return path
}

private func label(_ text: String, size: CGFloat = 11, weight: Font.Weight = .regular) -> Text {
    Text(text).font(.system(size: size, weight: weight)).foregroundStyle(Color.secondary)
}

// MARK: - Score

/// The score as a smooth line over an area, between 20 % and 100 %, with the marker of today at the end.
struct ScoreLineChart: View {
    let points: [ScorePoint]
    /// The score of today, where the line ends.
    let now: Double
    let start: Int32
    let end: Int32
    let palette: ChartPalette

    private let plotHeight: CGFloat = 151
    private let axisWidth: CGFloat = 38

    var body: some View {
        Canvas { context, size in
            let left: CGFloat = 0
            let right = size.width - axisWidth
            let top: CGFloat = 6
            let bottom = top + plotHeight
            func y(_ value: Double) -> CGFloat {
                top + CGFloat((1 - min(max(value, 0.2), 1)) / 0.8) * plotHeight
            }
            func x(_ day: Int32) -> CGFloat {
                let span = max(CGFloat(end - start), 1)
                return left + CGFloat(day - start) / span * (right - left)
            }
            for tick in stride(from: 20, through: 100, by: 20) {
                let line = y(Double(tick) / 100)
                var path = Path()
                path.move(to: CGPoint(x: left, y: line))
                path.addLine(to: CGPoint(x: right, y: line))
                context.stroke(path, with: .color(palette.line), lineWidth: 0.5)
                context.draw(context.resolve(label("\(tick)%")), at: CGPoint(x: right + 8, y: line), anchor: .leading)
            }
            var curve = points.filter { $0.day >= start && $0.day <= end }.map { CGPoint(x: x($0.day), y: y($0.value)) }
            curve.append(CGPoint(x: right, y: y(now)))
            if curve.count > 1 {
                var area = smoothPath(curve)
                area.addLine(to: CGPoint(x: right, y: bottom))
                area.addLine(to: CGPoint(x: curve[0].x, y: bottom))
                area.closeSubpath()
                context.fill(area, with: .color(palette.area))
                context.stroke(smoothPath(curve), with: .color(palette.accent), style: StrokeStyle(lineWidth: 2.5, lineCap: .round, lineJoin: .round))
            }
            let marker = CGPoint(x: right, y: y(now))
            context.fill(Path(ellipseIn: CGRect(x: marker.x - 6.5, y: marker.y - 6.5, width: 13, height: 13)), with: .color(Color(.secondarySystemGroupedBackground)))
            context.stroke(Path(ellipseIn: CGRect(x: marker.x - 6.5, y: marker.y - 6.5, width: 13, height: 13)), with: .color(palette.accent), lineWidth: 2)
            context.fill(Path(ellipseIn: CGRect(x: marker.x - 3, y: marker.y - 3, width: 6, height: 6)), with: .color(palette.accent))
            for mark in monthMarks(from: start, to: end) {
                let position = min(max(x(mark.day), 10), right - 10)
                context.draw(context.resolve(label(mark.text)), at: CGPoint(x: position, y: bottom + 16), anchor: .center)
            }
        }
        .frame(height: plotHeight + 6 + 30)
    }
}

// MARK: - History

/// The completions of each week or month as rounded bars, the current one in the full colour of the habit.
struct HistoryBarsChart: View {
    let bars: [BarPoint]
    let start: Int32
    let end: Int32
    let palette: ChartPalette

    private let plotHeight: CGFloat = 150
    private let axisWidth: CGFloat = 24

    var body: some View {
        let shown = bars.filter { $0.day >= start && $0.day <= end }
        let top = niceTop(shown.map(\.value).max() ?? 0)
        Canvas { context, size in
            let right = size.width - axisWidth
            let bottom = plotHeight + 6
            func y(_ value: Double) -> CGFloat { bottom - CGFloat(value / top) * plotHeight }
            for step in 0...4 {
                let value = top * Double(step) / 4
                context.draw(
                    context.resolve(label(axisText(value))),
                    at: CGPoint(x: right + 8, y: y(value)),
                    anchor: .leading
                )
            }
            let slot = right / CGFloat(max(shown.count, 1))
            let width = min(slot * 0.62, 14)
            for (index, bar) in shown.enumerated() {
                let centre = slot * (CGFloat(index) + 0.5)
                let height = max(CGFloat(bar.value / top) * plotHeight, width)
                let rect = CGRect(x: centre - width / 2, y: bottom - height, width: width, height: height)
                let color = index == shown.count - 1 ? palette.accent : palette.muted
                context.fill(Path(roundedRect: rect, cornerRadius: width / 2), with: .color(color))
            }
            let span = max(CGFloat(end - start), 1)
            for mark in monthMarks(from: start, to: end) {
                let position = min(max(CGFloat(mark.day - start) / span * right, 10), right - 10)
                context.draw(context.resolve(label(mark.text)), at: CGPoint(x: position, y: bottom + 16), anchor: .center)
            }
        }
        .frame(height: plotHeight + 6 + 30)
    }

    /// The top of the axis: four equal steps of a round size that reach the largest bar (8 for a bar of 7).
    private func niceTop(_ maximum: Double) -> Double {
        guard maximum > 0 else { return 4 }
        let raw = maximum / 4
        let magnitude = pow(10, floor(log10(raw)))
        let step = [1.0, 2.0, 5.0, 10.0].map { $0 * magnitude }.first { $0 >= raw } ?? raw
        return step * 4
    }

    private func axisText(_ value: Double) -> String {
        value == value.rounded() ? String(Int(value)) : String(format: "%.1f", value)
    }
}

// MARK: - Frequency

/// The completions of each day of the week as horizontal bars, the day with most completions in the full colour.
struct WeekdayBarsChart: View {
    let totals: [WeekdayTotal]
    let names: [String]
    let palette: ChartPalette

    var body: some View {
        let maximum = max(totals.map { Int($0.total) }.max() ?? 0, 1)
        let best = totals.max { $0.total < $1.total }?.day
        VStack(spacing: 0) {
            ForEach(Array(totals.enumerated()), id: \.offset) { index, item in
                HStack(spacing: 0) {
                    Text(names.indices.contains(index) ? names[index] : "")
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)
                        .frame(width: 44, alignment: .leading)
                    GeometryReader { proxy in
                        let width = max(proxy.size.width * CGFloat(item.total) / CGFloat(maximum), 20)
                        Capsule()
                            .fill(item.day == best ? palette.accent : palette.muted)
                            .frame(width: width, height: 19)
                            .frame(maxHeight: .infinity, alignment: .center)
                    }
                    Text("\(item.total)")
                        .font(.system(size: 15, weight: .semibold))
                        .frame(width: 36, alignment: .trailing)
                }
                .frame(height: 36)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel("\(names.indices.contains(index) ? names[index] : ""), \(item.total)")
            }
        }
    }
}

// MARK: - Best streaks

/// The longest streaks, one per row: the days, a bar as long as the streak and its length.
struct StreakRowsChart: View {
    let streaks: [StreakItem]
    let palette: ChartPalette
    let dayText: (Int) -> String

    private let barSpace: CGFloat = 84

    var body: some View {
        let longest = max(streaks.map { Int($0.length) }.max() ?? 1, 1)
        VStack(spacing: 0) {
            ForEach(Array(streaks.enumerated()), id: \.offset) { index, streak in
                if index > 0 { Divider().overlay(palette.line) }
                HStack(spacing: 0) {
                    Text(rangeText(streak))
                        .font(.system(size: 15))
                    Spacer(minLength: 8)
                    HStack {
                        Spacer(minLength: 0)
                        Capsule()
                            .fill(Int(streak.length) == longest ? palette.accent : palette.muted)
                            .frame(
                                width: max(CGFloat(streak.length) / CGFloat(longest) * barSpace, 9),
                                height: Int(streak.length) == 1 ? 9 : 7
                            )
                    }
                    .frame(width: barSpace)
                    Text(dayText(Int(streak.length)))
                        .font(.system(size: 15))
                        .frame(width: 70, alignment: .trailing)
                }
                .frame(height: 44)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel("\(rangeText(streak)), \(dayText(Int(streak.length)))")
            }
        }
    }

    /// "Oct 20 – 28" inside a month, "Sep 28 – Oct 2" across two and "Oct 18" for a single day.
    private func rangeText(_ streak: StreakItem) -> String {
        let first = date(fromDaysSince2000: Int(streak.start))
        let last = date(fromDaysSince2000: Int(streak.end))
        let long = appDateFormatter()
        long.setLocalizedDateFormatFromTemplate("MMMd")
        if streak.start == streak.end { return long.string(from: first) }
        let calendar = Calendar(identifier: .gregorian)
        if calendar.component(.month, from: first) == calendar.component(.month, from: last) {
            return "\(long.string(from: first)) – \(calendar.component(.day, from: last))"
        }
        return "\(long.string(from: first)) – \(long.string(from: last))"
    }
}

// MARK: - Calendar

/// The last weeks as columns of round squares, one per day, with the names of the days at the side and of the months above.
struct CalendarDotsChart: View {
    let days: [CalendarDay]
    let today: Int32
    /// 0 for Sunday.
    let firstWeekday: Int
    let weeks: Int
    let palette: ChartPalette
    let onTap: (Int32) -> Void

    // The room for the letters and for the months, in squares of a day, so the chart keeps its shape at any width
    private let letterUnits: CGFloat = 1.3
    private let headerUnits: CGFloat = 1.2

    private struct Week {
        let start: Int32
        let states: [Int32?]
    }

    var body: some View {
        let layout = columns()
        Color.clear
            .aspectRatio((CGFloat(weeks) + letterUnits) / (7 + headerUnits), contentMode: .fit)
            .overlay {
                Canvas { context, size in
                    let pitch = size.width / (CGFloat(weeks) + letterUnits)
                    let dot = pitch - 3.5
                    let letters = appCalendar().veryShortWeekdaySymbols
                    for row in 0..<7 {
                        let symbol = (firstWeekday + row) % 7
                        context.draw(
                            context.resolve(label(letters.indices.contains(symbol) ? letters[symbol] : "")),
                            at: CGPoint(x: 6, y: (headerUnits + CGFloat(row)) * pitch + dot / 2),
                            anchor: .center
                        )
                    }
                    for (column, week) in layout.enumerated() {
                        let x = letterUnits * pitch + CGFloat(column) * pitch
                        if let mark = monthMark(of: week) {
                            context.draw(context.resolve(label(mark)), at: CGPoint(x: x, y: headerUnits * pitch / 2), anchor: .leading)
                        }
                        for (row, state) in week.states.enumerated() {
                            guard let state else { continue }
                            let rect = CGRect(x: x, y: (headerUnits + CGFloat(row)) * pitch, width: dot, height: dot)
                            let path = Path(roundedRect: rect, cornerRadius: 4)
                            switch state {
                            case 1:
                                context.fill(path, with: .color(palette.accent))
                            case 2:
                                context.fill(path, with: .color(palette.accent.opacity(0.45)))
                            case 3:
                                context.fill(path, with: .color(palette.line))
                                var dash = Path()
                                dash.move(to: CGPoint(x: rect.minX + dot * 0.3, y: rect.midY))
                                dash.addLine(to: CGPoint(x: rect.maxX - dot * 0.3, y: rect.midY))
                                context.stroke(dash, with: .color(Color.secondary), lineWidth: 1.5)
                            default:
                                context.fill(path, with: .color(palette.line))
                            }
                        }
                    }
                }
                .contentShape(Rectangle())
                .gesture(
                    SpatialTapGesture().onEnded { value in
                        // The same grid as the drawing, on the size of the chart
                        guard let width = lastWidth else { return }
                        if let day = day(at: value.location, width: width, layout: layout) { onTap(day) }
                    }
                )
                .background(
                    GeometryReader { proxy in
                        Color.clear.preference(key: CalendarWidthKey.self, value: proxy.size.width)
                    }
                )
                .onPreferenceChange(CalendarWidthKey.self) { lastWidth = $0 }
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(L("calendar", "Calendar"))
            .accessibilityValue(summary(layout))
            .accessibilityAddTraits(.isButton)
    }

    @State private var lastWidth: CGFloat?

    private func columns() -> [Week] {
        let known = Dictionary(uniqueKeysWithValues: days.map { ($0.day, $0.state) })
        // 2000-01-01 was a Saturday: the weekday of a day counted from Sunday
        let sunday = (Int(today) + 6) % 7
        let sinceWeekStart = (sunday - firstWeekday + 7) % 7
        let newestStart = today - Int32(sinceWeekStart)
        return (0..<weeks).reversed().map { back in
            let start = newestStart - Int32(back * 7)
            let states: [Int32?] = (0..<7).map { row in
                let day = start + Int32(row)
                return day > today ? nil : (known[day] ?? 0)
            }
            return Week(start: start, states: states)
        }
    }

    private func monthMark(of week: Week) -> String? {
        let calendar = Calendar(identifier: .gregorian)
        let formatter = appDateFormatter()
        formatter.setLocalizedDateFormatFromTemplate("MMM")
        for row in 0..<7 {
            let date = date(fromDaysSince2000: Int(week.start) + row)
            if calendar.component(.day, from: date) == 1 { return formatter.string(from: date) }
        }
        return nil
    }

    private func day(at point: CGPoint, width: CGFloat, layout: [Week]) -> Int32? {
        let pitch = width / (CGFloat(weeks) + letterUnits)
        let column = Int(floor((point.x - letterUnits * pitch) / pitch))
        let row = Int(floor(point.y / pitch - headerUnits))
        guard layout.indices.contains(column), (0..<7).contains(row) else { return nil }
        let day = layout[column].start + Int32(row)
        return day > today ? nil : day
    }

    private func summary(_ layout: [Week]) -> String {
        let shown = Set(layout.flatMap { week in (0..<7).map { week.start + Int32($0) } })
        let done = days.filter { $0.state == 1 && shown.contains($0.day) }.count
        return "\(done) \(L("done", "Done"))"
    }
}

private struct CalendarWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = nextValue() }
}
