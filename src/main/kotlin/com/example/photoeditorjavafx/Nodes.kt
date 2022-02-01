package com.example.photoeditorjavafx

import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.math.max


const val STRING_TYPE: String = "String"
const val FLOAT_TYPE: String = "Float"
const val INT_TYPE: String = "Int"
const val IMAGE_TYPE: String = "Image"

const val INPUT: Boolean = true
const val OUTPUT: Boolean = false

abstract class BaseNode {
    @FXML
    lateinit var mainNode: GridPane

    @FXML
    lateinit var titleGrid: GridPane

    @FXML
    lateinit var title: Text

    @FXML
    var removeBtn: Button? = null

    @FXML
    lateinit var connectionGrid: GridPane

    @FXML
    lateinit var inputGrid: GridPane

    @FXML
    lateinit var outputGrid: GridPane

    val outputs = mutableListOf<BaseNode?>()

    val inputConnectors = mutableListOf<Connector>()
    val outputConnectors = mutableListOf<Connector>()

    abstract val content: Any
    var x: Double = 0.0
        private set
    var y: Double = 0.0
        private set

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

            for (inputConnector in inputConnectors) {
                inputConnector.getBoundOnScene().let { tempBounds ->
                    inputConnector.line?.endX = tempBounds.centerX
                    inputConnector.line?.endY = tempBounds.centerY
                }
            }
            for (outputConnector in outputConnectors) {
                outputConnector.getBoundOnScene().let { tempBounds ->
                    outputConnector.line?.startX = tempBounds.centerX
                    outputConnector.line?.startY = tempBounds.centerY
                }
            }
        }

        removeBtn!!.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            val scene = (mainNode.parent as AnchorPane)

            for (i in 0 until inputConnectors.size) {
                scene.children.remove(inputConnectors[i].line)
                inputConnectors[i].line = null
                inputConnectors[i].to?.line = null
                inputConnectors[i].to?.to = null
                inputConnectors[i].to = null
            }
            inputConnectors.removeAll { true }

            outputs.removeAll { true }

            for (i in 0 until outputConnectors.size) {
                scene.children.remove(outputConnectors[i].line)
                outputConnectors[i].line = null
                outputConnectors[i].to?.line = null
                val outputParent = outputConnectors[i].to?.parent
                outputConnectors[i].to?.to = null
                outputConnectors[i].to = null
                outputParent?.needUpdate()
            }
            outputConnectors.removeAll { true }

            scene.children.remove(this.mainNode)
        }
    }

    fun setInitX(newX: Double) {
        x = newX
        mainNode.layoutX = newX
    }

    fun setInitY(newY: Double) {
        y = newY
        mainNode.layoutX = newY
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

    protected fun createAndAddConnector(type: String, isInput: Boolean, index: Int) {
        val box = HBox()
        val conType = Text(type)
        val connector = Connector(this, type, isInput, index)

        if (isInput) {
            inputConnectors.add(connector)

            box.children.add(connector.connectionDot)
            box.children.add(conType)
            inputGrid.addRow(index, box)
        } else {
            outputs.add(null)
            outputConnectors.add(connector)

            box.children.add(conType)
            box.children.add(connector.connectionDot)
            outputGrid.addRow(index, box)

        }
    }

    protected fun imageToMat(image: Image): Mat {
        val width = image.width.toInt()
        val height = image.height.toInt()
        val buffer = ByteArray(width * height * 4)
        val reader = image.pixelReader
        val format: WritablePixelFormat<ByteBuffer> = WritablePixelFormat.getByteBgraInstance()
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4)
        val mat = Mat(height, width, CvType.CV_8UC4)
        mat.put(0, 0, buffer)
        return mat
    }

    protected fun matToImage(frame: Mat): Image {
        val buffer = MatOfByte()
        Imgcodecs.imencode(".png", frame, buffer)
        return Image(ByteArrayInputStream(buffer.toArray()))
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
    @FXML
    override lateinit var content: ImageView

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
                content.text = temp.substring(0, index) + '.' + if (temp.length > index) temp.substring(index) else ""
            }
            needUpdate()
        }

        mainNode.add(content, 0, 1)
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
        createAndAddConnector(FLOAT_TYPE, OUTPUT, 0)
        update()
    }
}

class IntNode : BaseNonImageNode() {
    override val content = TextField()

    init {
        title.text = "Int"

        content.textProperty().addListener { _, _, _ ->
            content.text = content.text.filter { it.isDigit() }
            needUpdate()
        }

        mainNode.add(content, 0, 1)
    }

    override fun update() {
        ans = try {
            content.text.toInt()
        } catch (e: Exception) {
            0
        }
        needUpdate = false
    }

    init {
        createAndAddConnector(INT_TYPE, OUTPUT, 0)
        update()
    }
}

class StringNode : BaseNonImageNode() {
    override val content = TextField()

    init {
        title.text = "String"

        content.textProperty().addListener { _, _, _ ->
            needUpdate()
        }

        mainNode.add(content, 0, 1)
    }

    override fun update() {
        ans = content.text
        needUpdate = false
    }

    init {
        createAndAddConnector(STRING_TYPE, OUTPUT, 0)
        update()
    }
}


class ImageNode : BaseImageNode() {
    val contentButton = Button("Open")

    init {
        title.text = "Image Open"

        contentButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("All Files", "*.*"),
                FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
                FileChooser.ExtensionFilter("PNG", "*.png"),
            )
            val file = fileChooser.showOpenDialog(contentButton.scene.window)
            try {
                content.image = SwingFXUtils.toFXImage(ImageIO.read(file), null)
                needUpdate()
            } catch (e: Exception) {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.contentText = e.toString()
                alert.show()
            }
        }
        connectionGrid.add(contentButton, 1, 0)
    }

    override fun update() {
        ans = content.image
        needUpdate = false
    }

    init {
        createAndAddConnector(IMAGE_TYPE, OUTPUT, 0)
        update()
    }
}


class EndImageNode(private val outputImage: ImageView) : BaseImageNode() {

    init {
        title.text = "Выходное изображение"

        titleGrid.children.remove(removeBtn)
        removeBtn = null
    }

    override fun update() {
        ans = inputConnectors[0].to?.parent?.getData()
        content.image = ans as Image?
        outputImage.image = ans as Image?
        outputImage.fitWidth = outputImage.image?.width ?: 0.0
        outputImage.fitHeight = outputImage.image?.height ?: 0.0
        needUpdate = false
    }

    init {
        createAndAddConnector(IMAGE_TYPE, INPUT, 0)
        update()
    }
}

class AddTextNode : BaseImageNode() {

    init {
        title.text = "Добавить текст"
    }

    override fun update() {
        needUpdate = false
        val inputImg = inputConnectors[0].to?.parent?.getData() as Image?
        val inputX = inputConnectors[1].to?.parent?.getData() as Int?
        val inputY = inputConnectors[2].to?.parent?.getData() as Int?
        val inputStr = inputConnectors[3].to?.parent?.getData() as String?

        if (inputImg === null || inputX === null || inputY === null || inputStr === null) {
            content.image = null
            ans = null
            return
        }

        val mat = imageToMat(inputImg)
        Imgproc.putText(
            mat,
            inputStr,
            Point(inputX.toDouble(), inputY.toDouble()),
            Imgproc.FONT_HERSHEY_SIMPLEX,
            1.0,
            Scalar(100.0, 100.0, 100.0, 255.0)
        )
        content.image = matToImage(mat)
        ans = content.image
    }

    init {
        createAndAddConnector(IMAGE_TYPE, INPUT, 0)
        createAndAddConnector(INT_TYPE, INPUT, 1)
        createAndAddConnector(INT_TYPE, INPUT, 2)
        createAndAddConnector(STRING_TYPE, INPUT, 3)

        createAndAddConnector(IMAGE_TYPE, OUTPUT, 0)

        update()
    }
}

class AddImageNode : BaseImageNode() {

    init {
        title.text = "Добавить изображение"
    }

    override fun update() {
        needUpdate = false
        val inputImg = inputConnectors[0].to?.parent?.getData() as Image?
        val inputX = inputConnectors[1].to?.parent?.getData() as Int?
        val inputY = inputConnectors[2].to?.parent?.getData() as Int?
        val inputAddImg = inputConnectors[3].to?.parent?.getData() as Image?

        if (inputImg === null || inputX === null || inputY === null || inputAddImg === null) {
            content.image = null
            ans = null
            return
        }
        val width = max(inputImg.width, inputAddImg.width + inputX).toInt()
        val height = max(inputImg.height, inputAddImg.height + inputY).toInt()

        val mat = imageToMat(inputImg)
        val addMat = imageToMat(inputAddImg)
        val outMat = Mat(height, width, CvType.CV_8UC4)

        mat.copyTo(outMat.rowRange(0, mat.rows()).colRange(0, mat.cols()))
        addMat.copyTo(outMat.rowRange(inputY, inputY + addMat.rows()).colRange(inputX, inputX + addMat.cols()))

        content.image = matToImage(outMat)
        ans = content.image

    }

    init {
        createAndAddConnector(IMAGE_TYPE, INPUT, 0)
        createAndAddConnector(INT_TYPE, INPUT, 1)
        createAndAddConnector(INT_TYPE, INPUT, 2)
        createAndAddConnector(IMAGE_TYPE, INPUT, 3)

        createAndAddConnector(IMAGE_TYPE, OUTPUT, 0)
    }

}


class PrintNode : BaseNonImageNode() {
    override val content = Text()

    init {
        createAndAddConnector(STRING_TYPE, INPUT, 0)
        mainNode.add(content, 0, 1)
    }

    override fun update() {
        ans = inputConnectors[0].to?.parent?.getData()
        content.text = ans.toString()
        needUpdate = false
    }

    init {
        update()
    }
}
