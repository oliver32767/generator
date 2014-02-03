package io.firstwave.generator;

import io.firstwave.generator.noise.*;

/**
 * Created by waxwing on 1/31/14.
 */
public class LevelGenerator {


	private final LevelConfiguration config;

	private LevelGenerator(LevelConfiguration config) {
		this.config = config;
	}

	public static LevelGenerator configure(LevelConfiguration config) {
		return new LevelGenerator(config);
	}

	public Level generate() {
		if (config.getInteger(LevelConfiguration.SEED) == null) {
			throw new InvalidConfigurationException("seed cannot be null!");
		}
		int seed = config.getInteger(LevelConfiguration.SEED);
		float noiseScale = config.getFloat("noiseScale", 0.1f);
		int size = config.getInteger("size", 1024);

		float hostileScale = config.getFloat("hostileScale", noiseScale);
		float friendlyScale = config.getFloat("friendlyScale", noiseScale);
		float asteroidScale = config.getFloat("asteroidScale", noiseScale);
		float valueScale = config.getFloat("valueScale", noiseScale);


		float hostileThresh = config.getFloat("hostileThresh", 0.5f);
		float hostileIntensity = config.getFloat("hostileIntensity", 0.0f);
		String hostileCurve = config.getString("hostileCurve", "cubicIn");

		float friendlyThresh = config.getFloat("friendlyThresh", 0.1f);
		float friendlyIntensity = config.getFloat("friendlyIntensity", 0.0f);
		String friendlyCurve = config.getString("friendlyCurve", "cubicOut");

		float asteroidLow = config.getFloat("asteroidLow", 0.1f);
		float asteroidHi = config.getFloat("asteroidHi", 0.5f);
		float asteroidIntensity = config.getFloat("asteroidIntensity", 0.0f);
		float oreDepth = config.getFloat("oreDepth", 0.9f);
		String asteroidCurve = config.getString("asteroidCurve", "sineInOut");

		float valueBracket = config.getFloat("valueBracket", 0.1f);
		String valueContour = config.getString("valueContour", "wire");
		String valueCurve = config.getString("valueCurve", "none");
		float valueAlpha = config.getFloat("valueAlpha", 0.5f);

		final double[][][] matrices = new double[4][][];
		final double[][][] interpolations = new double[4][][];

		float scale = size * friendlyScale;
		matrices[Level.FRIENDLY] = getNoise((int) (size / scale), noiseMaker(seed));
		interpolations[Level.FRIENDLY] = getInterpolation(matrices[Level.FRIENDLY], size, Interpolator.CUBIC, Curve.lookup(friendlyCurve), friendlyIntensity, friendlyThresh, 1.0f);

		scale = size * hostileScale;
		matrices[Level.HOSTILE] = getNoise((int) (size / scale), noiseMaker(seed * -1));
		interpolations[Level.HOSTILE] = getInterpolation(matrices[Level.HOSTILE], size, Interpolator.CUBIC, Curve.lookup(hostileCurve), hostileIntensity, hostileThresh, 1.0f);

		scale = size * asteroidScale;
		matrices[Level.ASTEROID] = getNoise((int) (size / scale), noiseMaker(seed - 1));
		interpolations[Level.ASTEROID] = getInterpolation(matrices[Level.ASTEROID], size, Interpolator.CUBIC, Curve.lookup(asteroidCurve), asteroidIntensity, asteroidLow, asteroidHi);

		scale = size * valueScale;
		matrices[Level.VALUE] = getNoise((int) (size / scale), noiseMaker(seed + 1));
		interpolations[Level.VALUE] = getInterpolation(matrices[Level.VALUE], size, Interpolator.CUBIC, Curve.lookup(valueCurve), 0.0f, 0.0f, 1.0f);

		final int finalSeed = seed;
		return new Level() {
			@Override
			public LevelConfiguration getConfig() {
				return config;
			}

			@Override
			public int getSeed() {
				return finalSeed;
			}

			@Override
			public double[][] getRawNoise(int type) {
				return matrices[type];
			}

			@Override
			public double[][] getInterpolation(int type) {
				return interpolations[type];
			}
		};
	}

	private NoiseGenerator noiseMaker(int seed) {
		if (config.getBoolean("useSimplex", true)) {
			return new SimplexNoiseGenerator(seed);
		}
		return new PerlinNoiseGenerator(seed);
	}

	private double[][] getNoise(int size, NoiseGenerator generator) {
		double[][] rv = new double[size][size];
		for (int x = 0; x < rv.length; x++) {
			for (int y = 0; y < rv[0].length; y++) {
				rv[x][y] = generator.noise(x, y);
			}
		}
		return rv;
	}

	private double[][] getInterpolation(double[][] matrix, int size, Interpolator interpolator, Curve gradientCurve, float intensity, float thresholdMin, float thresholdMax) {
		RadialGradient gradient = new RadialGradient(gradientCurve);
		float scale = (float) matrix.length / (float) size;
		double value;
		double gx, gy;
		double[][] rv = new double[size][size];

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				value = interpolator.get(matrix, (double) x * scale, (double) y * scale);
				gx = x - (size / 2);
				gy = y - (size / 2);
				gx = gx * (2.0f / size);
				gy = gy * (2.0f / size);
				value = normalize(value) * constrain(gradient.get(gx, gy) + intensity);
				if (value > thresholdMin && value < thresholdMax) {
					rv[x][y] = value;
				} else {
					rv[x][y] = 0.0;
				}
			}
		}
		return rv;
	}

	protected double normalize(double v) {
		return (v + 1) / 2f;
	}

	protected float constrain(float f) {
		if (f < 0.0f) return 0.0f;
		if (f > 1.0f) return 1.0f;
		return f;
	}

	protected double constrain(double d) {
		if (d < 0.0f) return 0.0f;
		if (d > 1.0f) return 1.0f;
		return d;
	}

	public static class InvalidConfigurationException extends RuntimeException {
		public InvalidConfigurationException(String message) {
			super(message);
		}
	}
}
