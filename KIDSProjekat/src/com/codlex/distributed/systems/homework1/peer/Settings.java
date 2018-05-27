package com.codlex.distributed.systems.homework1.peer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;

public class Settings {

	public static final int K = 2;
	public static long refreshInterval = 10000;

	public static NodeInfo bootstrapNode = new NodeInfo(new KademliaId("0000"), "localhost", 1337);

	// new DefaultConfiguration()

}
