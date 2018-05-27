package com.codlex.distributed.systems.homework1.peer.messages;

import java.io.Serializable;
import java.util.List;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class GetValueResponse implements Serializable {

	@Getter
	final List<NodeInfo> nodes;

	@Getter
	final String value;

}
