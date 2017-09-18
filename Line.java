import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;

public class Line implements Comparable<Line> {
	int num;
	ArrayList<Float> row;
	ArrayList<Float> col;
	private int frame;
	private int ID;
	public Lines splits;
	public int[] covered;
	public int[] coverHelper;
	public boolean matched;
	public boolean isAdded; //added by connection or by splitting
	int processed;
	int [] connections; //only used for added lines;
	private Line.contour_class cont_class;
	public Loss[] lossList;
	public ArrayList<Integer> microID = new ArrayList<Integer>();

	// the microtubule(s) matched to

	public Line(int ID) {
		this.ID = ID;
		this.row = new ArrayList<Float>();
		this.col = new ArrayList<Float>();
		this.num = 0;
		this.processed = 0;
		this.lossList = new Loss[1];
		this.matched = false;
		this.isAdded = false;
	}

	public void initCoverage(int num) {
		// initialize the coverage array.
		covered = new int[num];
		coverHelper = new int[num];
	}
	
	public void updateCoverage() {
		for (int i = 0; i < this.num; ++i) {
			this.covered[i] = this.coverHelper[i];
		}
	}
	
	public void revertCoverage() {
		for (int i = 0; i < this.num; ++i) {
			this.coverHelper[i] = this.covered[i];
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result
				+ ((cont_class == null) ? 0 : cont_class.hashCode());
		result = prime * result + frame;
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line other = (Line) obj;
		if (ID != other.ID)
			return false;
		if (cont_class != other.cont_class)
			return false;
		if (num != other.num)
			return false;
		return true;
	}

	public int getFrame() {
		return this.frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public int getID() {
		return this.ID;
	}

	public Line.contour_class getContourClass() {
		return this.cont_class;
	}

	public void setContourClass(Line.contour_class c) {
		this.cont_class = c;
	}

	public boolean isConnected(Line l) {
		return (Math.abs(this.col.get(0) - l.col.get(0)) < 1 && Math
				.abs(this.row.get(0) - l.row.get(0)) < 1)
				|| (Math.abs(this.col.get(0) - l.col.get(l.num - 1)) < 1 && Math
						.abs(this.row.get(0) - l.row.get(l.num - 1)) < 1)
				|| (Math.abs(this.col.get(this.num - 1) - l.col.get(0)) < 1 && Math
						.abs(this.row.get(this.num - 1) - l.row.get(0)) < 1)
				|| (Math.abs(this.col.get(this.num - 1) - l.col.get(l.num - 1)) < 1 && Math
						.abs(this.row.get(this.num - 1) - l.row.get(l.num - 1)) < 1);
	}

	public static float[][] findCloserEnds(Line l1, Line l2) {
		float[] dists = new float[4];
		Tool tool = new Tool();
		float[] l1Start = { l1.col.get(0), l1.row.get(0) };
		float[] l1End = { l1.col.get(l1.num - 1), l1.row.get(l1.num - 1) };
		float[] l2Start = { l2.col.get(0), l2.row.get(0) };
		float[] l2End = { l2.col.get(l2.num - 1), l2.row.get(l2.num - 1) };
		dists[0] = tool.dist(l1Start, l2Start);
		dists[1] = tool.dist(l1Start, l2End);
		dists[2] = tool.dist(l1End, l2Start);
		dists[3] = tool.dist(l1End, l2End);
		float mindist = Math.min(Math.min(dists[0], dists[1]),
				Math.min(dists[2], dists[3]));
		if (mindist == dists[0]) {
			float[][] rtn = { l1Start, l2Start };
			return rtn;
		} else if (mindist == dists[1]) {
			float[][] rtn = { l1Start, l2End };
			return rtn;
		} else if (mindist == dists[2]) {
			float[][] rtn = { l1End, l2Start };
			return rtn;
		} else if (mindist == dists[3]) {
			float[][] rtn = { l1End, l2End };
			return rtn;
		} else {
			System.out.println("error in closer ends");
			return null;
		}
	}
	
	public static float [][] findFarEnds (Line l1, Line l2) {
		float[] dists = new float[4];
		Tool tool = new Tool();
		float[] l1Start = { l1.col.get(0), l1.row.get(0) };
		float[] l1End = { l1.col.get(l1.num - 1), l1.row.get(l1.num - 1) };
		float[] l2Start = { l2.col.get(0), l2.row.get(0) };
		float[] l2End = { l2.col.get(l2.num - 1), l2.row.get(l2.num - 1) };
		dists[0] = tool.dist(l1Start, l2Start);
		dists[1] = tool.dist(l1Start, l2End);
		dists[2] = tool.dist(l1End, l2Start);
		dists[3] = tool.dist(l1End, l2End);
		float mindist = Math.max(Math.max(dists[0], dists[1]),
				Math.max(dists[2], dists[3]));
		if (mindist == dists[0]) {
			float[][] rtn = { l1Start, l2Start };
			return rtn;
		} else if (mindist == dists[1]) {
			float[][] rtn = { l1Start, l2End };
			return rtn;
		} else if (mindist == dists[2]) {
			float[][] rtn = { l1End, l2Start };
			return rtn;
		} else if (mindist == dists[3]) {
			float[][] rtn = { l1End, l2End };
			return rtn;
		} else {
			System.out.println("error in far ends");
			return null;
		}
	}

	// helper method to interpolate all points to connect the two open ends.
	public static ArrayList<float[]> interpolate_line(float[] op1, float[] op2) {
		ArrayList<float[]> linePoints = new ArrayList<float[]>();
		float x = op1[0];
		float y = op1[1];
		float w = (op2[0] - op1[0]);
		float h = (op2[1] - op1[1]);
		float dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (w < 0)
			dx1 = -1;
		else if (w > 0)
			dx1 = 1;
		if (h < 0)
			dy1 = -1;
		else if (h > 0)
			dy1 = 1;
		if (w < 0)
			dx2 = -1;
		else if (w > 0)
			dx2 = 1;
		int longest = Math.abs((int) w);
		int shortest = Math.abs((int) h);
		if (!(longest > shortest)) {
			longest = Math.abs((int) h);
			shortest = Math.abs((int) w);
			if (h < 0)
				dy2 = -1;
			else if (h > 0)
				dy2 = 1;
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for (int i = 0; i <= longest; i++) {
			float[] point = new float[2];
			point[0] = x;
			point[1] = y;
			linePoints.add(point);
			numerator += shortest;
			if (!(numerator < longest)) {
				numerator -= longest;
				x += dx1;
				y += dy1;
			} else {
				x += dx2;
				y += dy2;
			}
		}
		float[] lastPoint = { op2[0], op2[1] };
		linePoints.add(lastPoint);
		return linePoints;
	}

	public enum contour_class {
		cont_no_junc, /* no end point is a junction */
		cont_start_junc, /* only the start point of the line is a junction */
		cont_end_junc, /* only the end point of the line is a junction */
		cont_both_junc, /* both end points of the line are junctions */
		cont_closed; /* the contour is closed */
	}

	@Override
	public String toString() {
		return "Line " + ID;
	}

	@Override
	public int compareTo(Line o) {
		float thisStart = this.col.get(0);
		float thisEnd = this.col.get(this.num - 1);
		float oStart = o.col.get(0);
		float oEnd = o.col.get(o.num - 1);
		if (thisStart <= thisEnd) {
			if (oStart <= oEnd) {
				// both have start at the left
				// compare their start points
				if (thisStart == oStart) {
					return thisEnd > oEnd ? 1 : -1;
				}
				else {
					return thisStart > oStart ? 1 : -1;
				}
			} else {
				if (thisStart == oEnd) {
					return thisEnd > oStart ? 1 : -1;
				}
				else {
					return thisStart > oEnd ? 1 : -1;
				}
			}
		} else {
			if (oStart <= oEnd) {
				if (thisEnd==oStart) {
					return thisStart > oEnd ? 1:-1;
				}
				else {
					return thisEnd > oStart ? 1 : -1;
				}
			} else {
				if (thisEnd == oEnd) {
					return thisStart > oStart? 1:-1;
				}
				else {
					return thisEnd > oEnd ? 1 : -1;
				}
			}
		}
	}

}

class Lines extends ArrayList<Line> implements Comparable<Lines> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3939887299438046597L;
	public int frame;

	public Lines(int frame) {
		this.frame = frame;
	}

	public int getFrame() {
		return this.frame;
	}

	public Line getLineByID(int ID) {
		for (int i = 0; i < this.size(); ++i) {
			if (this.get(i).getID() == ID) {
				return this.get(i);
			}
		}
		System.out.println("failed to find such line with ID" + ID);
		return null;
	}

	public int getIndexByID(int ID) {
		for (int i = 0; i < this.size(); ++i) {
			if (this.get(i).getID() == ID) {
				return i;
			}
		}
		System.out.println("failed to find such line with ID" + ID);
		return -1;
	}

	@Override
	public int compareTo(Lines o) {
		return this.frame - o.frame;
	}
}

class PathID {
	GeneralPath gp;
	int ID;

	public PathID(GeneralPath gp, int ID) {
		this.gp = gp;
		this.ID = ID;
	}
}