package com.codlex.distributed.systems.homework1.peer.dht.content;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

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
}
