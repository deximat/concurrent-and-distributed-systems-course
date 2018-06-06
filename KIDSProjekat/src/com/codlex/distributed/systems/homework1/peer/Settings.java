package com.codlex.distributed.systems.homework1.peer;

import java.util.concurrent.TimeUnit;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;

public class Settings {
	// number of concurrent requests allowed
	public static final int ConcurrencyParam = 3;

	public static final int K = 3;
	public static final int BucketSize = 2;
	public static final long REFRESH_BUCKETS_VIEW = 10;

	public static final long ViewExpiryMillis = TimeUnit.DAYS.toMillis(1);

	public static long REFRESH_INTERVAL_SECONDS = 20;

	public static NodeInfo bootstrapNode = new NodeInfo(new KademliaId(IdType.Node, Region.Europe, "BOOTSRAP"), HostGetter.getUnsafe(), 1337, 1338);

}
