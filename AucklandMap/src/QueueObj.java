public class QueueObj {
	
	private Node start; 
	private QueueObj from; // parent
	private double length;
	private double estimate; // estimate to end node
	private Segment seg;
	
	public QueueObj(Node start,QueueObj from,double length, double estimate, Segment seg) {
		this.setStart(start);
		this.setFrom(from);
		this.setLength(length);
		this.setEstimate(estimate);
		this.setSeg(seg);
	}

	public Node getStart() {
		return start;
	}

	public void setStart(Node start) {
		this.start = start;
	}

	public QueueObj getFrom() {
		return from;
	}

	public void setFrom(QueueObj from) {
		this.from = from;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getEstimate() {
		return estimate;
	}

	public void setEstimate(double estimate) {
		this.estimate = estimate;
	}

	public Segment getSeg() {
		return seg;
	}

	public void setSeg(Segment seg) {
		this.seg = seg;
	}
}
