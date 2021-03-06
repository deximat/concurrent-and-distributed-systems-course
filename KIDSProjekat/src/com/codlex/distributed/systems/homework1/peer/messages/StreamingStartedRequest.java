package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StreamingStartedRequest implements Serializable {

	@Getter
	private final NodeInfo node;

	@Getter
	private final KademliaId id;

}
