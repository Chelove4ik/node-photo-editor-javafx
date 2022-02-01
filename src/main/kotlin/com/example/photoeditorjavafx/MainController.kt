package com.example.photoeditorjavafx

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane

class MainController {
    @FXML
    lateinit var outputImage: ImageView

    @FXML
    private lateinit var mainAP: AnchorPane

    private val startNode = ImageNode()
    private lateinit var endNode: EndImageNode

    @FXML
    private fun createNodeFromStringIdInEvent(event: ActionEvent) {
        createNodeFromString((event.source as MenuItem).id)
    }

    private fun createNodeFromString(str: String) : BaseNode? {
        val newNode: BaseNode? = when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
            "Image" -> ImageNode()
            "AddText" -> AddTextNode()
//            "AddImage" -> PrintNode()
//            "GrayFIlter" -> PrintNode()
//            "Brightness" -> PrintNode()
//            "Sepia" -> PrintNode()
//            "InvertFilter" -> PrintNode()
//            "BlurFilter" -> PrintNode()
//            "TransformMove" -> PrintNode()
//            "TransformScale" -> PrintNode()
            "TransformRotate" -> PrintNode()
            else -> null
        }

        if (newNode !== null) {
            mainAP.children.add(newNode.mainNode)
        } else {
            // TODO мб сообщение об ошибке
        }
        return newNode
    }

    @FXML
    private fun initialize() {
        startNode.titleGrid.children.remove(startNode.removeBtn)
        startNode.removeBtn = null
        startNode.title.text = "Входное изображение"

        endNode = EndImageNode(outputImage)

        endNode.setInitX(300.0)

        mainAP.children.add(startNode.mainNode)
        mainAP.children.add(endNode.mainNode)
    }
}