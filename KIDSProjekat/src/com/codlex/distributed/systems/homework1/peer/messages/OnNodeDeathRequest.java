package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class OnNodeDeathRequest implements Serializable {

	@Getter
	private NodeInfo me;

	@Getter
	private NodeInfo deadNode;

}
