package io.firstwave.generator;

import io.firstwave.generator.noise.NoiseGenerator;
import io.firstwave.generator.noise.PerlinNoiseGenerator;
import io.firstwave.generator.noise.SimplexNoiseGenerator;

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

		final double[][][] matrices = new double[4][][];

		float scale = size * friendlyScale;
		matrices[Level.FRIENDLY_MATRIX] = getNoise((int) (size / scale), noiseMaker(seed));
		
		scale = size * hostileScale;
		matrices[Level.HOSTILE_MATRIX] = getNoise((int) (size / scale), noiseMaker(seed * -1));

		scale = size * asteroidScale;
		matrices[Level.ASTEROID_MATRIX] = getNoise((int) (size / scale), noiseMaker(seed - 1));

		scale = size * valueScale;
		matrices[Level.VALUE_MATRIX] = getNoise((int) (size / scale), noiseMaker(seed + 1));

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
			public double[][] getMatrix(int type) {
				return matrices[type];
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

	public static class InvalidConfigurationException extends RuntimeException {
		public InvalidConfigurationException(String message) {
			super(message);
		}
	}
}
