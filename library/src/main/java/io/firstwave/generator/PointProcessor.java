package io.firstwave.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Totally naive way of processing a two dimensional array of data. If the given predicate returns a value of true,
 * then a point corresponding to the given x, y will be added to the list of matching points
 */
public class PointProcessor {
	public static List<Point> get(int w, int h, Predicate predicate) {
		return get(0, 0, w, h, predicate);
	}

	public static List<Point> get(int x, int y, int w, int h, Predicate predicate) {
		List<Point> points = new ArrayList<Point>();
		for (int u = x; u < x + w; u++) {
			for (int v = y; v < y + h; v++) {
				if (predicate.match(u, v))
					points.add(new Point(u, v));
				if (predicate.isCanceled) break;
			}
			if (predicate.isCanceled) break;
		}
		return points;
	}

	public static class Point {
		public final int x, y;
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public static abstract class Predicate {
		boolean isCanceled = false;
		/**
		 * Determine whether a point at the given coordinates matches specific conditions.
		 * Matching points will be added to the list of returned points.
		 * @param x
		 * @param y
		 * @return
		 */
		public abstract boolean match(int x, int y);

		/**
		 * Calling this will cancel further processing. The Processor will still return the list of points matched up to this point.
		 */
		protected void cancel() {
			isCanceled = true;
		}
	}

}
