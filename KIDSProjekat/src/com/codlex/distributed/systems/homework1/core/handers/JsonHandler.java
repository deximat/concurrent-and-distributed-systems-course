package com.codlex.distributed.systems.homework1.core.handers;

import java.io.Serializable;

import com.google.gson.Gson;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonHandler<Request extends Serializable, Response extends Serializable>
		implements Handler<RoutingContext> {

	private final Class<Request> clazz;

	public JsonHandler(final Class<Request> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void handle(final RoutingContext event) {
		// log.debug("Received message: {} deserialization to: {} ",  event.getBodyAsString(), JsonHandler.this.clazz.getSimpleName());
		final Gson gson = new Gson();
		Request message = gson.fromJson(event.getBodyAsString(), JsonHandler.this.clazz);

		event.response().end(gson.toJson(callback(message)));
	}

	public abstract Response callback(Request message);

}
