package com.codlex.distributed.systems.homework1.bootstrap.messages;

import java.io.Serializable;
import java.util.List;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class JoinResponse implements Serializable {

	@Getter
	private List<NodeInfo> bootstrapNodes;
}
