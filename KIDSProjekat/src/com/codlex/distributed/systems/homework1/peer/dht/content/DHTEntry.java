package com.codlex.distributed.systems.homework1.peer.dht.content;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor

@EqualsAndHashCode(of = "id")
@Getter
public abstract class DHTEntry {
	protected KademliaId id;
}
