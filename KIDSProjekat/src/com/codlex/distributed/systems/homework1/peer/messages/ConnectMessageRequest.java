package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

public class ConnectMessageRequest implements Serializable {
	public NodeInfo node;
	
	public ConnectMessageRequest(NodeInfo info) {
		this.node = info;
	}

	@Override
	public String toString() {
		return "ConnectMessageRequest [node=" + node + "]";
	}
}
