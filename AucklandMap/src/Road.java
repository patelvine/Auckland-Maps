import java.util.*;


/** Road   */

public class Road{
    
	private int id;  
	private int type;
	private String name;
    private String city;  
    private int oneway;  
    private int speed;  
    private int roadClass;  
    private int notForCars;  
    private int notForPedestrians;  
    private int notForBicycles;  
    private List<Segment> segments = new ArrayList<Segment>();  
	
    public Road(int id, int type, String label, String city, int oneWay, int speed, int roadClass,
			int notForCar, int notForPede, int notForBicy) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.city = city;
		this.oneway = oneway;
		this.speed = speed;
		this.roadClass = roadClass;
		this.notForCars = notForCar;
		this.notForPedestrians = notForPede;
		this.notForBicycles = notForBicy;
	}

    /** Construct a new Road object from a line from the data file*/
    public Road(String line){
	String[] values = line.split("\t");
	this.id = Integer.parseInt(values[0]);
	this.type = Integer.parseInt(values[1]);
	this.name = values[2];
	this.city = values[3];
	if (city.equals("-")) { city = ""; }
	oneway = Integer.parseInt(values[1]);
	speed = Integer.parseInt(values[5]);
	roadClass = Integer.parseInt(values[6]);  
	notForCars = Integer.parseInt(values[7]);  
	notForPedestrians = Integer.parseInt(values[8]);  
	notForBicycles = Integer.parseInt(values[9]);
    }

	public int getID(){return this.id;}
    public String getName(){return this.name;}
    public String getFullName(){
	if (this.city=="") {return this.name;}
	return this.name + " " + this.city;
    }
    public int getRoadclass(){return this.roadClass;}
    public int getSpeed(){return this.speed;}

    public boolean isOneWay(){return false;}
    //public boolean isNotForCars(){return this.notForCars;}
    //public boolean isNotForPedestrians(){return this.notForPedestrians;}
    //public boolean isNotForBicycles(){return this.notForBicycles;}
  /**  
    public boolean isOneWay(){return ture;}
    public boolean isNotForCars(){return this.notForCars;}
    public boolean isNotForPedestrians(){return this.notForPedestrians;}
    public boolean isNotForBicycles(){return this.notForBicycles;}
**/
    public void addSegment(Segment seg){
	this.segments.add(seg);
    }

    public List<Segment> getSegments(){
	return this.segments;
    }

    public String toString(){
	return "Road: "+getFullName();
    }

    public static void main(String[] arguments){
	AucklandMapper.main(arguments);
    }

	public String getLabel() {
		return this.name;
	}

	public String getCity() {
		return this.city;
	}	


}
