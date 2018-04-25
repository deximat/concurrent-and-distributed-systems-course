package com.codlex.raf.kids.domaci2.pipeline.node.worker;

import com.codlex.raf.kids.domaci2.tests.basic.nodes.worker.AverageWorker;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.worker.RangeSplitterWorker;
import com.codlex.raf.kids.domaci2.tests.basic.nodes.worker.SumWorker;

public enum WorkerType {
	RangeSplitter, Average, Sum;

	public Worker produceWorker() {
		switch (this) {

		case Sum:
			return new SumWorker();
		case Average:
			return new AverageWorker();

		case RangeSplitter:
			return new RangeSplitterWorker();
		}

		return null;
	}
}
