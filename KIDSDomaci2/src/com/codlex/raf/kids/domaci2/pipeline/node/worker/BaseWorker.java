package com.codlex.raf.kids.domaci2.pipeline.node.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.BaseNode;
import com.codlex.raf.kids.domaci2.pipeline.node.input.Input;
import com.codlex.raf.kids.domaci2.pipeline.node.input.InputType;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;
import com.codlex.raf.kids.domaci2.pipeline.node.output.OutputType;
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
import lombok.Setter;

public abstract class BaseWorker extends BaseNode implements Worker {

	@Setter
	private Worker next;

	private final List<Output> outputs = new ArrayList<>();
	private final List<Input> inputs = new ArrayList<>();

	@Override
	public final void give(final PipelineCollection toProcess) {
		try {
			processAll(toProcess);
		} catch (Exception e) {
			throw new Error("Exception happened in worker: {}", e);
		}
	}

	protected final void onFinish(final PipelineCollection finalResult) {

		for (Output output : this.outputs) {
			output.accept(finalResult);
		}

		if (this.next != null) {
			this.next.give(finalResult);
		}

		super.onFinish(finalResult);
	}


	@Override
	public void addOutput(Output output) {
		this.outputs.add(output);
	}

	public void addInput(Input input) {
		this.inputs.add(input);
	}


	@Override
	public Node produceView() {
		VBox nodeView = (VBox) super.produceView();

		HBox workerView = new HBox();
		workerView.getChildren().add(buildInputs());
		workerView.getChildren().add(nodeView);
		workerView.getChildren().add(buildOutputs());

		return workerView;
	}

	private Node buildOutputs() {
		VBox vbox = new VBox();
		String cssLayout = "-fx-border-color: black;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: dashed;\n";
		vbox.setStyle(cssLayout);

		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.setPrefWidth(500);


		Text title = new Text("Outputs:");
		title.setFont(Font.font ("Verdana", 25));

		vbox.getChildren().add(title);
		for (Output output : this.outputs) {
			vbox.getChildren().add(output.produceView());
		}
		vbox.getChildren().add(buildOutputChooser());
		return vbox;
	}

	private Node buildInputs() {
		VBox vbox = new VBox();
		String cssLayout = "-fx-border-color: black;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: dashed;\n";
		vbox.setStyle(cssLayout);

		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.setPrefWidth(500);

		Text title = new Text("Inputs:");
		title.setFont(Font.font ("Verdana", 25));

		vbox.getChildren().add(title);
		for (Input input : this.inputs) {
			vbox.getChildren().add(input.produceView());
		}
		vbox.getChildren().add(buildInputChooser());

		return vbox;
	}

	private Node buildOutputChooser() {
		HBox hbox = new HBox();

		AtomicReference<OutputType> currentType = new AtomicReference<>();
		ChoiceBox<OutputType> workerTypes = new ChoiceBox<>(FXCollections.observableArrayList(OutputType.values()));
		workerTypes.getSelectionModel().selectedItemProperty().addListener((object, oldValue, newValue) -> {
			currentType.set(newValue);
		});
		workerTypes.getSelectionModel().selectFirst();
		hbox.getChildren().add(workerTypes);
		hbox.getChildren().add(GUI.createButton("Add output", () -> {
			BaseWorker.this.addOutput(currentType.get().produceOutput());
			PipelineGUI.redrawEverything();
		}));

		return hbox;
	}

	private Node buildInputChooser() {
		HBox hbox = new HBox();

		AtomicReference<InputType> currentType = new AtomicReference<>();
		ChoiceBox<InputType> workerTypes = new ChoiceBox<>(FXCollections.observableArrayList(InputType.values()));
		workerTypes.getSelectionModel().selectedItemProperty().addListener((object, oldValue, newValue) -> {
			currentType.set(newValue);
		});
		workerTypes.getSelectionModel().selectFirst();
		hbox.getChildren().add(workerTypes);
		hbox.getChildren().add(GUI.createButton("Add input", () -> {
			currentType.get().produceInput(BaseWorker.this);
			PipelineGUI.redrawEverything();
		}));

		return hbox;
	}


}
