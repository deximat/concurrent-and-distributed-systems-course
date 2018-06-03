package com.codlex.distributed.systems.homework1.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {
	public static Gson get() {
		return new GsonBuilder()
				.create();
	}
}
