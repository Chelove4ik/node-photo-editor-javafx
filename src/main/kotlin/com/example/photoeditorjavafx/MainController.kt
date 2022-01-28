package com.example.photoeditorjavafx

import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.shape.CubicCurve

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

    private fun initialize() {

    }
}