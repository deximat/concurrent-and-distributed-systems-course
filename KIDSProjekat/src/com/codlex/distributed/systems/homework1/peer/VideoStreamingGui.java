package com.codlex.distributed.systems.homework1.peer;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.codlex.distributed.systems.homework1.bootstrap.BootstrapNode;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;
import com.codlex.distributed.systems.homework1.peer.dht.content.Keyword;
import com.codlex.distributed.systems.homework1.peer.dht.content.Video;
import com.codlex.distributed.systems.homework1.peer.operations.GetValueOperation;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.codlex.distributed.systems.homework1.starter.Log4jConfigurator;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoStreamingGui {

	static {
		// initializes JavaFX
		new JFXPanel();
	}

	private Stage stage;
	private final Node node;

	protected ObservableList<String> searchResults = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

	private MediaView mediaView;
	private Label videoLabel;
private Label streamingFrom;

	public VideoStreamingGui(Node node) {
		this.node = node;
		redraw();
	}

	private void redraw() {

		Platform.runLater(() -> {
			if (this.stage == null) {
				this.stage = new Stage();
			}

			VBox everything = new VBox();
			everything.getChildren().add(buildConnectBar());
			everything.getChildren().add(buildMainPanel());
			this.stage.setScene(new Scene(everything));
			this.stage.setOnCloseRequest((e) -> {
				log.debug("{} terminating self.", this.node);
				System.exit(0);
			});
			this.stage.show();
		});
	}

	private javafx.scene.Node buildMainPanel() {
		HBox mainPanel = new HBox(2);
		mainPanel.getChildren().add(buildSearchAndUpload());
		mainPanel.getChildren().add(buildVideoPlayer());
		return mainPanel;
	}

	private javafx.scene.Node buildVideoPlayer() {
		VBox videoPlayer = new VBox();
		videoPlayer.setAlignment(Pos.CENTER);
		videoPlayer.setSpacing(50);
		videoPlayer.setPrefWidth(500);

		this.videoLabel = new Label("No video loaded");
		this.videoLabel.setFont(Font.font(30));

		videoPlayer.getChildren().add(this.videoLabel);
		this.streamingFrom = new Label();
		videoPlayer.getChildren().add(streamingFrom);

		this.mediaView = new MediaView();
        this.mediaView.setFitHeight(200);
        this.mediaView.setFitWidth(500);
        videoPlayer.getChildren().add(mediaView);

		videoPlayer.getChildren().add(buildDebugger());

		return videoPlayer;
	}

	private javafx.scene.Node buildDebugger() {

		VBox container = new VBox();
		container.getChildren().add(buildDHTContent());
		container.getChildren().add(buildConnectionsTable());

		return container;
	}


	private javafx.scene.Node buildDHTContent() {
		ObservableList<DHTEntry> listOfItems = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		this.node.SCHEDULER.scheduleAtFixedRate(() -> {
			Platform.runLater(() -> {
				listOfItems.setAll(this.node.getDht().getTableAsList());
			});
		}, 0, 1, TimeUnit.SECONDS);

		TableView<DHTEntry> table = new TableView<>(listOfItems);

		TableColumn<DHTEntry, String> column1 = new TableColumn<>("Region");
		column1.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						return new SimpleStringProperty(p.getValue().getId().getRegion().toString());
					}
				});

		TableColumn<DHTEntry, String> column2 = new TableColumn<>("Type");
		column2.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						return new SimpleStringProperty(p.getValue().getId().getType().toString());
					}
				});

//		TableColumn<DHTEntry, String> column2 = new TableColumn<>("Distance");
//		column2.setCellValueFactory(
//				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
//					@Override
//					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
//
//						return new SimpleStringProperty(Integer.toString(
//								p.getValue().getId().getDistance(VideoStreamingGui.this.node.getInfo().getId())));
//					}
//				});

		TableColumn<DHTEntry, String> column3 = new TableColumn<>("Name");
		column3.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						// for second column we use value
						return new SimpleStringProperty(p.getValue().getId().getData());
					}
				});

		TableColumn<DHTEntry, String> column4 = new TableColumn<>("Videos");
		column4.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {

						if (p.getValue() instanceof Keyword) {
							return new SimpleStringProperty(((Keyword) p.getValue()).getVideos() + "");
						} else {
							return new SimpleStringProperty("");
						}
					}
				});

		TableColumn<DHTEntry, String> column5 = new TableColumn<>("Views");
		column5.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						// for second column we use value
						if (p.getValue() instanceof Video) {
							return new SimpleStringProperty(((Video) p.getValue()).getViewCount() + "");
						} else {
							return new SimpleStringProperty("");
						}
					}
				});

		TableColumn<DHTEntry, String> column6 = new TableColumn<>("Redundancy");
		column6.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						// for second column we use value
						if (p.getValue() instanceof Video) {
							return new SimpleStringProperty(((Video) p.getValue()).getDynamicRedundancy() + "");
						} else {
							return new SimpleStringProperty("");
						}
					}
				});

		TableColumn<String, String> column = new TableColumn<>();
		column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

		table.getColumns().addAll(column1, column2, column3, column4, column5, column6);
		table.setPrefHeight(250);

		return table;
	}

	private javafx.scene.Node buildConnectionsTable() {
		val pane = new javafx.scene.control.ScrollPane();
		pane.setPrefHeight(250);
		this.node.SCHEDULER.scheduleAtFixedRate(() -> {
			Platform.runLater(() -> {
			if (!this.node.getInited().get()) {
				return;
			}
					Label text = new Label();
				text.setText(this.node.getRoutingTable().toString());
					text.setPrefWidth(500);
					pane.setContent(text);
				});
		}, 0, Settings.RefreshBucketsViewSeconds, TimeUnit.SECONDS);
		return pane;
	}

	private javafx.scene.Node buildSearchAndUpload() {
		VBox searchAndUpload = new VBox();
		searchAndUpload.setAlignment(Pos.CENTER);
		searchAndUpload.setPrefWidth(500);

		Label searchLabel = new Label("Search:");
		searchLabel.setFont(Font.font(22));

		searchAndUpload.getChildren().add(searchLabel);
		searchAndUpload.getChildren().add(buildSearch());
		searchAndUpload.getChildren().add(buildUpload());
		return searchAndUpload;
	}

	private javafx.scene.Node buildUpload() {
		VBox upload = new VBox();
		upload.setAlignment(Pos.CENTER);
		String cssLayout = "-fx-border-color: GRAY;\n" + "-fx-insets: 10;\n" + "-fx-border-width: 1;\n"
				+ "-fx-border-style: solid;\n";
		upload.setStyle(cssLayout);
		upload.setSpacing(10);

		Label uploadLabel = new Label("Upload video:");
		uploadLabel.setFont(Font.font(22));
		upload.getChildren().add(uploadLabel);

		HBox name = new HBox();

		name.getChildren().add(new Label("Name: "));
		TextField nameField = new TextField();
		name.getChildren().add(nameField);
		Button browseFileButton = new Button("Browse file...");
		Label filePath = new Label();
		name.getChildren().add(filePath);
		browseFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Video File");

				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
					nameField.textProperty().set(file.getName());
					filePath.textProperty().set(file.getAbsolutePath());
				}
			}
		});
		name.getChildren().add(browseFileButton);

		upload.getChildren().add(name);

		Label uploadStatus = new Label("");

		Button uploadButton = new Button("Upload video");
		uploadButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				uploadStatus.setText("UPLOADING...");
				VideoStreamingGui.this.node.uploadVideo(nameField.getText(), filePath.getText(), (result) -> {
					Platform.runLater(() -> {
						uploadStatus.setText(result.toString());
					});
				});
			}
		});
		upload.getChildren().add(uploadStatus);
		upload.getChildren().add(uploadButton);
		return upload;
	}


	protected void startStreaming(String videoName) {
		log.debug("{} started streaming {}", this.node, videoName);

		KademliaId videoId = new KademliaId(IdType.Video, this.node.getRegion(), videoName);
		new GetValueOperation(this.node, videoId, false, (targetNode, value) -> {
			this.node.onStartedStreaming(targetNode, videoId);
			Platform.runLater(() -> {
				this.streamingFrom.textProperty().set("Streaming from: " + targetNode);
				String URI = String.format("http://%s:%d/%s", targetNode.address, targetNode.streamingPort, URLEncoder.encode(videoId.toHex()));

				Media media = new Media(URI);
		        MediaPlayer mediaPlayer = new MediaPlayer(media);
		        mediaPlayer.setAutoPlay(true);

		        this.videoLabel.textProperty().set(videoName);

		        MediaPlayer old = VideoStreamingGui.this.mediaView.getMediaPlayer();
		        VideoStreamingGui.this.mediaView.setMediaPlayer(mediaPlayer);

		        if (old != null) {
		        	old.stop();
		        }

			});

		}).execute();
	}

	private class ButtonCell extends TableCell<String, String> {

		private Button cellButton = new Button("Stream");
		{
			cellButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					startStreaming(getItem());
				}
			});
		}

		ButtonCell() {
		}


		@Override
		protected void updateItem(String record, boolean empty) {
			super.updateItem(record, empty);
			if (!empty) {
				setGraphic(this.cellButton);
			}
		}
	}

	private javafx.scene.Node buildSearch() {
		VBox search = new VBox();
		search.getChildren().add(buildSearchBar());
		TableView<String> table = new TableView<String>(this.searchResults);

		TableColumn<String, String> column = new TableColumn<>("Video");
		column.setCellValueFactory((data) -> new SimpleStringProperty(data.getValue()));

		TableColumn<String, String> column2 = new TableColumn<>("DUGME");
		column2.setCellValueFactory((data) -> new SimpleStringProperty(data.getValue()));
		column2.setCellFactory(new Callback<TableColumn<String, String>, TableCell<String, String>>() {
			@Override
			public TableCell<String, String> call(TableColumn<String, String> p) {
				return new ButtonCell();
			}
		});

		table.getColumns().add(column);
		table.getColumns().add(column2);

		// table.setPlaceholder(new Label("You didn't search for anything
		// yet."));
		search.getChildren().add(table);

		return search;
	}

	private javafx.scene.Node buildSearchBar() {
		HBox searchBar = new HBox();

		TextField searchInput = new TextField();
		searchInput.setPrefWidth(400);
		searchBar.getChildren().add(searchInput);

		Button searchButton = new Button("Search");
		searchButton.setPrefWidth(100);

		searchButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				log.debug("SEARCH CLICKED.");
				VideoStreamingGui.this.node.search(searchInput.getText(), (results) -> {
					log.debug("Results in search: {}", results);
					Platform.runLater(() -> {
						VideoStreamingGui.this.searchResults.clear();
						VideoStreamingGui.this.searchResults.addAll(results);
					});
				});
			}
		});
		searchBar.getChildren().add(searchButton);

		return searchBar;
	}

	private javafx.scene.Node buildConnectBar() {
		HBox topBar = new HBox();
		String cssLayout = "-fx-border-color: GRAY;\n" + "-fx-insets: 10;\n" + "-fx-border-width: 1;\n"
				+ "-fx-border-style: solid;\n";
		topBar.setStyle(cssLayout);
		topBar.setAlignment(Pos.CENTER);
		topBar.setSpacing(10);

		topBar.getChildren().add(new Label("Choose region:"));
		ChoiceBox<Region> regions = new ChoiceBox<Region>(FXCollections.observableArrayList(Region.values()));
		regions.getSelectionModel().selectedItemProperty().addListener((object, oldValue, newValue) -> {
			this.node.setRegion(newValue);
		});
		regions.getSelectionModel().selectFirst();

		topBar.getChildren().add(regions);
		Button connectButton = new Button("Connect");
		connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				VideoStreamingGui.this.node.bootstrap(() -> {
					Platform.runLater(() -> {
						VideoStreamingGui.this.stage.setTitle(VideoStreamingGui.this.node.toString());
					});
				});
				topBar.setDisable(true);
			}
		});

		Label taskLabel = new Label();
		taskLabel.textProperty().bindBidirectional(this.node.getTask());
		topBar.getChildren().add(connectButton);
		topBar.getChildren().add(taskLabel);

		return topBar;
	}

	public static void main(String[] args) {
//		BootstrapNode.main(null);
		Integer port = Integer.parseInt(args[0]);
		Integer streamingPort = Integer.parseInt(args[1]);
		Log4jConfigurator.configure(String.format("bootstrap-%d-%d.log", port, streamingPort));
		log.debug("Started VideoStreamingGui(port = {}, streamingPort = {})", port, streamingPort);
		val node = new Node(port, streamingPort);
		new VideoStreamingGui(node);
	}

}
