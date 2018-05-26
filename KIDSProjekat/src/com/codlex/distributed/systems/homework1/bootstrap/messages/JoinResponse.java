package com.codlex.distributed.systems.homework1.bootstrap.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class JoinResponse implements Serializable {
	@Getter
	private NodeInfo bootstrapNode;
}
