package com.codlex.distributed.systems.homework1.peer;

import java.util.concurrent.TimeUnit;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;

public class Settings {

	// Kadelmia params

	public static NodeInfo bootstrapNode = new NodeInfo(new KademliaId(IdType.Node, Region.Europe, "BOOTSRAP"), HostGetter.getUnsafe(), 1337, 1338);

	// number of concurrent requests allowed
	public static final int ConcurrencyParam = 3;
	public static final int BucketSize = 2;
	public static long RefreshIntervalSeconds = 20;

	// failure settings
	public static final long SoftTimeoutMillis = 1000;
	public static final long HardTimeoutMillis = 5000;
	// because my upload is single request, it will have long timeout time
	public static final long VideoUploadTimeoutMillis = TimeUnit.MINUTES.toMillis(2);


	// redundancy settings
	public static final int K = 3; // this is needed to guarantee uptime (two nodes can go down at a time)
	public static final int ViewsToStartDynamicRedundancy = 10;
	public static final int LinearDinamicRedundancyFactorPerView = 5; // 5 meaning if we have 10 views, we should have 2 copies
	public static final long ViewExpiryMillis = TimeUnit.DAYS.toMillis(1);


	// GUI settings
	public static final long RefreshBucketsViewSeconds = 10;

}
