package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@ToString
public class BasePipelineCollection implements PipelineCollection {

	@Getter
	@Setter
	private PipelineID ID;

	@Getter
	@Setter
	private int partsCount;

	private final BlockingQueue<PipelineData> backingCollection = new LinkedBlockingQueue<PipelineData>();

	public BasePipelineCollection(PipelineID id) {
		this.ID = id;
	}

	@Override
	public Iterator<PipelineData> iterator() {
		return this.backingCollection.iterator();
	}

	@Override
	public PipelineData peek(PipelineID id) {
		for (PipelineData data : this.backingCollection) {
			if (data.getID().equals(id)) {
				return data;
			}
		}
		return null;
	}

	@Override
	public PipelineData take() {
		try {
			return this.backingCollection.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PipelineData first() {
		return this.backingCollection.peek();
	}

	@Override
	public void put(PipelineData data) {
		this.backingCollection.add(data);
	}

	@Override
	public int size() {
		return this.backingCollection.size();
	}

	@Override
	public boolean isEmpty() {
		return this.backingCollection.isEmpty();
	}

	@Override
	public void addAll(List<PipelineData> unmerged) {
		this.backingCollection.addAll(unmerged);
	}


}
