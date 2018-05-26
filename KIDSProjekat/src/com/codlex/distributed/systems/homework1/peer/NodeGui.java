package com.codlex.distributed.systems.homework1.peer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.codlex.distributed.systems.homework1.core.id.KademliaId;
import com.codlex.distributed.systems.homework1.peer.operations.NodeLookup;
import com.codlex.distributed.systems.homework1.peer.routing.RoutingTable;

public class NodeGui {

	public static final AtomicInteger ID = new AtomicInteger();

	private final Node node;

	private final TableModel routingTableModel;

	private JLabel taskLabel;

	private JFrame frame;

	private JLabel dhtContent;

	final static ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

	public NodeGui(final Node node) {
		this.node = node;
		this.routingTableModel = new AbstractTableModel() {

			private RoutingTable table = NodeGui.this.node.getRoutingTable();

			public String getColumnName(int col) {
				switch (col) {
				case 0:
					return "distance";
				case 1:
					return "connections";
				default:
					return "jojo";
				}
			}

			public int getRowCount() {
				return table.getBucketsCount();
			}

			public int getColumnCount() {
				return 2;
			}

			public Object getValueAt(int row, int col) {
				return this.table.getBucket(row).getValue(col);
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}

			public void setValueAt(Object value, int row, int col) {
				throw new RuntimeException("Not supported.");
			}
		};

		this.frame = new JFrame(this.node.getInfo().getId().toString());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.getContentPane().add(new Label("Testing"),
		// BorderLayout.CENTER);

		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.pack();
		frame.setSize(400, 200);
		int id = ID.getAndIncrement();
		int xPos = (id % 7) * (frame.getWidth() + 20);
		int yPos = (id / 7) * (frame.getHeight() + 40);
		frame.setLocation(xPos, yPos);

		this.taskLabel = new JLabel("IDLE");
		frame.getContentPane().add(new JLabel("Task:"));
		frame.getContentPane().add(this.taskLabel);
//		frame.getContentPane().add(new Label("Routing table: "));
		frame.getContentPane().add(new JTable(this.routingTableModel));
		this.dhtContent = new JLabel("EMPTY");
		frame.getContentPane().add(this.dhtContent);
		frame.getContentPane().add(buildFindNodeButton());
		frame.getContentPane().add(buildStoreButton());

		frame.setVisible(true);

		SCHEDULER.scheduleAtFixedRate(() -> {
			SwingUtilities.invokeLater(() -> {
				refresh();
			});
		}, 0, 1, TimeUnit.SECONDS);
	}

	private Component buildFindNodeButton() {
		JButton findNodeButton = new JButton("Find node");
		findNodeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("Find node");
				frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
				JTextField findId = new JTextField();
				JButton findNodeButton = new JButton("Find");
				JLabel result = new JLabel("NO RESULT");
				findNodeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						result.setText("WORKING...");
						final NodeLookup lookup = new NodeLookup(NodeGui.this.node, new KademliaId(findId.getText()));
						lookup.execute((nodes) -> {
							SwingUtilities.invokeLater(() -> {
								if (nodes.size() > 0) {
									result.setText("Closest node: " + nodes.get(0));
								} else {
									result.setText("NOTHING FOUND");
								}
							});
						});

					}
				});
				frame.getContentPane().add(findId);
				frame.getContentPane().add(findNodeButton);
				frame.getContentPane().add(result);
				frame.pack();
				frame.setSize(200, 200);
				frame.setVisible(true);
			}
		});

		return findNodeButton;
	}


	private Component buildStoreButton() {
		JButton storeValueButton = new JButton("Store");
		storeValueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("Store");
				frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
				JTextField value = new JTextField();
				JButton storeButton = new JButton("Store");

				storeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						NodeGui.this.node.getDht().store(new KademliaId(value.getText()), value.getText());
					}
				});
				frame.getContentPane().add(value);
				frame.getContentPane().add(storeButton);
				frame.pack();
				frame.setSize(200, 200);
				frame.setVisible(true);
			}
		});

		return storeValueButton;
	}

	private void refresh() {
		this.taskLabel.setText(this.node.getCurrentTask());
		this.frame.getContentPane().validate();
		this.frame.getContentPane().repaint();
		this.dhtContent.setText(this.node.getDht().toString());
	}

}
