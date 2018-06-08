package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;

public enum Messages {
	// Bootstrap
	Join("/join"),
	OnNodeDeath("/onNodeDeath"),

	// Client
	Ping("/ping"),
	FindNodes("/findNodes"),
	Get("/getValue"),
	Store("/storeValue"),
	StreamingStarted("/streamingStarted");

	private final String address;

	private Messages(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}

	public long getTimeout(Serializable message) {
		if (message instanceof StoreValueRequest) {
			StoreValueRequest storeRequest = (StoreValueRequest) message;
			if (storeRequest.getValue().get() instanceof Video) {
				return Settings.VideoUploadTimeoutMillis;
			}
		}
		return Settings.SoftTimeoutMillis;
	}
}
