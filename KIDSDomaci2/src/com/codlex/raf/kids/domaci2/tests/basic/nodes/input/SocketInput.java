package com.codlex.raf.kids.domaci2.tests.basic.nodes.input;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.codlex.raf.kids.domaci2.pipeline.PipelineID;
import com.codlex.raf.kids.domaci2.pipeline.data.CollectionCollector;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.data.PipelineData;
import com.codlex.raf.kids.domaci2.pipeline.node.input.BaseInput;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.Worker;
import com.codlex.raf.kids.domaci2.view.GUI;
import com.google.gson.Gson;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Socket Reader - čeka na konfigurisanom portu JSON kodiran PipelineCollection objekat.
 *  Jedna nit čeka na portu, dok ostale vrše komunikaciju preko soketa i skladištenje u kolekcije.
 *  Treba da bude moguće da se jedna kolekcija šalje ovom reader-u iz više JSON poruka
 *  (koje će imati isti ID kolekcije ako se odnose na istu kolekciju),
 *  kao i naglasiti kada je čitava kolekcija isporučena.
 *
 *  {
 *     id : 1,
 *	   partsCount : 5,
 *     list : [ { a: b, b : 3}]
 *  }
 *
 */
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

	public class Request {
		private int id;
		private int partsCount;
		private List<Map<String, String>> list;

		public PipelineCollection toCollection(int nodeId) {
			PipelineCollection collection = PipelineCollection.create(PipelineID.of(nodeId, this.id), this.partsCount);
			for (Map<String, String> dataObject : list) {
				collection.put(createData(dataObject));
			}
			return collection;
		}

		private PipelineData createData(Map<String, String> dataObject) {
			PipelineData data = PipelineData.create();
			for (Entry<String, String> keyValue : dataObject.entrySet()) {
				data.setValue(keyValue.getKey(), keyValue.getValue());
			}
			return data;
		}

	}

	private void processRequest(final Socket socket) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(socket.getInputStream());
			String json = scanner.nextLine();
			Request request = new Gson().fromJson(json, Request.class);

			final PipelineID id = generateId(request.id);
			final CollectionCollector collector = getCollector(id, request.partsCount);
			if (collector.submitResultAndShouldMerge(request.toCollection(getID()))) {
				onFinish(mergeBatches(collector.getAllResults()));
				removeCollector(id);
			}

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


		 // echo '{ id : 1, partsCount : 2, list : [ { rating: 1, b : 3}, {rating : 3}] }' | nc localhost 8080
	}

}
