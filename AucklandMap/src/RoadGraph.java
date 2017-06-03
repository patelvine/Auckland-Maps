import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;

import javax.swing.JTextArea;

/** RoadMap: The list of the roads and the graph of the road network */

public class RoadGraph {
	
	Map<Integer, List<Polygons>> polygonMap = new HashMap<Integer, List<Polygons>>();

	double westBoundary = Double.POSITIVE_INFINITY;
	double eastBoundary = Double.NEGATIVE_INFINITY;
	double southBoundary = Double.POSITIVE_INFINITY;
	double northBoundary = Double.NEGATIVE_INFINITY;

	private QueueObj fin;

	// the map containing the graph of nodes (and roadsegments), indexed by the
	// nodeID
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();

	// the map of roads, indexed by the roadID
	Map<Integer, Road> roads = new HashMap<Integer, Road>();;

	// the map of roads, indexed by name
	Map<String, Set<Road>> roadsByName = new HashMap<String, Set<Road>>();;

	Map<Integer, List<Node>> neighbours = new HashMap<Integer, List<Node>>(); // stores neighbours of a node
	ArrayList<Node> artNodes = new ArrayList<Node>(); // stores all known articulation points

	Set<String> roadNames = new HashSet<String>();

	/** Construct a new RoadMap object */
	public RoadGraph() {
	}

	public void artPoint(Node start, int depth, Node root) {
		Stack<ArtPoint> stack = new Stack<ArtPoint>();
		stack.push(new ArtPoint(start, 1, new ArtPoint(null, 0, null)));
		while (!stack.isEmpty()) {
			ArtPoint elem = stack.peek();
			Node n = elem.getNode();
			if (elem.getChildren() == null) {
				n.setDepth(elem.getDepth());
				elem.setReach(elem.getDepth());
				elem.setChildren(new LinkedList<Node>());

				List<Node> listNeigh = neighbours.get(n.getID());
				for (Node neigh : listNeigh) {
					if (neigh != elem.getAp().getNode()) {
						elem.addChildren(neigh);
					}
				}
			} else if (!elem.getChildren().isEmpty()) {
				Node child = elem.dequeueChild();
				if (child.getDepth() < Integer.MAX_VALUE) {
					elem.setReach(Math.min(elem.getReach(), child.getDepth()));
				} else {
					stack.push(new ArtPoint(child, n.getDepth() + 1, elem));
				}
			} else {
				if (n != start) {
					if (elem.getReach() >= elem.getAp().getDepth()) {
						artNodes.add(elem.getAp().getNode());
					}
					elem.getAp().setReach(
							Math.min(elem.getAp().getReach(), elem.getReach()));
				}
				stack.pop();
			}
		}
	}

	public void aStar(Node start, Node end) {
		double costToHere = 0;
		for (Node node : nodes.values()) {
			node.setVisited(false);
			node.setFrom(null);
		}
		PriorityQueue<QueueObj> pq = new PriorityQueue<QueueObj>(10,
				new LengthComparison());
		QueueObj pathFrom = null;
		pq.offer(new QueueObj(start, pathFrom, 0,
				start.distanceTo(end.getLoc()), null));

		while (!pq.isEmpty()) {
			QueueObj dequeue = pq.poll();
			Node node = dequeue.getStart();
			dequeue.setLength(costToHere);
			this.fin = dequeue;
			if (node.getVisited() == false) {
				node.setVisited(true);
				pathFrom = dequeue;
				if (node.equals(end)) {
					break;
				}
				List<Segment> both = node.getOutNeighbours();
				both.addAll(node.getInNeighbours());
				for (Segment seg : both) {
					Node neigh = null;
					costToHere += seg.getLength();
					if (node == seg.getEndNode()) {
						neigh = seg.getStartNode();
					} else if (node == seg.getStartNode()) {
						neigh = seg.getEndNode();
					}

					if (neigh.getVisited() == false && neigh != null) {
						double cost = costToHere + seg.getLength();
						double estCost = cost + seg.getLength();
						pq.offer(new QueueObj(neigh, pathFrom, cost, estCost,
								seg));
					}
				}
			}
		}
	}

	/** comparator for aStar **/
	public class LengthComparison implements Comparator<QueueObj> {
		@Override
		public int compare(QueueObj one, QueueObj two) {
			if (one.getLength() + one.getEstimate() < two.getLength()
					+ two.getEstimate())
				return -1;
			else if (one.getLength() + one.getEstimate() > two.getLength()
					+ two.getEstimate())
				return 1;
			else
				return 0;
		}
	}

	public String loadData(String dataDirectory) {
		// Read roads into roads array.
		// Read the nodes into the roadGraph array.
		// Read each road segment
		// put the segment into the neighbours of the startNode
		// If the road of the segment is not one way,
		// also construct the reversed segment and put it into
		// the neighbours of the endNode
		// Work out the boundaries of the region.
		String report = "";
		System.out.println("Loading roads...");
		loadRoads(dataDirectory);
		report += String.format(
				"Loaded %,d roads, with %,d distinct road names%n", roads
						.entrySet().size(), roadNames.size());
		System.out.println("Loading intersections...");
		loadNodes(dataDirectory);
		report += String.format("Loaded %,d intersections%n", nodes.entrySet()
				.size());
		System.out.println("Loading road segments...");
		loadSegments(dataDirectory);
		report += String.format("Loaded %,d road segments%n", numSegments());
		System.out.println("Loading road polygons...");
		loadPolygons(dataDirectory);
		report += String.format("Loaded polygons");
		return report;
	}

	public void loadPolygons(String dataDirectory) {
		File polygonFile = new File(dataDirectory + "polygon-shapes.mp");
		if (!polygonFile.exists()) {
			System.out.println("roadID-roadInfo.tab not found");
			return;
		}
		try {
			String type = null;
			String label = null;
			int endLevel = 0;
			int cityIdx = 0;
			ArrayList<List<Location>> locations = new ArrayList<List<Location>>(); // allows storage of more then one list of polygons incase of outcuts

			BufferedReader reader = new BufferedReader(new FileReader(polygonFile));
			String line;
			boolean start = false; // used to no when to stop reading to add to polygonMap and when to start reading or continue reading
			
			while ((line = reader.readLine()) != null) {
				if (line.contains("[POLYGON]")) { // if line contains [POLYGON] then sets start to true
					start = true;
				}
				if (line.contains("[END]")) { // if line had [END] then stops reading and adds to map and sets start to false
					start = false;

					if (polygonMap.containsKey(endLevel)) {
						List<Polygons> r = polygonMap.get(endLevel);
						r.add(new Polygons(type, label, endLevel, cityIdx,   // LOOK IN ROAD FILE READER FOR COMMENTS ABOUT THESE (1)
								locations));
						polygonMap.put(endLevel, r);
					} else {
						List<Polygons> newList = new ArrayList<Polygons>();
						newList.add(new Polygons(type, label, endLevel,   // LOOK IN ROAD FILE READER FOR COMMENTS ABOUT THESE (2)
								cityIdx, locations));
						polygonMap.put(endLevel, newList);
					}
					/** since some polygons dont contain all info, resets all variables **/
					type = null;
					label = null;
					endLevel = 0;
					cityIdx = 0;
					locations = new ArrayList<List<Location>>();
				}
				if (start == true) {
					String[] split = line.split("="); // removes = which splits string into a name and its data and places into array
					switch (split[0]) { // checks what the name was and sets a variables value
					case "Type":
						type = split[1];
						break;
					case "Label":
						label = split[1];
						break;
					case "EndLevel":
						endLevel = Integer.parseInt(split[1]);
						break;
					case "CityIdx":
						cityIdx = Integer.parseInt(split[1]);
						break;
					case "Data0": 
						String dP = split[1].replaceAll("[()]", ""); // removes all paranthiess
						String[] loc = dP.split(","); // removes all , and places all coords into a string
						ArrayList<Location> temp = new ArrayList<Location>(); // new empty list of locations
						for (int i = 0; i < loc.length; i += 2) { // iterates through loc array 
							temp.add(Location.newFromLatLon(
									Double.parseDouble(loc[i]),
									Double.parseDouble(loc[i + 1]))); // creates new xy location and adds to list of locations for all coords in the array
						}
						locations.add(temp);
						break;
					case "Data1":
						String dP2 = split[1].replaceAll("[()]", "");
						String[] loc2 = dP2.split(",");
						ArrayList<Location> temp2 = new ArrayList<Location>();  // LOOK ABOVE AT DATA0 SWITCH CASE FOR COMMMENTS
						for (int i = 0; i < loc2.length; i += 2) {
							temp2.add(Location.newFromLatLon(
									Double.parseDouble(loc2[i]),
									Double.parseDouble(loc2[i + 1])));
						}
						locations.add(temp2);
						break;
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println("error Polygons");
		}
	}
	
	public void loadRoads(String dataDirectory) {
		File roadFile = new File(dataDirectory + "roadID-roadInfo.tab");
		if (!roadFile.exists()) {
			System.out.println("roadID-roadInfo.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(roadFile));
			data.readLine(); // throw away header line.
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Road road = new Road(line);
				roads.put(road.getID(), road);
				String fullName = road.getFullName();
				roadNames.add(fullName);
				Set<Road> rds = roadsByName.get(fullName);
				if (rds == null) {
					rds = new HashSet<Road>(4);
					roadsByName.put(fullName, rds);
				}
				rds.add(road);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadNodes(String dataDirectory) {
		File nodeFile = new File(dataDirectory + "nodeID-lat-lon.tab");
		if (!nodeFile.exists()) {
			System.out.println("nodeID-lat-lon.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(nodeFile));
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Node node = new Node(line);
				nodes.put(node.getID(), node);
			}

		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadSegments(String dataDirectory) {
		File segFile = new File(dataDirectory
				+ "roadSeg-roadID-length-nodeID-nodeID-coords.tab");
		if (!segFile.exists()) {
			System.out
					.println("roadSeg-roadID-length-nodeID-nodeID-coords.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(segFile));
			data.readLine(); // get rid of headers
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Segment seg = new Segment(line, roads, nodes);
				// System.out.println(seg);
				Node node1 = seg.getStartNode();
				Node node2 = seg.getEndNode();
				node1.addOutSegment(seg);
				node2.addInSegment(seg);
				Road road = seg.getRoad();
				road.addSegment(seg);

				/** sets the map which stores the neighbours of each node (takes into account of both node1 and node2) **/
				if (neighbours.containsKey(node1.getID())) { 
					List<Node> temp = neighbours.get(node1.getID());
					temp.add(nodes.get(node2.getID()));
					neighbours.put(node1.getID(), temp);
				} else {
					List<Node> temp = new ArrayList<Node>();
					temp.add(nodes.get(node2.getID()));
					neighbours.put(node1.getID(), temp);
				}
				if (neighbours.containsKey(node2.getID())) {
					List<Node> temp = neighbours.get(node2.getID());
					temp.add(nodes.get(node1.getID()));
					neighbours.put(node2.getID(), temp);
				} else {
					List<Node> temp = new ArrayList<Node>();
					temp.add(nodes.get(node1.getID()));
					neighbours.put(node2.getID(), temp);
				}

				if (!road.isOneWay()) {
					Segment revSeg = seg.reverse();
					node2.addOutSegment(revSeg);
					node1.addInSegment(revSeg);
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
		for (Node n : nodes.values()) {
			if (n.getVisited() == false) {
				artPoint(n, 0, n);
			}
		}
	}

	public double[] getBoundaries() {
		double west = Double.POSITIVE_INFINITY;
		double east = Double.NEGATIVE_INFINITY;
		double south = Double.POSITIVE_INFINITY;
		double north = Double.NEGATIVE_INFINITY;

		for (Node node : nodes.values()) {
			Location loc = node.getLoc();
			if (loc.x < west) {
				west = loc.x;
			}
			if (loc.x > east) {
				east = loc.x;
			}
			if (loc.y < south) {
				south = loc.y;
			}
			if (loc.y > north) {
				north = loc.y;
			}
		}
		return new double[] { west, east, south, north };
	}

	public void checkNodes() {
		for (Node node : nodes.values()) {
			if (node.getOutNeighbours().isEmpty()
					&& node.getInNeighbours().isEmpty()) {
				System.out.println("Orphan: " + node);
			}
		}
	}

	public int numSegments() {
		int ans = 0;
		for (Node node : nodes.values()) {
			ans += node.getOutNeighbours().size();
		}
		return ans;
	}

	public void redrawPolygons(Graphics g, Location origin, double scale){
		int LOD = 3;
		while (LOD >= 1) { // 3 levels of detail. starts at 3-1
			List<Polygons> polyList = polygonMap.get(LOD); // gets list of polygons from map with the current LOD
			for (Polygons poly : polyList) {
				poly.draw(g, origin, scale); // calls draw method in Polygons class
			}
			LOD--;
		}
	}
	
	public void redraw(Graphics g, Location origin, double scale,
			Node selectedNode2, JTextArea textOutput) {
		// System.out.printf("Drawing road graph. at (%.2f, %.2f) @ %.3f%n",
		// origX, origY, scale);
		g.setColor(Color.black);
		for (Node node : nodes.values()) {
			for (Segment seg : node.getOutNeighbours()) {
				seg.draw(g, origin, scale);
			}
		}
		g.setColor(Color.blue);
		for (Node node : nodes.values()) {
			node.draw(g, origin, scale);
		}

		for (Node node : artNodes) {
			g.setColor(new Color(255, 0, 255));
			node.draw(g, origin, scale);
		}

		if (selectedNode2 != null) { //if selected node 2 is not null
			g.setColor(Color.green);
			Map<String, Double> temp = new HashMap<String, Double>();

			QueueObj cur = this.fin; // last object dequeued in Astar
			while (cur != null) {
				if (cur.getSeg() != null) {
					cur.getSeg().draw(g, origin, scale);

					/** uses road name as key and adds length to the road each time the same road comes **/
					if (temp.containsKey(cur.getSeg().getRoad().getName())) {
						double value = temp.get(cur.getSeg().getRoad()
								.getName())
								+ cur.getSeg().getLength();
						temp.put(cur.getSeg().getRoad().getName(), value);
					} else {
						temp.put(cur.getSeg().getRoad().getName(), cur.getSeg()
								.getLength());
					}
				}
				cur = cur.getFrom(); // sets cur to the parent (back-tracks back to start)
			}
			textOutput.setText("");
			double i = 0;
			for (String u : temp.keySet()) {
				textOutput.append(u + ": " + temp.get(u) + "km\n"); // prints all information about route
				i = i + temp.get(u);
			}
			textOutput.append("\nTotal distance = " + i);
		}
	}

	private double mouseThreshold = 5; // how close does the mouse have to be?

	public Node findNode(Point point, Location origin, double scale) {
		Location mousePlace = Location.newFromPoint(point, origin, scale);
		/*
		 * System.out.printf("find at %d %d -> %.3f %.3f -> %d %d %n", point.x,
		 * point.y, x, y, (int)((x-origX)*scale),(int)((y-origY)*(-scale)) );
		 */
		Node closestNode = null;
		double mindist = Double.POSITIVE_INFINITY;
		for (Node node : nodes.values()) {
			double dist = node.distanceTo(mousePlace);
			if (dist < mindist) {
				mindist = dist;
				closestNode = node;
			}
		}
		return closestNode;
	}

	/**
	 * Returns a set of full road names that match the query. If the query
	 * matches a full road name exactly, then it returns just that name
	 */
	public Set<String> lookupName(String query) {
		Set<String> ans = new HashSet<String>(10);
		if (query == null)
			return null;
		query = query.toLowerCase();
		for (String name : roadNames) {
			if (name.equals(query)) { // this is the right answer
				ans.clear();
				ans.add(name);
				return ans;
			}
			if (name.startsWith(query)) { // it is an option
				ans.add(name);
			}
		}
		return ans;
	}

	/** Get the Road objects associated with a (full) road name */
	public Set<Road> getRoadsByName(String fullname) {
		return roadsByName.get(fullname);
	}

	/**
	 * Return a list of all the segments belonging to the road with the given
	 * (full) name.
	 */
	public List<Segment> getRoadSegments(String fullname) {
		Set<Road> rds = roadsByName.get(fullname);
		if (rds == null) {
			return null;
		}
		System.out.println("Found " + rds.size() + " road objects: "
				+ rds.iterator().next());
		List<Segment> ans = new ArrayList<Segment>();
		for (Road road : rds) {
			ans.addAll(road.getSegments());
		}
		return ans;
	}

	public static void main(String[] arguments) {
		AucklandMapper.main(arguments);
	}

}
