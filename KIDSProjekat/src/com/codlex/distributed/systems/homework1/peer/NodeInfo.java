package com.codlex.distributed.systems.homework1.peer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(of={"id"})
public class NodeInfo {

	@Getter
	public KademliaId id;

	public String address;
	public int port;
	public int streamingPort;

	public String toString() {
		return "[" + this.id.toHexShort() + "]";
	}

}
