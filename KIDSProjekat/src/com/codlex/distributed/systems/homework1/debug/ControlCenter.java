package com.codlex.distributed.systems.homework1.debug;

import java.util.List;

import com.codlex.distributed.systems.homework1.bootstrap.BootstrapNode;
import com.codlex.distributed.systems.homework1.peer.Settings;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ControlCenter extends Application {

	private Stage stage;

	private Node buildNodeCreator() {
		VBox vbox = new VBox();
		final TextField numberOfNodes = new TextField("1");
		vbox.getChildren().add(numberOfNodes);
		Button button = new Button("Add node(s)");
		button.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				int numberOfNodesInt = Integer.parseInt(numberOfNodes.getText());
				NodeFactory.create(numberOfNodesInt);
			}
		});
		vbox.getChildren().add(button);
		return vbox;
	}

	public static void main(String[] args) throws InterruptedException {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		this.stage = stage;
		stage.setTitle("Control Center");

		new BootstrapNode(Settings.bootstrapNode);

		redrawEverything();
		stage.show();
	}

	private void redrawEverything() {
		VBox vbox = new VBox(10);
		List<Node> vboxChildren = vbox.getChildren();
		vboxChildren.add(buildNodeCreator());
		Scene scene = new Scene(vbox, 1024, 768);
		this.stage.setScene(scene);
	}

}
