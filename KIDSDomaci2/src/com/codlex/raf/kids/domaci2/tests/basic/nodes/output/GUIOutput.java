package com.codlex.raf.kids.domaci2.tests.basic.nodes.output;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.base.BaseNode;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;
import com.codlex.raf.kids.domaci2.view.GUI;
import com.codlex.raf.kids.domaci2.view.PipelineGUI;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class GUIOutput extends BaseNode implements Output {

    private final ObservableList<PipelineData> data = FXCollections.observableArrayList();
	private TableView<PipelineData> table = new TableView<PipelineData>(data);
	private AtomicBoolean columnsSet = new AtomicBoolean();

    public void initColumns(List<String> keys) {
    	this.table.getColumns().clear();
    	for (final String key : keys) {
	    	TableColumn<PipelineData, String> column = new TableColumn<>(key);
	        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PipelineData, String>, ObservableValue<String>>() {
	            @Override
	            public ObservableValue<String> call(TableColumn.CellDataFeatures<PipelineData, String> p) {
	            	Object value = p.getValue().getValue(key);
	            	String text = value == null ? "" : value.toString();
	                return new SimpleStringProperty(text);
	            }
	        });
	        this.table.getColumns().add(column);
    	}
    }

    public GUIOutput() {
	}

	@Override
	public void accept(PipelineCollection collection) {
		execute(() -> {
			Platform.runLater(() -> {
				for (PipelineData data : collection) {
					// if (!this.columnsSet.getAndSet(true)) {
						initColumns(data.keys());
					//}

					GUIOutput.this.data.add(data);
				}
			});
		});
	}

	@Override
	protected PipelineCollection processBatch(PipelineCollection toProcess) {

		return super.processBatch(toProcess);
	}

	@Override
	protected void onFinish(PipelineCollection toProcess) {

	}

	@Override
	public Node produceView() {
		VBox vbox = new VBox();
		vbox.getChildren().add(this.table);
		vbox.getChildren().add(GUI.createButton("Clear", () -> {
			Platform.runLater(() -> {
				this.data.clear();
			});
		}));
		return vbox;
	}

}
