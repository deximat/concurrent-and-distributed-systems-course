package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.Getter;

public class CollectionBatcher {
	
	final AtomicInteger processedCount = new AtomicInteger();
	private final List<PipelineCollection> batches;
	
	public List<PipelineCollection> getBatches() {
		return this.batches;
	}
	
	private ConcurrentLinkedQueue<PipelineCollection> results = new ConcurrentLinkedQueue<>();
	
	
	public CollectionBatcher(final PipelineCollection collection, final int batchSize) {
		this.batches = split(collection, batchSize);
	}

	private List<PipelineCollection> split(PipelineCollection collection, int batchSize) {
		final List<PipelineCollection> collections = new ArrayList<>();
		
		PipelineCollection batchCollection = PipelineCollection.create();
		
		for (final PipelineData data : collection) {
			
			batchCollection.put(data);
			
			if (batchCollection.size() >= batchSize) {
				collections.add(batchCollection);
				batchCollection = PipelineCollection.create();
			}
			
		}
		
		if (!batchCollection.isEmpty()) {
			collections.add(batchCollection);
		}
		
		return collections;
	}

	public boolean submitResultAndShouldMerge(final PipelineCollection result) {
		if (result != null) {
			this.results.add(result);
		}
		
		int processed = this.processedCount.incrementAndGet();
		return processed == this.batches.size();
	}

	public List<PipelineCollection> getAllResults() {
		return new ArrayList<>(this.results);
	}

}
