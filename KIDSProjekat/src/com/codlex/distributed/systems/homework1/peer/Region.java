package com.codlex.distributed.systems.homework1.peer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

public enum Region {
	Serbia, Tokio, America, RestOfEurope, Unknown;

	public String getCode() {
		StringBuilder builder = new StringBuilder();
		builder.append(name().substring(0, Math.min(name().length(), KademliaId.ID_LENGTH_REGION - 1)));
		while (builder.length() < KademliaId.ID_LENGTH_REGION) {
			builder.append("-");
		}

		return builder.toString();
	}
}
