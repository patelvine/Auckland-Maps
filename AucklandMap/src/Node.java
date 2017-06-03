import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;
import java.io.*;


/** Node   */

public class Node{

    private int id;  
    private Location loc;
    private List<Segment> outNeighbours = new ArrayList<Segment>(2);
    private List<Segment> inNeighbours = new ArrayList<Segment>(2);
    private boolean visited;
	private Node from;
	private double cost;
	private int depth;


    /** Construct a new Node object */
    public Node(int id, Location l){
	this.id = id;
	this.loc = l;
	this.visited = false;
	this.from = null;
	this.setCost(0);
	this.setDepth(Integer.MAX_VALUE);
    }

    /** Construct a new Node object from a line in the data file*/
    public Node(String line){
	String[] values = line.split("\t");
	id = Integer.parseInt(values[0]);
	double lat = Double.parseDouble(values[1]);
	double lon = Double.parseDouble(values[2]);
	loc = Location.newFromLatLon(lat, lon);
	this.setDepth(Integer.MAX_VALUE);
	this.visited = false;
	this.from = null;
	this.setCost(0);
    }
    
    public boolean on(Location locc, double scale){
    	if(this.loc.getX() == locc.getX() && this.loc.getY() == locc.getY()){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public int getID(){
	return id;
    }
    public Location getLoc(){
	return this.loc;
    }

    public void addInSegment(Segment seg){
	inNeighbours.add(seg);
    }	
    public void addOutSegment(Segment seg){
	outNeighbours.add(seg);
    }	

    public List<Segment> getOutNeighbours(){
	return outNeighbours;
    }	
    
    public List<Segment> getInNeighbours(){
	return inNeighbours;
    }	
    
    public boolean getVisited(){
		return this.visited;
	}
	
	public void setVisited(boolean b){
		this.visited = b;
	}
	
	public Node getFrom(){
		return this.from;
	}
	
	public void setFrom(Node n){
		this.from = n;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
    
    public boolean closeTo(Location place, double dist){
	return loc.closeTo(place, dist);
    }

    public double distanceTo(Location place){
	return loc.distanceTo(place);
    }

    public void draw(Graphics g, Location origin, double scale){
	Point p = loc.getPoint(origin, scale);
	g.fillRect(p.x, p.y, 2, 2);
    }
    
    public String toString(){
	StringBuilder b = new StringBuilder(String.format("Intersection %d: at %s; Roads:  ", id, loc));
	Set<String> roadNames = new HashSet<String>();
	for (Segment neigh : inNeighbours){
	    roadNames.add(neigh.getRoad().getName());
	}
	for (Segment neigh : outNeighbours){
	    roadNames.add(neigh.getRoad().getName());
	}
	for (String name : roadNames){
	    b.append(name).append(", ");
	}
	return b.toString();
    }
}