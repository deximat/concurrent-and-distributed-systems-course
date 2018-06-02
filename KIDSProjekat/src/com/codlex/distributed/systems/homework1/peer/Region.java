package com.codlex.distributed.systems.homework1.peer;

import java.util.ArrayList;
import java.util.List;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

public enum Region {

	Europe, America, Asia, Unknown;

	public String getCode() {
		StringBuilder builder = new StringBuilder();
		builder.append(name().substring(0, Math.min(name().length(), KademliaId.ID_LENGTH_REGION - 1)));
		while (builder.length() < KademliaId.ID_LENGTH_REGION) {
			builder.append("-");
		}

		return builder.toString();
	}

	public static List<Region> realValues() {
		List<Region> values = new ArrayList<>();

		for (Region region : values()) {
			if (region != Unknown) {
				values.add(region);
			}
		}

		return values;
	}
}
