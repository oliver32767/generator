package io.firstwave.generator.noise;

/**
 * Created by waxwing on 2/1/14.
 */
public class RadialGradient {
	private final Curve curve;
	public RadialGradient(Curve curve) {
		this.curve = curve;
	}

	/**
	 * Calculate the value of the gradient at the given position
	 * The gradient is assumed to have a radius of 1.0f centered on 0.0f, 0.0f
	 * @param x
	 * @param y
	 * @return
	 */
	public double get(double x, double y) {
		return get(curve, x, y);
	}

	/**
	 * Calculate the value of the gradient at the given position using the given curve
	 * The gradient is assumed to have a radius of 1.0f centered on 0.0f, 0.0f
	 * @param curve
	 * @param x
	 * @param y
	 * @return
	 */
	public static double get(Curve curve, double x, double y) {
		double dist = Math.pow(Math.abs(x), 2) + Math.pow(Math.abs(y), 2);
		if (dist > 1.0f) dist = 1.0f;
		if (dist < 0.0f) dist = 0.0f;
		double rv = curve.calculate(dist);
		return rv;
	}
}
