package com.codlex.raf.kids.domaci2.view;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.GUIOutput;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class TableFromCollection {

	public static TableView build(PipelineCollection collection) {

	    final ObservableList<PipelineData> dataModel = FXCollections.observableArrayList();
	    for (PipelineData data : collection) {
	    	dataModel.add(data);
	    }

		TableView<PipelineData> table = new TableView<PipelineData>(dataModel);
		initColumns(table, collection.first().keys());
        Scene scene  = new Scene(table, 1024, 500);
		return table;
	}

    public static void initColumns(TableView table, List<String> keys) {
    	table.setMinHeight(500);
    	table.setMinWidth(1000);
    	table.getColumns().clear();
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
	        table.getColumns().add(column);
    	}
    }

}
