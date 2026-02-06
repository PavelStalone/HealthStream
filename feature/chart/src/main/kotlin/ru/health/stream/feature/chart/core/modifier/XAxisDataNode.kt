package ru.health.stream.feature.chart.core.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import ru.health.stream.feature.chart.core.XAxisSide

internal class XAxisDataNode(
    var x: Float,
    var side: XAxisSide,
    var alignment: Alignment.Horizontal,
) : Modifier.Node(), ParentDataModifierNode {

    override fun Density.modifyParentData(parentData: Any?) = this@XAxisDataNode
}

internal data class XAxisDataElement(
    val x: Float,
    val side: XAxisSide,
    val alignment: Alignment.Horizontal,
    val inspectorInfo: InspectorInfo.() -> Unit,
) : ModifierNodeElement<XAxisDataNode>() {

    override fun create(): XAxisDataNode = XAxisDataNode(
        x = x,
        side = side,
        alignment = alignment,
    )

    override fun update(node: XAxisDataNode) {
        node.x = x
        node.side = side
        node.alignment = alignment
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}
