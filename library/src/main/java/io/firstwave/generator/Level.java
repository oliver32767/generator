package io.firstwave.generator;

/**
 * Created by waxwing on 1/31/14.
 */
public interface Level {
	public final static int FRIENDLY = 0;
	public final static int HOSTILE = 1;
	public final static int ASTEROID = 2;
	public final static int VALUE = 3;

	public LevelConfiguration getConfig();

	public double[][] getRawNoise(int type);

	public double[][] getInterpolation(int type);

	public int getSeed();
}
