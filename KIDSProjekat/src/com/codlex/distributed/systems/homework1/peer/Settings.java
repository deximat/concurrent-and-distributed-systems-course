package com.codlex.distributed.systems.homework1.peer;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;

public class Settings {
	public static final int K = 3;
	public static final int BucketSize = 2;
	public static final long REFRESH_BUCKETS_VIEW = 10;
	public static long REFRESH_INTERVAL_SECONDS = 120;

	public static NodeInfo bootstrapNode = new NodeInfo(new KademliaId(IdType.Node, Region.Europe, "BOOTSRAP"), HostGetter.getUnsafe(), 1337, 1338);

}
