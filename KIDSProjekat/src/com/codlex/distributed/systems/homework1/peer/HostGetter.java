package com.codlex.distributed.systems.homework1.peer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostGetter {
	public static String getUnsafe() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.error("", e);
			return null;
		}

	}
}
