package io.firstwave.generator;

/**
 * Created by waxwing on 1/31/14.
 */
public interface Level {
	public final static int FRIENDLY_MATRIX = 0;
	public final static int HOSTILE_MATRIX = 1;
	public final static int ASTEROID_MATRIX = 2;
	public final static int VALUE_MATRIX = 3;
	public LevelConfiguration getConfig();
	public double[][] getMatrix(int type);
	public int getSeed();
}
