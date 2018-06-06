package com.codlex.distributed.systems.homework1.peer.messages;

public enum Messages {
	Join("/join"), Connect("/connect"), Get("/getValue"), FindNodes("/getNodes"), Store("/setValue"), StreamingStarted(
			"/streamingStarted"), Ping("/ping");

	private final String address;

	private Messages(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}
}
