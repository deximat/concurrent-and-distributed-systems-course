package com.codlex.raf.kids.domaci2.pipeline.node.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.codlex.raf.kids.domaci2.pipeline.data.CollectionBatcher;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;

import lombok.Getter;

public abstract class BaseNode implements Node {

	public static class Params { 
		public static final String THREADS = "threads";
	}
	
	private final Map<String, Object> params = new ConcurrentHashMap<>();
	
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

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
	
	protected final ThreadPoolExecutor buildPool(int threads) {
		return new ThreadPoolExecutor(threads, threads, 100, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	protected final void processAll(final PipelineCollection toProcess) {
		final CollectionBatcher batcher = new CollectionBatcher(toProcess, getBatchSize(toProcess.size()));
		for (final PipelineCollection batch : batcher.getBatches()) {
			this.threadPool.submit(() -> {
				try {
					final PipelineCollection result = processBatch(batch);
					final boolean shouldMerge = batcher.submitResultAndShouldMerge(result);
					if (shouldMerge) {
						final PipelineCollection mergedResults = mergeBatches(batcher.getAllResults());
						onFinish(mergedResults);
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
		final PipelineCollection result = PipelineCollection.create();
		
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
	
}
