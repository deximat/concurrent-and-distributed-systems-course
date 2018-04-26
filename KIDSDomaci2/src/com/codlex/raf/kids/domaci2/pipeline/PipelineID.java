package com.codlex.raf.kids.domaci2.pipeline;

import lombok.Data;

@Data
public class PipelineID {
	private final int producerId;
	private final int dataId;

	public static PipelineID of(int nodeId, int dataId) {
		return new PipelineID(nodeId, dataId);
	}
}
