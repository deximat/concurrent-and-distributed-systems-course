package com.codlex.distributed.systems.homework1.bootstrap.messages;

import java.io.Serializable;

import com.codlex.distributed.systems.homework1.peer.NodeInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JoinRequest  implements Serializable {

	@Getter
	private NodeInfo info;

}
