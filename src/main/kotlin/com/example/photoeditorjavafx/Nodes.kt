package com.example.photoeditorjavafx

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.text.Text
import java.io.Closeable

const val STRING_TYPE: String = "String"
const val FLOAT_TYPE: String = "Float"
const val INT_TYPE: String = "Int"
const val IMAGE_TYPE: String = "Image"

abstract class BaseNode {
    @FXML
    lateinit var mainNode: GridPane

    @FXML
    lateinit var titleGrid: GridPane

    @FXML
    lateinit var title: Text

    @FXML
    lateinit var removeBtn: Button

    @FXML
    lateinit var connectionGrid: GridPane

    @FXML
    lateinit var inputGrid: GridPane

    @FXML
    lateinit var outputGrid: GridPane

    var input: BaseNode? = null
    val outputs = mutableListOf<BaseNode?>()

    var inputConnector: Connector? = null
    val outputConnectors = mutableListOf<Connector?>()

    abstract val content: Any
    var x: Double = 0.0
    var y: Double = 0.0

    var ans: Any? = null
    var needUpdate: Boolean = false

    var pressedX = 0.0
    var pressedY = 0.0

    @FXML
    private fun initialize() {
        mainNode.styleClass.add("node")

        titleGrid.addEventHandler(MouseDragEvent.MOUSE_PRESSED) {
            pressedX = it.x
            pressedY = it.y
        }

        titleGrid.addEventHandler(MouseDragEvent.MOUSE_DRAGGED) {
            x += it.x - pressedX
            y += it.y - pressedY

            val parent = (mainNode.parent as AnchorPane)
            if (x < 0)
                x = 0.0
            if (y < 0)
                y = 0.0
            if (x + mainNode.width > parent.width)
                x = parent.width - mainNode.width
            if (y + mainNode.height > parent.height)
                y = parent.height - mainNode.height

            mainNode.layoutX = x
            mainNode.layoutY = y

            inputConnector?.getBoundOnScene()?.let { tempBounds ->
                inputConnector?.line?.endX = tempBounds.centerX
                inputConnector?.line?.endY = tempBounds.centerY
            }

            for (outputConnector in outputConnectors) {
                outputConnector?.getBoundOnScene()?.let { tempBounds ->
                    outputConnector.line?.startX = tempBounds.centerX
                    outputConnector.line?.startY = tempBounds.centerY
                }
            }
        }

        removeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            val scene = (mainNode.parent as AnchorPane)
            input = null
            scene.children.remove(inputConnector?.line)
            inputConnector?.line = null
            inputConnector?.to?.line = null

            inputConnector?.to?.to = null
            inputConnector = null

            outputs.removeAll { true }

            for (i in 0 until outputConnectors.size) {
                scene.children.remove(outputConnectors[i]?.line)
                outputConnectors[i]?.line = null
                outputConnectors[i]?.to?.line = null
                val outputParent = outputConnectors[i]?.to?.parent
                outputConnectors[i]?.to?.to = null
                outputConnectors[i]?.to = null
                outputParent?.needUpdate()
            }
            outputConnectors.removeAll { true }

            scene.children.remove(this.mainNode)
        }
    }

    abstract fun update()

    fun needUpdate() {
        if (needUpdate)
            return
        var count = 0
        needUpdate = true
        for (output in outputs) {
            if (output !== null) {
                output.needUpdate()
                count++
            }
        }
        if (count == 0) {
            update()
        }
    }

    fun getData(): Any? {
        if (needUpdate)
            update()
        return ans
    }
}

abstract class BaseNonImageNode : BaseNode() {
    init {
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("non-image-node.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }
}

abstract class BaseImageNode : BaseNode() {
    init {
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("image-node.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }
}

class FloatNode : BaseNonImageNode() {
    override val content = TextField()

    init {
        title.text = "Float"

        content.textProperty().addListener { _, _, _ ->
            content.text = content.text.filter { it.isDigit() || it == '.' }
            val index = content.text.indexOf('.')
            if (index != -1) {
                val temp = content.text.replace(".", "")
                content.text = temp.substring(0, index) + '.' + if (temp.length > index) temp.substring(index) else  ""
            }
            needUpdate()
        }

        mainNode.add(content, 0, 1)
    }

    init {
        val output = Connector(this, FLOAT_TYPE, false, 0)
        outputs.addAll(arrayOf(null))
        outputConnectors.add(output)

        outputGrid.addRow(0, output.connectionDot)
    }

    override fun update() {
        ans = try {
            content.text.toFloat()
        } catch (e: Exception) {
            0.0
        }
        needUpdate = false
    }

    init {
        update()
    }
}

class PrintNode : BaseNonImageNode() {
    override val content = Text()

    val testInput: Connector = Connector(this, FLOAT_TYPE, true, 0)

    init {
        inputConnector = testInput
        mainNode.add(content, 0, 1)
        inputGrid.addRow(0, testInput.connectionDot)
    }

    override fun update() {
        ans = testInput.to?.parent?.getData()
        content.text = ans.toString()
        needUpdate = false
    }

    init {
        update()
    }
}