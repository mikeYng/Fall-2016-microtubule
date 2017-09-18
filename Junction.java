import java.util.ArrayList;


public class Junction {
	float x;
	float y;
	int cont1; //ID of line1
	int cont2; //ID of line2
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cont1 + cont2;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
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
		Junction other = (Junction) obj;
		if ((cont1 != other.cont1) && (cont1!=other.cont2))
			return false;
		if ((cont2 != other.cont2) && (cont2!=other.cont1))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

}

class Junctions extends ArrayList<Junction> implements Comparable<Junctions>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7784738864508915334L;
	public int frame;
	
	public Junctions (int frame) {
		this.frame = frame;
	}
	public int getFrame() {
		return this.frame;
	}
	@Override
	public int compareTo(Junctions o) {
		return this.frame - o.frame;
	}
	
}