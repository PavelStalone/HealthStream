package ru.health.stream.feature.chart.core.modifier

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density

internal class PointDataNode(
    var x: Float,
    var y: Float,
    var alignment: Alignment,
) : Modifier.Node(), ParentDataModifierNode {

    override fun Density.modifyParentData(parentData: Any?) = this@PointDataNode
}

internal data class PointDataElement(
    val x: Float,
    val y: Float,
    val alignment: Alignment,
    val inspectorInfo: InspectorInfo.() -> Unit,
) : ModifierNodeElement<PointDataNode>() {

    override fun create(): PointDataNode = PointDataNode(
        x = x,
        y = y,
        alignment = alignment,
    )

    override fun update(node: PointDataNode) {
        node.x = x
        node.y = y
        node.alignment = alignment
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}
