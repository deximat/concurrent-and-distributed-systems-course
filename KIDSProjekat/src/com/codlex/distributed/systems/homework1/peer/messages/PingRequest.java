package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.Getter;

public class PingRequest implements Serializable {

	@Getter
	public NodeInfo node;

	public PingRequest(NodeInfo info) {
		this.node = info;
	}
}
