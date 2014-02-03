package io.firstwave.generator.viewer;

import io.firstwave.generator.Level;
import io.firstwave.generator.LevelConfiguration;
import io.firstwave.generator.LevelGenerator;
import io.firstwave.generator.noise.Curve;
import io.firstwave.generator.noise.Interpolator;
import io.firstwave.generator.noise.RadialGradient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by waxwing on 2/1/14.
 */
public class LevelRenderer extends Renderer {

	public LevelRenderer(MessageHandler messageHandler) {
		super(messageHandler);
	}

	@Override
	public List<Layer> render(Properties properties, MessageHandler messageHandler) {
		LevelConfiguration config = new LevelConfiguration(properties);

		// Level Generation
		long genStart = System.currentTimeMillis();
		Level level = LevelGenerator.configure(config).generate();
		long genEnd = System.currentTimeMillis();
		long renderStart = genEnd;

		// Level Configuration
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


		List<Layer> layers = new ArrayList<Layer>();
		Layer layer;

		try {
			String res = properties.getProperty("level.bg");
			if (res != null) {
				layer = new Layer("Background", size, size, BufferedImage.TYPE_INT_ARGB);
				BufferedImage bg = ImageIO.read(ClassLoader.getSystemResourceAsStream(res));
				int srcSize = (bg.getHeight() < bg.getWidth()) ? bg.getHeight() : bg.getWidth();
				layer.getGraphics().drawImage(bg, 0, 0, size, size, 0, 0, srcSize, srcSize, null);
				layers.add(layer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// FRIENDLY ////////////////////////////////////////////////////////////////////////////////////////////////////
		layer = new Layer("Friendly", size, size, BufferedImage.TYPE_INT_ARGB);
		float scale = size * friendlyScale;
		double[][] noise = level.getMatrix(Level.FRIENDLY_MATRIX);

		Interpolator interpolator = Interpolator.CUBIC;
		RadialGradient gradient = new RadialGradient(Curve.lookup(friendlyCurve));
		double value;
		double gx, gy;
		for (int x = 0; x < size; x++ ) {
			for (int y = 0; y < size; y++) {
				value = interpolator.get(noise, (double) x / scale, (double) y / scale);
				gx = x - (size / 2);
				gy = y - (size / 2);
				gx = gx * (2.0f / size);
				gy = gy * (2.0f / size);
				value = normalize(value) * constrain(gradient.get(gx, gy) + friendlyIntensity);
				if (value > friendlyThresh)
					layer.setRGB(x, y, color((float) value, 0.0f, 1.0f, 0.0f));
			}
		}
		layers.add(layer);

		// HOSTILE /////////////////////////////////////////////////////////////////////////////////////////////////////
		layer = new Layer("Hostile", size, size, BufferedImage.TYPE_INT_ARGB);
		scale = size * hostileScale;
		noise = level.getMatrix(Level.HOSTILE_MATRIX);

		gradient = new RadialGradient(Curve.lookup(hostileCurve));
		for (int x = 0; x < size; x++ ) {
			for (int y = 0; y < size; y++) {
				value = interpolator.get(noise, (double) x / scale, (double) y / scale);
				gx = x - (size / 2);
				gy = y - (size / 2);
				gx = gx * (2.0f / size);
				gy = gy * (2.0f / size);
				value = normalize(value) * constrain(gradient.get(gx, gy) + hostileIntensity);
				if (value > hostileThresh)
					layer.setRGB(x, y, color((float) value, 1.0f, 0.0f, 0.0f));
			}
		}
		layers.add(layer);


		// ASTEROID ////////////////////////////////////////////////////////////////////////////////////////////////////
		layer = new Layer("Asteroid", size, size, BufferedImage.TYPE_INT_ARGB);
		scale = size * asteroidScale;
		float oreThresh = (asteroidHi - asteroidLow) * oreDepth / 2; // ore depth gives us the depth we need to
		// penetrate in to an asteroid region before we find asteroids,
		// i.e. a depth of .9 means we need to penetrate at least 90% of the way to the center

		noise = level.getMatrix(Level.ASTEROID_MATRIX);
		gradient = new RadialGradient(Curve.lookup(asteroidCurve));
		for (int x = 0; x < size; x++ ) {
			for (int y = 0; y < size; y++) {
				value = interpolator.get(noise, (double) x / scale, (double) y / scale);
				gx = x - (size / 2);
				gy = y - (size / 2);
				gx = gx * (2.0f / size);
				gy = gy * (2.0f / size);
				value = normalize(value) * constrain(gradient.get(gx, gy) + asteroidIntensity);

				if (value > asteroidLow && value < asteroidHi) {
					if (value > asteroidLow + oreThresh && value < asteroidHi - oreThresh) {
						layer.setRGB(x, y, color((float) value, 0.0f, 0.0f, 1.0f));
					} else {
						layer.setRGB(x, y, color((float) value, 0.5f, 0.5f, 0.5f));
					}
				}
			}
		}
		layers.add(layer);

		// VALUE //////////////////////////////////////////////////////////////////////////////////////////////////////
		layer = new Layer("Value", size, size, BufferedImage.TYPE_INT_ARGB);
		scale = size * valueScale;
		noise = level.getMatrix(Level.VALUE_MATRIX);
		gradient = new RadialGradient(Curve.lookup(valueCurve));
		for (int x = 0; x < size; x++ ) {
			for (int y = 0; y < size; y++) {
				value = interpolator.get(noise, (double) x / scale, (double) y / scale);
				gx = x - (size / 2);
				gy = y - (size / 2);
				gx = gx * (2.0f / size);
				gy = gy * (2.0f / size);
				value = normalize(value) * constrain(gradient.get(gx, gy) + hostileIntensity);
				if (valueContour.equals("wire")) {
					if (value % valueBracket < 0.005f)
						layer.setRGB(x, y, color(valueAlpha * ((float) value - ((float) value % valueBracket)), 1.0f, 1.0f, 0.0f));
				} else if (valueContour.equals("flat")) {
					layer.setRGB(x, y, color(valueAlpha * ((float) value - ((float) value % valueBracket)), 1.0f, 1.0f, 0.0f));
				}

			}
		}
		layers.add(layer);

		long renderEnd = System.currentTimeMillis();

		messageHandler.setMessage(String.format("Generated: %s Rendered: %s Total: %s",
				Main.convertMillis(genEnd - genStart),
				Main.convertMillis(renderEnd - renderStart),
				Main.convertMillis(renderEnd - genStart)));
		return layers;
	}

}
