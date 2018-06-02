package com.codlex.distributed.systems.homework1.peer;

import java.net.InetAddress;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;

public class Settings {

	public static final int K = 1;
	public static long refreshInterval = 1000000;

	public static NodeInfo bootstrapNode = new NodeInfo(new KademliaId(IdType.Node, Region.Serbia, "BOOTSRAP"), HostGetter.getUnsafe(), 1337, 1338);

}
