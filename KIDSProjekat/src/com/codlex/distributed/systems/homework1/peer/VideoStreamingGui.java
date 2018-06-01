package com.codlex.distributed.systems.homework1.peer;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.core.util.FileUtils;

import com.codlex.distributed.systems.homework1.bootstrap.BootstrapNode;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.content.DHTEntry;
import com.codlex.distributed.systems.homework1.peer.dht.content.IdType;
import com.codlex.distributed.systems.homework1.peer.operations.GetValueOperation;
import com.google.common.collect.Lists;

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
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
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
			this.stage.setTitle(this.node.getInfo().toString());
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

		Label label = new Label("Need for speed soundtrack 1");
		label.setFont(Font.font(30));

		videoPlayer.getChildren().add(label);

		this.mediaView = new MediaView();
        this.mediaView.setFitHeight(500);
        this.mediaView.setFitWidth(500);

        videoPlayer.getChildren().add(mediaView);

		videoPlayer.getChildren().add(new Label("Current streamers count: "));

		Label streamersCount = new Label();
		streamersCount.textProperty().bind(this.node.getCurrentStreamers().asString());
		videoPlayer.getChildren().add(streamersCount);
		videoPlayer.getChildren().add(buildDebugger());

		return videoPlayer;
	}

	private javafx.scene.Node buildDebugger() {
		VBox container = new VBox();

		container.getChildren().add(new Label("Local DHT:"));

		ObservableList<DHTEntry> listOfItems = FXCollections.observableArrayList();

		this.node.getDht().getTable().addListener(new MapChangeListener<KademliaId, DHTEntry>() {
			@Override
			public void onChanged(
					javafx.collections.MapChangeListener.Change<? extends KademliaId, ? extends DHTEntry> change) {

				listOfItems.removeIf((key) -> {
					return key.getId().equals(change.getKey());
				});

				if (change.getValueAdded() != null) {
					listOfItems.add((DHTEntry) change.getValueAdded());
				}
			}
		});

		TableView<DHTEntry> table = new TableView<>(listOfItems);

		TableColumn<DHTEntry, String> column1 = new TableColumn<>("Key");
		column1.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						return new SimpleStringProperty(p.getValue().getId().toString());
					}
				});

		TableColumn<DHTEntry, String> column2 = new TableColumn<>("Distance");
		column2.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {

						return new SimpleStringProperty(Integer.toString(
								p.getValue().getId().getDistance(VideoStreamingGui.this.node.getInfo().getId())));
					}
				});

		TableColumn<DHTEntry, String> column3 = new TableColumn<>("Value");
		column3.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
						// for second column we use value
						return new SimpleStringProperty(p.getValue().toString());
					}
				});

		TableColumn<String, String> column = new TableColumn<>();
		column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

		table.getColumns().add(column1);
		table.getColumns().add(column2);
		table.getColumns().add(column3);

		container.getChildren().add(table);

		return container;
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
				byte[] bytes = null;
				try {
					bytes = Files.readAllBytes(Paths.get(filePath.getText()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				VideoStreamingGui.this.node.uploadVideo(nameField.getText(), bytes, (result) -> {
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
		log.debug("Started streaming {}", videoName);

		KademliaId videoId = new KademliaId(IdType.Video, this.node.getRegion(), videoName);
		new GetValueOperation(node, videoId).execute(false, (targetNode, value) -> {
			Platform.runLater(() -> {
				log.debug("Target node:" + targetNode);
				String URI = String.format("http://%s:%d/%s", targetNode.address, targetNode.streamingPort, URLEncoder.encode(videoId.toHex()));

				Media media = new Media(URI);
		        MediaPlayer mediaPlayer = new MediaPlayer(media);
		        mediaPlayer.setAutoPlay(true);

		        MediaPlayer old = VideoStreamingGui.this.mediaView.getMediaPlayer();
		        VideoStreamingGui.this.mediaView.setMediaPlayer(mediaPlayer);

		        if (old != null) {
		        	old.stop();
		        }

			});

		});
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
			this.stage.setTitle(this.node.getInfo().toString());
		});
		regions.getSelectionModel().selectFirst();

		topBar.getChildren().add(regions);
		Button connectButton = new Button("Connect");
		connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				VideoStreamingGui.this.node.bootstrap();
			}
		});

		topBar.getChildren().add(connectButton);
		topBar.getChildren().add(new Label(this.node.getCurrentTask()));

		return topBar;
	}

	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

	public static void main(String[] args) throws IOException {
		deleteFolder(new File("videos"));
		new BootstrapNode(Settings.bootstrapNode);
		for (int i = 1; i < 4; i++) {
			new VideoStreamingGui(new Node(8100 + i, 8000 + i * 2));
		}
	}

}
