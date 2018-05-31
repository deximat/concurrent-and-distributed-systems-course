package com.codlex.distributed.systems.homework1.core.streaming;


import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.Node;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final Node node;

	public ServerHandler(Node node) {
		this.node = node;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception {
		final KademliaId videoId = getVideoId(request);
		String fileName = this.node.getVideoForStreaming(videoId);
		log.debug("Streaming started: {}", fileName);

		File file = new File("videos/", fileName);
		if (file.isHidden() || !file.exists()) {
			System.err.println("NOT_FOUND");
			return;
		}

		final RandomAccessFile randomAccessFile;

		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			return;
		}

		long fileLength = randomAccessFile.length();

		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpHeaders.setContentLength(response, fileLength);

		if (HttpHeaders.isKeepAlive(request)) {
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		ctx.write(response);

		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;

		sendFileFuture = ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0,fileLength)
									, ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future,long progress, long total) {
				if (total < 0) {
					System.err.println(future.channel()+" Transfer progress: " + progress);
				} else {
					System.err.println(future.channel()+" Transfer progress: " + progress + " / " + total);
				}
			}
			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				System.err.println(future.channel() + " Transfer complete.");
				ServerHandler.this.node.onVideoStreamingEnd();
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!HttpHeaders.isKeepAlive(request)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private KademliaId getVideoId(FullHttpRequest request) {
		String idString = URLDecoder.decode(request.getUri().replace("/", ""));
		return new KademliaId(idString.getBytes());
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			System.err.println("INTERNAL_SERVER_ERROR");
		}
	}
}