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

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;
import com.codlex.raf.kids.domaci2.pipeline.data.CollectionBatcher;
import com.codlex.raf.kids.domaci2.pipeline.data.CollectionCollector;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.view.GUI;

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
		final CollectionBatcher batcher = new CollectionBatcher(toProcess, getBatchSize(toProcess.size()));
		for (final PipelineCollection batch : batcher.getBatches()) {
			this.threadPool.submit(() -> {
				try {
					final PipelineCollection result = processBatch(batch);
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
					e.printStackTrace();
				}
			});
		}
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

	protected abstract void onFinish(final PipelineCollection toProcess);

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
		String cssLayout = "-fx-border-color: green;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: dashed;\n" +
                "-fx-padding: 20; \n";
		vbox.setStyle(cssLayout);

		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.setPrefWidth(500);
		vbox.setMinHeight(200);

		Text title = new Text(getClass().getSimpleName());
		title.setFont(Font.font ("Verdana", 25));

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

		return vbox;
	}

	public javafx.scene.Node produceView() {
		return produceParamSetupView();
	}

}
