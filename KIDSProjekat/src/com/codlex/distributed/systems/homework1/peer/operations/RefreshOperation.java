package com.codlex.distributed.systems.homework1.peer.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.codlex.distributed.systems.homework1.peer.Node;
import com.codlex.distributed.systems.homework1.peer.Settings;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;

public class RefreshOperation {

	private Node localNode;
	private Runnable callback;
	private List<DHTEntry> values;
	private int entryToRefresh;
	private int transmiting;
	private Consumer<DHTEntry> removeFunction;
	private int refreshedEntries;

	public RefreshOperation(Node localNode, Collection<DHTEntry> values, Consumer<DHTEntry> remove, Runnable onRefreshDone) {
		this.localNode = localNode;
		this.callback = onRefreshDone;
		this.values = new ArrayList<>(values);
		this.removeFunction = remove;
	}

	public void execute() {
		checkIfFinishedAndStoreIfNot();
	}

	private synchronized void checkIfFinishedAndStoreIfNot() {
		if (this.refreshedEntries == this.values.size()) {
			this.callback.run();
			return;
		}

		int concurrencyAvailable = Settings.ConcurrencyParam - this.transmiting;

		while (this.entryToRefresh < this.values.size()
				&& concurrencyAvailable > 0) {

			DHTEntry value = this.values.get(this.entryToRefresh);
			new StoreOperation(this.localNode, value, (closestNodes) -> {
				onStoreSuccess();
				if (!closestNodes.contains(this.localNode.getInfo())) {
					this.removeFunction.accept(value);
				}
			}).store();

			this.transmiting++;
			this.entryToRefresh++;
			concurrencyAvailable--;
		}
	}

	private synchronized void onStoreSuccess() {
		this.refreshedEntries++;
		this.transmiting--;
		checkIfFinishedAndStoreIfNot();
	}
}
