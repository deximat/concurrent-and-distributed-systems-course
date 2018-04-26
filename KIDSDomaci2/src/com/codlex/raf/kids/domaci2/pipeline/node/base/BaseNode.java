package com.codlex.raf.kids.domaci2.pipeline.node.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;
import com.codlex.raf.kids.domaci2.pipeline.data.CollectionBatcher;
import com.codlex.raf.kids.domaci2.pipeline.data.CollectionCollector;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.output.GUIOutput;
import com.codlex.raf.kids.domaci2.view.GUI;
import com.codlex.raf.kids.domaci2.view.PipelineGUI;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Getter;

public abstract class BaseNode implements Node {

	public static class Params {
		public static final String THREADS = "threads";
	}

	private final Map<String, Object> params = new ConcurrentHashMap<>();
	private final Map<PipelineID, CollectionCollector> collectors = new ConcurrentHashMap<>();

	private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

	private final AtomicInteger DATA_ID_GENERATOR = new AtomicInteger();

	@Override
	public List<String> getParams() {
		return new ArrayList<>(params.keySet());
	}

	@Override
	public void setParam(String parameterName, Object value) {
		this.params.put(parameterName, value);

		// to have live update of thread count
		if (Params.THREADS.equals(parameterName)) {
			this.threadPool.setCorePoolSize((Integer) value);
		}
	}

	@Getter
	private final int ID;

	private final ThreadPoolExecutor threadPool;
	private final AtomicReference<NodeState> state = new AtomicReference<>(NodeState.Waiting);

	public BaseNode() {
		final int defaultThreadsCount = 10;
		ID = generateId();
		this.threadPool = buildPool(defaultThreadsCount);
		setParam(Params.THREADS, defaultThreadsCount);
	}

	private final int generateId() {
		return ID_GENERATOR.incrementAndGet();
	}

	protected final PipelineID generateFullId() {
		return generateId(DATA_ID_GENERATOR.incrementAndGet());
	}

	protected final PipelineID generateId(int dataId) {
		return PipelineID.of(getID(), dataId);
	}

	protected final ThreadPoolExecutor buildPool(int threads) {
		return new ThreadPoolExecutor(threads, threads, 100, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	protected void removeCollector(PipelineID id) {
		this.collectors.remove(id);
	}

	protected CollectionCollector getCollector(PipelineID id, int size) {
		this.collectors.putIfAbsent(id, new CollectionCollector(id, size));
		return this.collectors.get(id);
	}

	protected final void processAll(final PipelineCollection toProcess) {
		setState(NodeState.Active);
		try {
			final CollectionBatcher batcher = new CollectionBatcher(toProcess, getBatchSize(toProcess.size()));
			for (final PipelineCollection batch : batcher.getBatches()) {
				this.threadPool.submit(() -> {
					try {
						// if (true)
						// if (this instanceof Worker)
						// throw new RuntimeException();
						final PipelineCollection result = processBatch(batch);
						Thread.sleep(300);
						CollectionCollector collector = getCollector(batch.getID(), batch.getPartsCount());
						final boolean shouldMerge = collector.submitResultAndShouldMerge(result);
						if (shouldMerge) {

							final PipelineCollection mergedResults = mergeBatches(collector.getAllResults());
							if (mergedResults != null) {
								onFinish(mergedResults);
							}

							removeCollector(batch.getID());
						}
					} catch (Throwable e) {
						if (this instanceof Worker) {
							System.out.println("Exception happened in worker:");
							e.printStackTrace();
							System.exit(-1);
						} else {
							System.out.println("Exception happened in in/out node.");
							setState(NodeState.Stopped);
							e.printStackTrace();
						}
					}
				});
			}
		} catch (Throwable e) {
			if (this instanceof Worker) {
				System.out.println("Exception happened in worker:");
				e.printStackTrace();
				System.exit(-1);
			} else {
				System.out.println("Exception happened in in/out node.");
				setState(NodeState.Stopped);
				e.printStackTrace();
			}
		}

	}

	protected void setState(NodeState value) {
		this.state.set(value);
		Platform.runLater(() -> {
			PipelineGUI.redrawEverything();
		});
	}

	protected PipelineCollection processBatch(final PipelineCollection toProcess) {
		return toProcess;
	}

	protected PipelineCollection mergeBatches(final List<PipelineCollection> toProcess) {
		final PipelineCollection result = PipelineCollection.create(toProcess.get(0).getID());

		for (PipelineCollection batch : toProcess) {
			for (PipelineData data : batch) {
				result.put(data);
			}
		}

		return result;
	}

	protected void onFinish(final PipelineCollection toProcess) {
		setState(NodeState.Waiting);
	}

	protected int getBatchSize(final int total) {
		return Math.max(1, total / this.threadPool.getCorePoolSize());
	}

	protected final void execute(Runnable command) {
		this.threadPool.execute(command);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getParam(String paramName) {
		return (T) this.params.get(paramName);
	}

	protected VBox produceParamSetupView() {
		VBox vbox = new VBox();
		String cssLayout = "-fx-border-color: green;\n" + "-fx-border-insets: 5;\n" + "-fx-border-width: 3;\n"
				+ "-fx-border-style: dashed;\n" + "-fx-padding: 20; \n";
		vbox.setStyle(cssLayout);

		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.setPrefWidth(500);
		vbox.setMinHeight(200);

		Text title = new Text(getClass().getSimpleName());
		title.setFont(Font.font("Verdana", 25));

		vbox.getChildren().add(title);
		for (Entry<String, Object> param : this.params.entrySet()) {
			HBox hbox = new HBox();
			hbox.getChildren().add(new Text(param.getKey()));
			final TextField text = new TextField(param.getValue().toString());
			hbox.getChildren().add(text);
			hbox.getChildren().add(GUI.createButton("💾", () -> {
				setParam(param.getKey(), GUI.parseToType(text.textProperty().get(), param.getValue().getClass()));
			}));
			vbox.getChildren().add(hbox);
		}

		vbox.getChildren().add(new Text("State:" + this.state.get()));

		return vbox;
	}

	public javafx.scene.Node produceView() {
		return produceParamSetupView();
	}

}
