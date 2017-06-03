import java.awt.Graphics;
import java.awt.Point;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.util.*;
import java.io.*;

public class AucklandMapper {

	private JFrame frame;
	private JComponent drawing;
	private JTextArea textOutput;
	private JTextField nameEntry;
	private int windowSize = 700;

	private RoadGraph roadGraph;

	private Node selectedNode;
	private Node selectedNode2;
	private List<Segment> selectedSegments;

	private boolean loaded = false;

	// Dimensions for drawing
	double westBoundary;
	double eastBoundary;
	double southBoundary;
	double northBoundary;
	Location origin;
	double scale;

	// Points for mouse motion
	private Point start, end;

	public AucklandMapper(String dataDir) {
		setupInterface();
		roadGraph = new RoadGraph();

		textOutput.setText("Loading data...");
		while (dataDir == null) {
			dataDir = getDataDir();
		}
		textOutput.append("Loading from " + dataDir + "\n");
		textOutput.append(roadGraph.loadData(dataDir));
		setupScaling();
		loaded = true;
		drawing.repaint();
	}

	private class DirectoryFileFilter extends FileFilter {
		public boolean accept(File f) {
			return f.isDirectory();
		}

		public String getDescription() {
			return "Directories only";
		}
	}

	private String getDataDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new DirectoryFileFilter());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return fc.getSelectedFile().getPath() + File.separator;
	}

	private void setupScaling() {
		double[] b = roadGraph.getBoundaries();
		westBoundary = b[0];
		eastBoundary = b[1];
		southBoundary = b[2];
		northBoundary = b[3];
		resetOrigin();
	}

	private void setupInterface() {
		// Set up a window .
		frame = new JFrame("Graphics Example");
		frame.setSize(windowSize, windowSize);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				redraw(g);
			}
		};
		frame.add(drawing, BorderLayout.CENTER);

		// Setup a text area for output
		textOutput = new JTextArea(5, 100);
		textOutput.setEditable(false);
		JScrollPane textSP = new JScrollPane(textOutput);
		frame.add(textSP, BorderLayout.SOUTH);

		// Set up a panel for some buttons.
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.NORTH);

		// Add buttons to the panel.
		JButton button = new JButton("+");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomIn();
			}
		});

		button = new JButton("-");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomOut();
			}
		});

		button = new JButton("<");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("left");
			}
		});

		button = new JButton(">");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("right");
			}
		});

		button = new JButton("^");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("up");
			}
		});

		button = new JButton("v");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("down");
			}
		});

		// method used to reset all selections
		button = new JButton("clear selected");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				selectedNode = null;
				selectedNode2 = null;
				selectedSegments = null;
				drawing.repaint();
			}
		});

		nameEntry = new JTextField(20);
		panel.add(nameEntry);
		nameEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lookupName(nameEntry.getText());
				drawing.repaint();
			}
		});

		button = new JButton("Quit");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});

		// Add a mouselistener to the drawing JComponent to respond to mouse
		// clicks.
		drawing.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (selectedNode != null) {
					selectedNode2 = findNode(e.getPoint());
					roadGraph.aStar(selectedNode, selectedNode2);
					textOutput.setText(selectedNode.toString());
				} else {
					selectedNode = findNode(e.getPoint());
				}
				drawing.repaint();
			}

			// Listener to respond to mouse panning
			public void mousePressed(MouseEvent e) {
				start = e.getPoint();
				end = null;
				drawing.repaint();
			}
		});

		// Add mouse wheel listener for fast zooming)
		drawing.addMouseWheelListener(new MouseAdapter() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				int rot = e.getWheelRotation();
				if (rot < 0) {
					zoomIn();
				} else {
					zoomOut();
				}
			}
		});

		// Adds a mouse motion listener for panning of map
		drawing.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				end = e.getPoint();
				double x = (e.getX() - start.getX()) / scale;
				double y = (e.getY() - start.getY()) / scale;
				origin = new Location((origin.getX() - x), origin.getY() + y);
				start = end;
				drawing.repaint();
			}
		});

		// Once it is all set up, make the interface visible
		frame.setVisible(true);
	}

	private double zoomFactor = 1.25;
	private double panFraction = 0.2;

	// set origin and scale for the whole map
	private void resetOrigin() {
		origin = new Location(westBoundary, northBoundary);
		scale = Math.min(windowSize / (eastBoundary - westBoundary), windowSize / (northBoundary - southBoundary));
	}

	// shrink the scale (pixels/per km) by zoomFactor and move origin
	private void zoomOut() {
		scale = scale / zoomFactor;
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor / 2;
		origin = new Location(origin.x - deltaOrig, origin.y + deltaOrig);
		drawing.repaint();
	}

	// expand the scale (pixels/per km) by zoomFactor and move origin
	private void zoomIn() {
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor / 2;
		origin = new Location(origin.x + deltaOrig, origin.y - deltaOrig);
		scale = scale * zoomFactor;
		drawing.repaint();
	}

	private void pan(String dir) {
		double delta = windowSize * panFraction / scale;
		switch (dir) {
		case "left": {
			origin = new Location(origin.x - delta, origin.y);
			break;
		}
		case "right": {
			origin = new Location(origin.x + delta, origin.y);
			break;
		}
		case "up": {
			origin = new Location(origin.x, origin.y + delta);
			break;
		}
		case "down": {
			origin = new Location(origin.x, origin.y - delta);
			break;
		}
		}
		drawing.repaint();
	}

	// Find the place that the mouse was clicked on (if any)
	private Node findNode(Point mouse) {
		return roadGraph.findNode(mouse, origin, scale);
	}

	private void lookupName(String query) {
		List<String> names = new ArrayList(roadGraph.lookupName(query));
		if (names.isEmpty()) {
			selectedSegments = null;
			textOutput.setText("Not found");
		} else if (names.size() == 1) {
			String fullName = names.get(0);
			nameEntry.setText(fullName);
			textOutput.setText("Found");
			selectedSegments = roadGraph.getRoadSegments(fullName);
		} else {
			selectedSegments = null;
			String prefix = maxCommonPrefix(query, names);
			nameEntry.setText(prefix);
			textOutput.setText("Options: ");
			for (int i = 0; i < 10 && i < names.size(); i++) {
				textOutput.append(names.get(i));
				textOutput.append(", ");
			}
			if (names.size() > 10) {
				textOutput.append("...\n");
			} else {
				textOutput.append("\n");
			}
		}
	}

	private String maxCommonPrefix(String query, List<String> names) {
		String ans = query;
		for (int i = query.length();; i++) {
			if (names.get(0).length() < i)
				return ans;
			String cand = names.get(0).substring(0, i);
			for (String name : names) {
				if (name.length() < i)
					return ans;
				if (name.charAt(i - 1) != cand.charAt(i - 1))
					return ans;
			}
			ans = cand;
		}
	}

	// The redraw method that will be called from the drawing JComponent and
	// will
	// draw the map at the current scale and shift.
	public void redraw(Graphics g) {
		frame.getContentPane().setBackground(new Color(229, 255, 229));
		if (roadGraph != null && loaded) {
			roadGraph.redrawPolygons(g, origin, scale);
			roadGraph.redraw(g, origin, scale, selectedNode2, textOutput);
			if (selectedNode != null) {
				g.setColor(Color.red);
				selectedNode.draw(g, origin, scale);
			}
			if (selectedNode2 != null) { // draws the second segment in red and
											// also the gets and highlights the
											// route
				g.setColor(Color.red);
				selectedNode2.draw(g, origin, scale);
			}
			if (selectedSegments != null) {
				g.setColor(Color.red);
				for (Segment seg : selectedSegments) {
					seg.draw(g, origin, scale);
				}
			}
		}
	}

	public static void main(String[] arguments) {
		if (arguments.length > 0) {
			AucklandMapper obj = new AucklandMapper(arguments[0]);
		} else {
			AucklandMapper obj = new AucklandMapper(null);
		}
	}
}