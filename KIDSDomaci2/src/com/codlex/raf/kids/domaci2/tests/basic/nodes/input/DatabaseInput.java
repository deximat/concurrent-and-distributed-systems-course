package com.codlex.raf.kids.domaci2.tests.basic.nodes.input;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.base.NodeState;
import com.codlex.raf.kids.domaci2.pipeline.node.input.BaseInput;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.view.GUI;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.core.runtimeDependencies.CreateLombokRuntimeApp;

public class DatabaseInput extends BaseInput {

	public static class Params {
		public static final String DATABASE_URL = "databaseUrl";
		public static final String DATABASE_USERNAME = "databaseUsername";
		public static final String DATABASE_PASSWORD = "databasePassword";
		public static final String DATABASE_QUERY = "databaseQuery";
	}

	public DatabaseInput(Worker worker) {
		super(worker);
		// default params
		setParam(Params.DATABASE_URL, "jdbc:postgresql://localhost/wizardworld");
		setParam(Params.DATABASE_USERNAME, "babe");
		setParam(Params.DATABASE_PASSWORD, "qwe123");
		setParam(Params.DATABASE_QUERY, "select * from public.user ;");
	}

	public void triggerRead() {
		execute(() -> {
			PipelineCollection result = doRead();
			if (result != null) {
				processAll(result);
			}
		});
	}

	private PipelineCollection doRead() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(getParam(Params.DATABASE_URL), getParam(Params.DATABASE_USERNAME),
					getParam(Params.DATABASE_PASSWORD));

			Statement stmt = connection.createStatement();
			String query = getParam(Params.DATABASE_QUERY);
			ResultSet rs = stmt.executeQuery(query);

			final PipelineCollection rows = PipelineCollection.create(generateFullId());
			while (rs.next()) {
				rows.put(buildPipelineData(rs));
			}

			return rows;

		} catch (Throwable e) {
			e.printStackTrace();
			setState(NodeState.Stopped);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					setState(NodeState.Stopped);
				}
			}
		}

		return null;
	}

	protected PipelineCollection processBatch(PipelineCollection toProcess) {
//		PipelineCollection processedColleciton = PipelineCollection.create();
//		for (PipelineData data : toProcess) {
//			// do some heavy lifting on data
//			// processedColleciton.put(PipelineData.ofInt(data.getIntValue("rating")));
//		}
//		return processedColleciton;
		return toProcess;
	}

	private PipelineData buildPipelineData(ResultSet rs) throws SQLException {
		PipelineData data = PipelineData.create();

		final ResultSetMetaData metaData = rs.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			data.setValue(metaData.getColumnName(i), rs.getObject(i));
		}

		return data;
	}

	@Override
	public Node produceView() {
		VBox vbox = (VBox) super.produceView();
		vbox.getChildren().add(GUI.createButton("Trigger", () -> {
			triggerRead();
		}));
		return vbox;
	}

}
