package io.firstwave.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Totally naive way of filtering out a matrix of noise data for matching points.
 * The best way to use this would be to cache the results,
 * because we have to check each and every individual point against matching conditions.
 */
public class PointProcessor {

	public static List<Point> get(int w, int h, Predicate predicate) {
		List<Point> points = new ArrayList<Point>();
		float weight;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				weight = predicate.weigh(x, y);
				if (weight > 0.0) {
					points.add(new Point(x, y, weight));
				}
			}
		}
		return points;
	}

	public static class Point implements Comparable<Point> {
		public final int x, y;
		public final float weight;
		public Point(int x, int y, float weight) {
			this.x = x;
			this.y = y;
			this.weight = weight;
		}


		@Override
		public int compareTo(Point point) {
			return Float.compare(this.x, point.x);
		}
	}

	public static interface Predicate {
		public static final float IGNORE = -1.0f;
		/**
		 * Assign a weight to a point at the given coordinates. Points with a weight <= 0.0 will be discarded.
		 * @param x
		 * @param y
		 * @return
		 */
		public float weigh(int x, int y);
	}

}
