package com.example.photoeditorjavafx

import javafx.geometry.Bounds
import javafx.scene.input.MouseDragEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Line

open class Connector(
    val parent: BaseNode,
    private val type: String,
    private val isInput: Boolean,
    private val index: Int
) {
    companion object StartConnector {
        var startConnector: Connector? = null
    }

    val connectionDot: Circle = Circle(0.0, 0.0, 7.0)
    var to: Connector? = null
    var line: Line? = null
    private var deleteOnRelease: Boolean = true

    fun getBoundOnScene(): Bounds {
        val parent1 = connectionDot.parent as GridPane
        val parent2 = parent1.parent as GridPane
        val parent3 = parent2.parent as GridPane
        val parent4 = parent3.parent as AnchorPane

        val boundsInParent1 = parent1.localToParent(connectionDot.boundsInParent)
        val boundsInParent2 = parent2.localToParent(boundsInParent1)
        val boundsInParent3 = parent3.localToParent(boundsInParent2)
        val boundsInParent4 = parent4.localToParent(boundsInParent3)

        return boundsInParent4
    }

    init {
        connectionDot.addEventHandler(MouseDragEvent.MOUSE_PRESSED) {
            if (line === null) {
                line = Line()

                val boundsInAnchorPane = getBoundOnScene()
                if (isInput) {
                    line!!.startX = it.x + boundsInAnchorPane.centerX
                    line!!.startY = it.y + boundsInAnchorPane.centerY
                    line!!.endX = boundsInAnchorPane.centerX
                    line!!.endY = boundsInAnchorPane.centerY
                } else {
                    line!!.startX = boundsInAnchorPane.centerX
                    line!!.startY = boundsInAnchorPane.centerY
                    line!!.endX = it.x + boundsInAnchorPane.centerX
                    line!!.endY = it.y + boundsInAnchorPane.centerY
                }

                (parent.mainNode.parent as AnchorPane).children.add(line)
            }
        }

        connectionDot.addEventHandler(MouseDragEvent.DRAG_DETECTED) {
            connectionDot.startFullDrag()
            startConnector = this

            val boundsInAnchorPane = getBoundOnScene()

            if (isInput) {
                line!!.startX = it.x + boundsInAnchorPane.centerX
                line!!.startY = it.y + boundsInAnchorPane.centerY
                line!!.endX = boundsInAnchorPane.centerX
                line!!.endY = boundsInAnchorPane.centerY
            } else {
                line!!.startX = boundsInAnchorPane.centerX
                line!!.startY = boundsInAnchorPane.centerY
                line!!.endX = it.x + boundsInAnchorPane.centerX
                line!!.endY = it.y + boundsInAnchorPane.centerY
            }

            if (to !== null) {
                to!!.line = null
                to!!.to = null

                if (isInput) {
                    to!!.parent.outputs[to!!.index] = null
                    to = null
                    parent.needUpdate()
                } else {
                    parent.outputs[index] = null
                    to!!.parent.needUpdate()
                    to = null
                }
            }
        }

        connectionDot.addEventHandler(MouseDragEvent.MOUSE_DRAGGED) {
            // TODO сделать кривую линию
            val boundsInParent = getBoundOnScene()

            if (isInput) {
                line!!.startX = it.x + boundsInParent.centerX + 0.5 * if (line!!.endX > line!!.startX) 1 else -1
                line!!.startY = it.y + boundsInParent.centerY + 0.5 * if (line!!.endY > line!!.startY) 1 else -1
            } else {
                line!!.endX = it.x + boundsInParent.centerX + 0.5 * if (line!!.endX < line!!.startX) 1 else -1
                line!!.endY = it.y + boundsInParent.centerY + 0.5 * if (line!!.endY < line!!.startY) 1 else -1
            }

//            val sign = if (line.startY > line.endY) -1 else 1
//            a.controlX1 = a.startX + (a.endX - a.startX) / 3
//            a.controlY1 = sign * a.startY + (a.endY - a.startY) / 3
//
//            a.controlY2 = sign * a.endY - (a.endY - a.startY) / 3
//            a.controlX2 = a.endX - (a.endX - a.startX) / 3

        }

        connectionDot.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED) {
            if (to === null && parent !== startConnector!!.parent && type === startConnector!!.type && isInput != startConnector!!.isInput) {
                val boundsInAnchorPane = getBoundOnScene()
                if (startConnector!!.isInput) {
                    startConnector!!.line!!.startX = boundsInAnchorPane.centerX
                    startConnector!!.line!!.startY = boundsInAnchorPane.centerY
                } else {
                    startConnector!!.line!!.endX = boundsInAnchorPane.centerX
                    startConnector!!.line!!.endY = boundsInAnchorPane.centerY
                }
                startConnector!!.to = this
                to = startConnector!!
                line = startConnector!!.line


                if (isInput) {
                    to!!.parent.outputs[to!!.index] = parent
                    parent.needUpdate()
                } else {
                    parent.outputs[index] = to!!.parent
                    to!!.parent.needUpdate()
                }
                startConnector!!.deleteOnRelease = false
            }
            startConnector = null
            it.consume()
        }

        connectionDot.addEventHandler(MouseDragEvent.MOUSE_RELEASED) {
            if (deleteOnRelease) {
                (parent.mainNode.parent as AnchorPane).children.remove(line)
                line = null
                startConnector = null
            }
            deleteOnRelease = true
        }
    }
}
