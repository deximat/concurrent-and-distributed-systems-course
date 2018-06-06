package com.codlex.distributed.systems.homework1.peer.operations;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.Settings;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class RefreshBucketOperation {

	private Node localNode;
	private Runnable callback;
	private int bucketToRefresh = 1;
	private int transmiting;
	private int refreshedEntries; // to have same logic

	public RefreshBucketOperation(Node localNode, Runnable onRefreshDone) {
		this.localNode = localNode;
		this.callback = onRefreshDone;
	}

	public void execute() {
		log.debug("{} started refreshing buckets", this.localNode);
		checkIfFinishedAndStoreIfNot();
	}

	private synchronized void checkIfFinishedAndStoreIfNot() {
		if (this.refreshedEntries == KademliaId.ID_LENGTH_BITS - 1) {
			this.callback.run();
			log.debug("{} finished refreshing buckets in {}ms.", this.localNode);
			return;
		}

		int concurrencyAvailable = Settings.ConcurrencyParam - this.transmiting;

		while (this.bucketToRefresh < KademliaId.ID_LENGTH_BITS
				&& concurrencyAvailable > 0) {

			final KademliaId current = this.localNode.getInfo().getId().generateNodeIdByDistance(this.bucketToRefresh);
			new NodeLookup(this.localNode, current, Settings.K, (nodes) -> {
				onLookupSuccess();
			}).execute();

			this.transmiting++;
			this.bucketToRefresh++;
			concurrencyAvailable--;
		}
	}

	private synchronized void onLookupSuccess() {
		this.refreshedEntries++;
		this.transmiting--;
		checkIfFinishedAndStoreIfNot();
	}
}
