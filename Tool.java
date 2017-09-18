import java.util.ArrayList;
import java.util.List;


public class Tool {
	
	public float dist(final float[] p1, final float[] p2) {
		return (float) Math.sqrt(Math.pow((p2[0] - p1[0]), 2) + Math.pow((p2[1] -
			p1[1]), 2));
	}
	
	public float straightCalc(final float[]... points) {
		float ideal = dist(points[0], points[points.length - 1]);
		float sum = 0;
		for (int i = 1; i < points.length; i++)
			sum += dist(points[i - 1], points[i]);

		return sum / ideal;
	}
	
	public float[] getInterceptPoint(final float[] p1, final Line query) {
		int dist = 0;
		final List<float[]> points = new ArrayList<float[]>();
		points.add(p1);

		return findLongestPath(query, dist, points);
	}
	
	
	private float[] findLongestPath(final Line query, int dist, final List<float[]> points) {
		// p1 is the original point to test
		final float[] p1 = points.get(0);
		// Incrementally check the next position
		dist += 5;

		// pos is is the position in the query line to check
		int pos = 0;

		// Determine the position to check based on whether the query line intersects
		// with p1 at its start or end
		if (Math.abs(p1[0] - query.col.get(0)) < 4.0f && Math.abs(p1[1] -
			query.row.get(0)) < 4.0f)
		{
			pos = Math.min(dist, query.num - 1);
		}
		else {
			pos = Math.max(0, query.num - 1 - dist);
		}

		final float[] p2 = new float[2];

		p2[0] = query.col.get(pos);
		p2[1] = query.row.get(pos);


		// if our position has reached the start (or end) of the query line,
		// return p2.
		if (pos == 0 || pos == query.num -1) {
			return p2;
		}

		// If we still have a straight enough line, recurse to the next position
		points.add(p2);
		if (straightCalc(points.toArray(new float[points.size()][])) <= 1.02f) {
			return findLongestPath(query, dist, points);
		}

		// If the line is not straight enough, return the previous point.
		return points.get(points.size() - 2);
	}

}
