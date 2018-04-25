package com.codlex.raf.kids.domaci2.pipeline.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;

import lombok.ToString;

@ToString
public class BasePipelineCollection implements PipelineCollection {
	
	private final List<PipelineData> backingCollection = new ArrayList<>();
	
	@Override
	public Iterator<PipelineData> iterator() {
		return this.backingCollection.iterator();
	}

	@Override
	public PipelineID getID() {
		return null;
	}

	@Override
	public PipelineData peek(PipelineID id) {
		return null;
	}

	@Override
	public PipelineData take() {
		return null;
	}

	@Override
	public PipelineData first() {
		return this.backingCollection.get(0);
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

}
