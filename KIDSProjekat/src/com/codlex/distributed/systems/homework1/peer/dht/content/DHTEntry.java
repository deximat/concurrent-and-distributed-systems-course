package com.codlex.distributed.systems.homework1.peer.dht.content;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Settings;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
public abstract class DHTEntry implements Comparable<DHTEntry> {
	protected KademliaId id;

	public DHTEntry getWithoutData() {
		// default implementation does nothing.
		return this;
	}

	@Override
	public int compareTo(DHTEntry o) {
		return id.toBigInt().compareTo(o.id.toBigInt());
	}

	public int getDynamicRedundancy() {
		return Math.max(Settings.K, calculateDesiredRedundancy());
	}

	protected int calculateDesiredRedundancy() {
		return 0;
	}
}
