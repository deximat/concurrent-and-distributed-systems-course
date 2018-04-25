package com.codlex.raf.kids.domaci2.tests.basic.nodes.input;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.codlex.raf.kids.domaci2.pipeline.node.input.BaseInput;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.view.GUI;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class SocketInput extends BaseInput {

	private final static String PORT_PARAM = "tcp port";

	public SocketInput(Worker worker) {
		super(worker);
		setParam(PORT_PARAM, 8080);
	}

	private void startServer() {
		execute(() -> {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(getParam(PORT_PARAM));
				while (true) {
					Socket socket = serverSocket.accept();
					execute(() -> processRequest(socket));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (serverSocket != null) {
						serverSocket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private void processRequest(final Socket socket) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream());
			String json = scanner.nextLine();
			System.out.println(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	@Override
	public Node produceView() {
		VBox vbox = (VBox) super.produceView();
		vbox.getChildren().add(GUI.createButton("Start server", () -> {
			startServer();
		}));
		return vbox;
	}


	private void test() {
		// echo '{"id":1, "method":"object.deleteAll", "params":["myParam"]}' | nc localhost 3994
	}

}
