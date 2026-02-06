package ru.health.stream.feature.chart.core.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import ru.health.stream.feature.chart.core.YAxisSide

internal class YAxisDataNode(
    var y: Float,
    var side: YAxisSide,
    var alignment: Alignment.Vertical,
) : Modifier.Node(), ParentDataModifierNode {

    override fun Density.modifyParentData(parentData: Any?) = this@YAxisDataNode
}

internal data class YAxisDataElement(
    val y: Float,
    val side: YAxisSide,
    val alignment: Alignment.Vertical,
    val inspectorInfo: InspectorInfo.() -> Unit,
) : ModifierNodeElement<YAxisDataNode>() {

    override fun create(): YAxisDataNode = YAxisDataNode(
        y = y,
        side = side,
        alignment = alignment,
    )

    override fun update(node: YAxisDataNode) {
        node.y = y
        node.side = side
        node.alignment = alignment
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}
