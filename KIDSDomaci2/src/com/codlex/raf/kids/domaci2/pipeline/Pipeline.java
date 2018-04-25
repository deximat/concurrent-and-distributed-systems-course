package com.codlex.raf.kids.domaci2.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;

public class Pipeline {
	
	final List<Worker> workers = new ArrayList<>();
	
	public void addLast(final Worker worker) {
		
		if (!this.workers.isEmpty()) {
			final Worker last = this.workers.get(this.workers.size() - 1);
			last.setNext(worker);
		}
		
		this.workers.add(worker);
	}
	
	
	
}
