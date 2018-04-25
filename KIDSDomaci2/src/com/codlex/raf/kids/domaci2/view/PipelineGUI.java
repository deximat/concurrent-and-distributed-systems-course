package com.codlex.raf.kids.domaci2.view;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.Pipeline;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PipelineGUI extends Application {

	private static Stage stage;

	private static final Pipeline pipeline = new Pipeline();


	@Override
	public void start(final Stage stage) throws Exception {
		PipelineGUI.stage = stage;
        stage.setTitle("Pipeline");
        Scene scene  = new Scene(new VBox(), 1024, 768);
        stage.setScene(scene);
        stage.show();
        redrawEverything();
	}

	public static void redrawEverything() {
		VBox vbox = new VBox(10);
		List<Node> vboxChildren = vbox.getChildren();
		vboxChildren.add(buildPipelineBuilder());
		stage.getScene().setRoot(vbox);
	}

	private static Node buildPipelineBuilder() {
		VBox vbox = new VBox();
		vbox.getChildren().add(pipeline.produceView());
		return vbox;
	}

	public static void main(String[] args) {
		launch(new String[0]);
	}

}

