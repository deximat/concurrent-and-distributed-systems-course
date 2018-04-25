package com.codlex.raf.kids.domaci2.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.WorkerType;
import com.codlex.raf.kids.domaci2.view.GUI;
import com.codlex.raf.kids.domaci2.view.PipelineGUI;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Getter;

public class Pipeline {

	@Getter
	final List<Worker> workers = new ArrayList<>();

	public void addLast(final Worker worker) {

		if (!this.workers.isEmpty()) {
			final Worker last = this.workers.get(this.workers.size() - 1);
			last.setNext(worker);
		}

		this.workers.add(worker);
	}


	public Node produceView() {
		VBox vbox = new VBox();
		String cssLayout = "-fx-border-color: GRAY;\n" +
                "-fx-border-insets: 10;\n" +
                "-fx-border-width: 10;\n" +
                "-fx-border-style: dashed;\n";
		vbox.setStyle(cssLayout);

		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.setPrefWidth(100000);

		Text title = new Text("PIPELINE:");
		title.setFont(Font.font ("Verdana", 40));

		vbox.getChildren().add(title);
		for (Worker worker : this.workers) {
			vbox.getChildren().add(worker.produceView());
		}

		vbox.getChildren().add(buildWorkerChooser());

		return vbox;
	}

	private Node buildWorkerChooser() {
		final AtomicReference<WorkerType> current = new AtomicReference<>();
		HBox hbox = new HBox();
		ChoiceBox<WorkerType> workerTypes = new ChoiceBox<>(FXCollections.observableArrayList(WorkerType.values()));
		workerTypes.getSelectionModel().selectedItemProperty().addListener((object, oldValue, newValue) -> {
			current.set(newValue);
		});
		workerTypes.getSelectionModel().selectFirst();

		hbox.getChildren().add(workerTypes);

		hbox.getChildren().add(GUI.createButton("Add worker", () -> {
			addLast(current.get().produceWorker());
			PipelineGUI.redrawEverything();
		}));

		return hbox;
	}

}
