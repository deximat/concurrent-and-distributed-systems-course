package com.codlex.distributed.systems.homework1.peer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.codlex.distributed.systems.homework1.bootstrap.BootstrapNode;
import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.dht.DHT;
import com.codlex.distributed.systems.homework1.peer.dht.DHT.DHTEntry;
import com.google.common.collect.ImmutableList;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class VideoStreamingGui {

	static {
		// initializes JavaFX
		new JFXPanel();
	}

	private Stage stage;
	private final Node node;

	protected ObservableList<String> searchResults = FXCollections.observableArrayList("fdslfj");

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

		final Image fakePlayer = new Image("fakePlayer.png");
		ImageView fakePlayerView = new ImageView(fakePlayer);
		fakePlayerView.setFitHeight(240);
		fakePlayerView.setFitWidth(320);
		videoPlayer.getChildren().add(fakePlayerView);

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
					return key.getKey().equals(change.getKey());
				});

				if (change.getValueAdded() != null) {
					listOfItems.add((DHTEntry) change.getValueAdded());
				}
			}
		});

		TableView<DHTEntry> table = new TableView<>(listOfItems);

		TableColumn<DHTEntry, String> column1 = new TableColumn<>("Key");
        column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
                return new SimpleStringProperty(p.getValue().getKey().toString());
            }
        });

		TableColumn<DHTEntry, String> column2 = new TableColumn<>("Distance");
        column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {

                return new SimpleStringProperty(Integer.toString(p.getValue().getKey().getDistance(VideoStreamingGui.this.node.getInfo().getId())));
            }
        });

        TableColumn<DHTEntry, String> column3 = new TableColumn<>("Value");
        column3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DHTEntry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DHTEntry, String> p) {
                // for second column we use value
                return new SimpleStringProperty(p.getValue().getValue().toString());
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
		String cssLayout = "-fx-border-color: GRAY;\n" +
                "-fx-insets: 10;\n" +
                "-fx-border-width: 1;\n" +
                "-fx-border-style: solid;\n";
		upload.setStyle(cssLayout);
		upload.setSpacing(10);

		Label uploadLabel = new Label("Upload video:");
		uploadLabel.setFont(Font.font(22));
		upload.getChildren().add(uploadLabel);

		HBox name = new HBox();

		name.getChildren().add(new Label("Name: "));
		TextField nameField = new TextField();
		name.getChildren().add(nameField);
		name.getChildren().add(new Button("Browse file..."));

		upload.getChildren().add(name);

		Label uploadStatus = new Label("");

		Button uploadButton = new Button("Upload video");
		uploadButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
			    new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent e) {
	        	uploadStatus.setText("UPLOADING...");
	        	VideoStreamingGui.this.node.uploadVideo(nameField.getText(), (result) -> {
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

	private javafx.scene.Node buildSearch() {
		VBox search = new VBox();
		search.getChildren().add(buildSearchBar());
		TableView<String> table = new TableView<String>(this.searchResults);

		TableColumn<String, String> column = new TableColumn<>();
		column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

        table.getColumns().add(column);

		//table.setPlaceholder(new Label("You didn't search for anything yet."));
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

		searchButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
			    new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent e) {
	        	VideoStreamingGui.this.node.search(searchInput.getText(), (results) -> {
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
		String cssLayout = "-fx-border-color: GRAY;\n" +
                "-fx-insets: 10;\n" +
                "-fx-border-width: 1;\n" +
                "-fx-border-style: solid;\n";
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
		connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED,
			    new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent e) {
	        	VideoStreamingGui.this.node.bootstrap();
	        }
		});

		topBar.getChildren().add(connectButton);
		topBar.getChildren().add(new Label(this.node.getCurrentTask()));

		return topBar;
	}

	public static void main(String[] SDFSDJDS) {
		new BootstrapNode(Settings.bootstrapNode);
		for (int i = 0; i < 6; i++) {
			new VideoStreamingGui(new Node(8000 + i));
		}
	}

}
