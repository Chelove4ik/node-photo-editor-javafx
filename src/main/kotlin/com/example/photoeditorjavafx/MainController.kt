package com.example.photoeditorjavafx

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.layout.AnchorPane

class MainController {
    @FXML
    private lateinit var nodeListSP: ScrollPane

    @FXML
    lateinit var mainSP: ScrollPane

    @FXML
    private fun createNodesList() {
        val b = FloatNode()
        (mainSP.content as AnchorPane).children.add(b.mainNode)

        val c = PrintNode()
        (mainSP.content as AnchorPane).children.add(c.mainNode)
    }

    @FXML
    private fun createNodeFromStringIdInEvent(event: ActionEvent) {
        createNodeFromString((event.source as MenuItem).id)
    }

    private fun createNodeFromString(str: String) {
        val newNode: BaseNode? = when (str) {
            "Float" -> FloatNode()
            "Int" -> IntNode()
            "String" -> StringNode()
//            "Image" -> PrintNode()
//            "AddText" -> PrintNode()
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
            (mainSP.content as AnchorPane).children.add(newNode.mainNode)
        } else {
            // TODO мб сообщение об ошибке
        }
    }
}