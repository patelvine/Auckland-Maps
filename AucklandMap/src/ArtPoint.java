import java.util.Queue;

/** this class stores all the needed values for the articulation points algorithm **/

public class ArtPoint {
	private Node node; //current node
	private int depth;
	private ArtPoint ap; // parent
	private int reach; 
	private Queue<Node> children;
	
	public ArtPoint(Node node, int depth, ArtPoint ap) {
		this.node = node;
		this.setDepth(depth);
		this.setAp(ap);
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public ArtPoint getAp() {
		return ap;
	}

	public void setAp(ArtPoint ap) {
		this.ap = ap;
	}

	public int getReach() {
		return reach;
	}

	public void setReach(int reach) {
		this.reach = reach;
	}

	public Queue<Node> getChildren() {
		return children;
	}

	public void setChildren(Queue<Node> children) {
		this.children = children;
	}
	
	public void addChildren(Node n) {
		this.children.add(n);
	}

	public Node dequeueChild() {
		return children.poll();
	}
}