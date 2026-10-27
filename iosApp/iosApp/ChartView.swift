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

typealias DrawOp = Salem_coreDrawOp

/// Paints the operations recorded by the core charts. Coordinates are in points, origin at the top left.
struct ChartCanvas: View {
    let ops: [DrawOp]
    var onTap: (CGPoint) -> Void = { _ in }

    var body: some View {
        Canvas { context, _ in
            var color = Color.black
            var fontSize: CGFloat = 12
            var bold = false
            var align = 0
            var stroke: CGFloat = 1
            for op in ops {
                switch op.kind {
                case 1:
                    color = habitColor(op.color)
                case 10:
                    bold = op.extra == 1
                case 11:
                    fontSize = CGFloat(op.x)
                case 12:
                    stroke = CGFloat(op.x)
                case 13:
                    align = Int(op.extra)
                case 2:
                    var path = Path()
                    path.move(to: CGPoint(x: CGFloat(op.x), y: CGFloat(op.y)))
                    path.addLine(to: CGPoint(x: CGFloat(op.w), y: CGFloat(op.h)))
                    context.stroke(path, with: .color(color), lineWidth: stroke)
                case 4:
                    context.fill(Path(CGRect(x: CGFloat(op.x), y: CGFloat(op.y), width: CGFloat(op.w), height: CGFloat(op.h))), with: .color(color))
                case 5:
                    context.fill(
                        Path(roundedRect: CGRect(x: CGFloat(op.x), y: CGFloat(op.y), width: CGFloat(op.w), height: CGFloat(op.h)), cornerRadius: CGFloat(op.a)),
                        with: .color(color)
                    )
                case 6:
                    context.stroke(Path(CGRect(x: CGFloat(op.x), y: CGFloat(op.y), width: CGFloat(op.w), height: CGFloat(op.h))), with: .color(color), lineWidth: stroke)
                case 7:
                    var path = Path()
                    path.addArc(
                        center: CGPoint(x: CGFloat(op.x), y: CGFloat(op.y)),
                        radius: CGFloat(op.w),
                        startAngle: .degrees(op.h),
                        endAngle: .degrees(op.h + op.a),
                        clockwise: false
                    )
                    path.addLine(to: CGPoint(x: CGFloat(op.x), y: CGFloat(op.y)))
                    context.fill(path, with: .color(color))
                case 8:
                    context.fill(Path(ellipseIn: CGRect(x: CGFloat(op.x - op.w), y: CGFloat(op.y - op.w), width: CGFloat(op.w * 2), height: CGFloat(op.w * 2))), with: .color(color))
                case 9:
                    context.blendMode = .destinationOut
                    context.fill(Path(ellipseIn: CGRect(x: CGFloat(op.x - op.w), y: CGFloat(op.y - op.w), width: CGFloat(op.w * 2), height: CGFloat(op.w * 2))), with: .color(.black))
                    context.blendMode = .normal
                case 3:
                    let text = Text(op.text).font(.system(size: fontSize, weight: bold ? .bold : .regular))
                    let anchor: UnitPoint = align == 1 ? .bottom : (align == 2 ? .bottomTrailing : .bottomLeading)
                    context.draw(text, at: CGPoint(x: CGFloat(op.x), y: CGFloat(op.y)), anchor: anchor)
                default:
                    break
                }
            }
        }
        .contentShape(Rectangle())
        .gesture(SpatialTapGesture().onEnded { onTap($0.location) })
    }
}
