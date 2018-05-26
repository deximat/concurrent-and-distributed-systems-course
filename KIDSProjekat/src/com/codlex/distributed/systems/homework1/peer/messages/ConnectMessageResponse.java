package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

public class ConnectMessageResponse implements Serializable {
	public int id;

	public ConnectMessageResponse(int id) {
		this.id = id;
	}
	
	
}
