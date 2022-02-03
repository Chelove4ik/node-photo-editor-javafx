package com.example.photoeditorjavafx

import com.example.photoeditorjavafx.BaseNode.Companion.listOfNodes
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.Line
import javafx.stage.FileChooser

class MainController {
    @FXML
    lateinit var outputImage: ImageView

    @FXML
    private lateinit var mainAP: AnchorPane

    private var startNode = StartImageNode()
    private lateinit var endNode: EndImageNode

    @FXML
    private fun createNodeFromStringIdInEvent(event: ActionEvent) {
        createNodeFromString((event.source as MenuItem).id)
    }

    @FXML
    private fun saveNodesToFile(event: ActionEvent) {
        val listOfNodesInString = mutableListOf<String>()  // nodeName:x:y[:content]
        val listOfLinksInString = mutableListOf<String>()  // outputNodeIndex:outputIndex:inputNodeIndex:inputIndex

        for (index in listOfNodes.indices) {
            var temp = "${nodeClassToString(listOfNodes[index])}:${listOfNodes[index].x}:${listOfNodes[index].y}"

            when (nodeClassToString(listOfNodes[index])) {
                "StartImage" -> temp += ":" + (listOfNodes[index] as StartImageNode).url
                "FloatNode" -> temp += ":" + listOfNodes[index].ans
                "Int" -> temp += ":" + listOfNodes[index].ans
                "String" -> temp += ":" + listOfNodes[index].ans
                "Image" -> temp += ":" + (listOfNodes[index] as ImageNode).url
            }
            listOfNodesInString.add(temp)

            for (output in listOfNodes[index].outputConnectors) {
                if (output.to === null)
                    continue
                listOfLinksInString.add(
                    "$index:${output.index}:${listOfNodes.indexOf(output.to!!.parent)}:${output.to!!.index}"
                )
            }
        }

        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Scene File", "*.pejfx"),
        )
        val file = fileChooser.showSaveDialog((event.source as MenuItem).parentPopup.ownerWindow)
        if (file === null)
            return

        var outputString = listOfNodesInString.size.toString() + "\n" + listOfLinksInString.size.toString() + "\n"

        for (str in listOfNodesInString)
            outputString += str + "\n"
        for (str in listOfLinksInString)
            outputString += str + "\n"
        file.writeText(outputString)
    }

    @FXML
    private fun createNodesFromFile(event: ActionEvent) {
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("All Files", "*.*"),
            FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
            FileChooser.ExtensionFilter("PNG", "*.png"),
        )
        val file = fileChooser.showOpenDialog((event.source as MenuItem).parentPopup.ownerWindow)
        val fileData: List<String>
        val n: Int
        val m: Int
        try {
            fileData = file.readLines()

            listOfNodes.removeAll { true }
            mainAP.children.removeAll { true }

            n = fileData[0].toInt()
            m = fileData[1].toInt()

            for (lineIndex in 2 until n + 2) {
                val line = fileData[lineIndex]
                val indexFirst = line.indexOf(':')
                val indexSecond = line.indexOf(':', indexFirst + 1)
                val indexThird = line.indexOf(':', indexSecond + 1)
                val nodeName = line.substring(0, indexFirst)
                val x: Double = line.substring(indexFirst + 1, indexSecond).toDouble()
                val y: Double
                val content: String

                val newNode = createNodeFromString(nodeName)

                when (nodeName) {
                    "StartImage" -> {
                        y = line.substring(indexSecond + 1, indexThird).toDouble()
                        content = line.substring(indexThird + 1)
                        (newNode as StartImageNode).url = content
                        startNode = newNode
                    }
                    "FloatNode" -> {
                        y = line.substring(indexSecond + 1, indexThird).toDouble()
                        content = line.substring(indexThird + 1)
                        (newNode as FloatNode).content.text = content
                    }
                    "Int" -> {
                        y = line.substring(indexSecond + 1, indexThird).toDouble()
                        content = line.substring(indexThird + 1)
                        (newNode as IntNode).content.text = content
                    }
                    "String" -> {
                        y = line.substring(indexSecond + 1, indexThird).toDouble()
                        content = line.substring(indexThird + 1)
                        (newNode as StringNode).content.text = content
                    }
                    "Image" -> {
                        y = line.substring(indexSecond + 1, indexThird).toDouble()
                        content = line.substring(indexThird + 1)
                        (newNode as ImageNode).url = content
                    }
                    "EndImage" -> {
                        y = line.substring(indexSecond + 1).toDouble()
                        endNode = newNode as EndImageNode
                    }
                    else -> y = line.substring(indexSecond + 1).toDouble()
                }
                newNode.setInitX(x)
                newNode.setInitY(y)
            }

            for (lineIndex in n + 2 until m + n + 2) {
                val line = fileData[lineIndex]
                val indexFirst = line.indexOf(':')
                val indexSecond = line.indexOf(':', indexFirst + 1)
                val indexThird = line.indexOf(':', indexSecond + 1)

                val outputNodeIndex: Int = line.substring(0, indexFirst).toInt()
                val outputIndex: Int = line.substring(indexFirst + 1, indexSecond).toInt()
                val inputNodeIndex: Int = line.substring(indexSecond + 1, indexThird).toInt()
                val inputIndex: Int = line.substring(indexThird + 1).toInt()

                val out = listOfNodes[outputNodeIndex]
                val inp = listOfNodes[inputNodeIndex]

                val fxLine = Line()

                out.outputs[outputIndex] = inp
                out.outputConnectors[outputIndex].to = inp.inputConnectors[inputIndex]
                out.outputConnectors[outputIndex].line = fxLine
                inp.inputConnectors[inputIndex].to = out.outputConnectors[outputIndex]
                inp.inputConnectors[inputIndex].line = fxLine

                mainAP.children.add(fxLine)

                Platform.runLater {
                    fxLine.startX = out.outputConnectors[outputIndex].getBoundOnScene().centerX
                    fxLine.startY = out.outputConnectors[outputIndex].getBoundOnScene().centerY
                    fxLine.endX = inp.inputConnectors[inputIndex].getBoundOnScene().centerX
                    fxLine.endY = inp.inputConnectors[inputIndex].getBoundOnScene().centerY
                }

                inp.needUpdate()
            }

        } catch (e: Exception) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.contentText = e.toString()
            alert.show()
            return
        }
    }

    private fun nodeClassToString(node: BaseNode): String {
        return when (node) {
            is StartImageNode -> "StartImage"
            is FloatNode -> "Float"
            is IntNode -> "Int"
            is StringNode -> "String"
            is ImageNode -> "Image"
            is AddTextNode -> "AddText"
            is AddImageNode -> "AddImage"
//            is GrayFIlterNode -> "GrayFIlter"
//            is BrightnessNode -> "Brightness"
//            is SepiaNode -> "Sepia"
//            is InvertFilterNode -> "InvertFilter"
//            is BlurFilterNode -> "BlurFilter"
//            is TransformMoveNode -> "TransformMove"
//            is TransformScaleNode -> "TransformScale"
//            is TransformRotateNode -> "TransformRotate"
            is EndImageNode -> "EndImage"
            else -> throw Exception("unknown node")
        }
    }
    private fun createNodeFromString(str: String): BaseNode {
        val newNode: BaseNode = when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
            "Image" -> ImageNode()
            "AddText" -> AddTextNode()
            "AddImage" -> AddImageNode()
//            "GrayFIlter" -> PrintNode()
//            "Brightness" -> PrintNode()
//            "Sepia" -> PrintNode()
//            "InvertFilter" -> PrintNode()
//            "BlurFilter" -> PrintNode()
//            "TransformMove" -> PrintNode()
//            "TransformScale" -> PrintNode()
            "TransformRotate" -> PrintNode()
            "StartImage" -> StartImageNode()
            "EndImage" -> EndImageNode(outputImage)
            else -> throw Exception("unknown node")
        }

        mainAP.children.add(newNode.mainNode)
        return newNode
    }

    @FXML
    private fun initialize() {
        endNode = EndImageNode(outputImage)

        endNode.setInitX(300.0)

        mainAP.children.add(startNode.mainNode)
        mainAP.children.add(endNode.mainNode)
    }
}