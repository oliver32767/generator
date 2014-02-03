package io.firstwave.generator.noise;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by waxwing on 2/1/14.
 */
public abstract class Curve {
	private static final float PI = 3.14159265f;
	public abstract double calculate(double t);

	public static final Curve NONE = new Curve() {
		@Override
		public double calculate(double t) {
			return 1.0f;
		}
	};


	public static final Curve LINEAR = new Curve() {
		@Override
		public double calculate(double t) {
			return t;
		}
	};

	public static final Curve CUBIC_OUT = new Curve() {

		@Override
		public double calculate(double t) {
			return (t-=1)*t*t + 1;
		}
	};

	public static final Curve CUBIC_IN = new Curve() {

		@Override
		public double calculate(double t) {
			return t * t * t;
		}
	};

	public static final Curve SINE_INOUT = new Curve() {
		@Override
		public double calculate(double t) {
			return -0.5f * ((float) Math.cos(PI*t) - 1);
		}
	};
	private static final Map<String, Curve> lookupMap;

	static {
		lookupMap = new HashMap<String, Curve>();
		lookupMap.put("none", NONE);
		lookupMap.put("linear", LINEAR);
		lookupMap.put("cubicOut", CUBIC_OUT);
		lookupMap.put("cubicIn", CUBIC_IN);
		lookupMap.put("sineInOut", SINE_INOUT);

		lookupMap.put("linearInv", LINEAR.invert());
		lookupMap.put("cubicOutInv", CUBIC_OUT.invert());
		lookupMap.put("cubicInInv", CUBIC_IN.invert());
		lookupMap.put("sineInOutInv", SINE_INOUT.invert());
	}

	public static Curve lookup(String curveName) {
		if (lookupMap.containsKey(curveName)) return lookupMap.get(curveName);
		return NONE;
	}

	public Curve invert() {
		return new InvertedCurve(this);
	}

	private static class InvertedCurve extends Curve {
		private final Curve curve;
		public InvertedCurve(Curve curve) {
			this.curve = curve;
		}
		@Override
		public double calculate(double t) {
			return 1.0f + (-1.0f * calculateImpl(t));
		}

		private double calculateImpl(double t) {
			return curve.calculate(t);
		}
	}
}
