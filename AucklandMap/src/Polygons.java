import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class Polygons {

	private String type;
	private String label;
	private int endLevel;
	private int cityIdx;
	private ArrayList<List<Location>> locations;

	public Polygons(String type, String label, int endLevel, int cityIdx,
			ArrayList<List<Location>> locations) {
		this.type = type;
		this.label = label;
		this.endLevel = endLevel;
		this.cityIdx = cityIdx;
		this.locations = locations;
	}

	/** method to find the type of a polygon and return its colour **/
	public Color findType(String string) {
		int type = Integer.decode(string);
		
		if (type >= 1 && type <= 3)
			return new Color(255, 185, 247); // CITY light pinkish
		if (type == 0xa)
			return new Color(232, 215, 149); // SCHOOL light orange/light brown
		if (type == 0xb)
			return new Color(255, 0, 0); // HOSPITAL // red
		if (type >= 7 && type <= 0xd)
			return new Color(255, 255, 0); // MAN MADE SECONDARY // yellow
		if (type == 0xe || type == 0x13)
			return new Color(122, 122, 122); // MAN MADE // gray
		if (type == 0x1a)// Cemetary
			return new Color(255, 255, 0); // MAN MADE SECONDARY // yellow
		if (type >= 0x14 && type <= 0x1f)
			return new Color(129, 232, 129); // PARK RESERVE light green
		if (type >= 0x28 && type <= 0x49)
			return new Color(164, 204, 255); // WATER // light blue
		if (type == 0x50)
			return new Color(23, 172, 0); // WOODS // dark green
		else {
			return new Color(0, 253, 245); // light blue
		}
	}

	/** method to draw a single polygon **/
	public void draw(Graphics g, Location origin, double scale) {
		boolean outCut = false; // if polygon is an outcut or not
		int count = 0; // array count for x and y
		Color color = findType(this.type); // gets color given from findType method
		for (List<Location> locs : locations){
			if (outCut == true) {
				color = new Color(255, 255, 255); // sets to white if polygons an outCut
			}
			int xPoints[] = new int[locs.size()];
			int yPoints[] = new int[locs.size()];
			for (Location loc : locs) {
				Point p = loc.getPoint(origin, scale);
				xPoints[count] = p.x;
				yPoints[count] = p.y;
				count++;
			}
			g.setColor(color);
			g.fillPolygon(xPoints, yPoints, count);
			count = 0; // resets variables
			outCut = true;
		}
	}

	public ArrayList<List<Location>> getPolygons() {
		return locations;
	}
}
