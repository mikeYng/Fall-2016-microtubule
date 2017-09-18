import java.util.ArrayList;

// class for a set of lines identified as microtubules. 
public class Microtubule {
	int ID;
	Lines lines;
	Lines added;

	public Microtubule(Lines lines, int ID) {
		this.lines = lines;
		this.ID = ID;
		this.added = new Lines(lines.getFrame());
	}

	@Override
	public String toString() {
		return "Microtubule " + ID;
	}

	// to get the displacement from the end of the microtubule in frame 1.
	// orignalEnd -- the coordinate of one end of the microtubule in frame 1
	// lastEnd -- coordinate of the corresponding end in last frame
	// endvector -- the vector pointing outside to the end
	// return -- a double value, positve = growing, negative = shrinking
	public double getEndDisplacement(float [] newEnd,float[] originalEnd, float[] lastEnd,
			float[] endVector) {
		Tool t = new Tool();
		double dist = t.dist(originalEnd, newEnd);
		// now determine the direction
		float[] newVector = { newEnd[0] - originalEnd[0],
				newEnd[1] - originalEnd[1] };
		double dot = newVector[0] * endVector[0] + newVector[1] * endVector[1];
//		double normLine = Math.sqrt(Math.pow(newVector[0], 2)
//				+ Math.pow(newVector[1], 2));
//		double normMicro = Math.sqrt(Math.pow(endVector[0], 2)
//				+ Math.pow(endVector[1], 2));
//		double cos = dot/(normLine*normMicro);
//		if (Math.abs(cos) < 0.5) {
//			System.out.println("WTF, angle does not make sense at " + this.ID + " at frame: " + this.lines.get(0).getID());
//			System.out.println("distance is " + dist);
//		}
		if (dot > 0) {
			return dist;
		} else {
			return -dist;
		}
	}

	public void updateCoverage() {
		for (Line l : this.lines) {
			for (int i = 0; i < l.covered.length; ++i) {
				l.covered[i] = l.coverHelper[i];
			}
		}
	}

	public void revertCoverage() {
		for (Line l : this.lines) {
			for (int i = 0; i < l.covered.length; ++i) {
				l.coverHelper[i] = l.covered[i];
			}
		}
	}
}

class Loss implements Comparable<Loss> {
	// the loss of matching to this microtubule
	double loss;
	int ID; // id of the microtubule matched to
	int loc; // which frame in the buffer is loss associated to

	public Loss(double loss, int ID, int loc) {
		this.loss = loss;
		this.ID = ID;
		this.loc = loc;
	}

	public int compareTo(Loss l) {
		if (this.loss < l.loss) {
			return -1;
		} else if (this.loss > l.loss) {
			return 1;
		} else {
			return 0;
		}
	}

	public String toString() {
		if (loss == Double.MAX_VALUE) {
			return "With " + ID + " : inifinity";
		} else {
			return "With " + ID + " : " + loss;
		}

	}
}

class Microtubules extends ArrayList<Microtubule> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4319231960079695230L;
	private int frame;

	public Microtubule getMicroByID(int ID) {
		for (Microtubule micro : this) {
			if (micro.ID == ID) {
				return micro;
			}
		}
		System.out.println("Cannot find such microtubule with ID " + ID);
		return null;
	}

	public int getIndexByID(int ID) {
		for (int i = 0; i < this.size(); ++i) {
			if (this.get(i).ID == ID) {
				return i;
			}
		}
		System.out.println("Cannot find such microtubule with ID " + ID);
		return -1;
	}

	public Microtubules(int frame) {
		this.frame = frame;
	}

	public int getFrame() {
		return frame;
	}

}