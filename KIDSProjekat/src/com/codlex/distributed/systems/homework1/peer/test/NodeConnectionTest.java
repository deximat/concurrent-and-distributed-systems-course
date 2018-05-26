package com.codlex.distributed.systems.homework1.peer.test;

import com.codlex.distributed.systems.homework1.peer.Node;

public class NodeConnectionTest {

	private static void basicTest() {
		final Node node1 = new Node(7575);
		System.out.println("Created Node1:" + node1.getInfo().id);

		final Node node2 = new Node(7672);
		System.out.println("Created Node2:" + node2.getInfo().id);

		node2.bootstrap(node1.getInfo());
	}

	private static void bigTest() {
		int basePort = 7574;
		final Node bootstrap = new Node(basePort);
		for (int port = basePort + 1; port < basePort + 3; port++) {
			new Node(port).bootstrap(bootstrap.getInfo());
			System.out.println("New node adding");
		}
	}

	public static void main(String[] args) {
		// basicTest();
		bigTest();
	}
}
