package com.codlex.distributed.systems.homework1.core.streaming;
import com.codlex.distributed.systems.homework1.peer.Node;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StreamingServer {

	public class ServerInitializer extends ChannelInitializer<SocketChannel>{

		private Node node;

		public ServerInitializer(Node node) {
			this.node = node;
		}

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			ChannelPipeline pipeline = arg0.pipeline();
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(65536));
			pipeline.addLast(new ChunkedWriteHandler());
			pipeline.addLast(new ServerHandler(node));
		}
	}

	public StreamingServer(final Node node, int port) {
		EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
		EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.TRACE))
			.childHandler(new ServerInitializer(node));
			serverBootstrap.bind(port).sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
