package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StoreValueRequest implements Serializable {
	private KademliaId key;
	private String value;
}
