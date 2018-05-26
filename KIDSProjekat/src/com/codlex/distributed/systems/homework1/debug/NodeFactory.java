package com.codlex.distributed.systems.homework1.debug;

import java.util.concurrent.atomic.AtomicInteger;

import com.codlex.distributed.systems.homework1.peer.Node;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeFactory {

	final static AtomicInteger portGenerator = new AtomicInteger(8000);

	public static void create(int numberOfNodesInt) {
		for (int i = 0; i < numberOfNodesInt; i++) {
			new Node(portGenerator.incrementAndGet()).bootstrap();
		}
	}

}
