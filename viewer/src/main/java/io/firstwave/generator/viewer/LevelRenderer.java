package io.firstwave.generator.viewer;

import io.firstwave.generator.Level;
import io.firstwave.generator.LevelConfiguration;
import io.firstwave.generator.LevelGenerator;
import io.firstwave.generator.PointProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

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
		final Level level = LevelGenerator.configure(config).generate();
		long genEnd = System.currentTimeMillis();
		long renderStart = genEnd;

		// Level Configuration
		int size = config.getInteger("size", 1024);


		float asteroidLow = config.getFloat("asteroidLow", 0.1f);
		float asteroidHi = config.getFloat("asteroidHi", 0.5f);
		float oreDepth = config.getFloat("oreDepth", 0.9f);

		float valueBracket = config.getFloat("valueBracket", 0.1f);
		float valueAlpha = config.getFloat("valueAlpha", 0.5f);


		List<Layer> layers = new ArrayList<Layer>();
		Layer layer, altLayer;

		try {
			String res = properties.getProperty("level.bg");
			if (res != null) {
				layer = new Layer("Background", size, size);
				BufferedImage bg = ImageIO.read(ClassLoader.getSystemResourceAsStream(res));
				int srcSize = (bg.getHeight() < bg.getWidth()) ? bg.getHeight() : bg.getWidth();
				layer.getGraphics().drawImage(bg, 0, 0, size, size, 0, 0, srcSize, srcSize, null);
				layers.add(layer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		double value;
        layer = new Layer("Friendly", size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                value = level.getInterpolation(Level.FRIENDLY)[x][y];
                layer.setRGB(x, y, color((float) value, 0.0f, 1.0f, 0.0f));
            }
        }
		layers.add(layer);

        layer = new Layer("Hostile", size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                value = level.getInterpolation(Level.HOSTILE)[x][y];
                layer.setRGB(x, y, color((float) value, 1.0f, 0.0f, 0.0f));
            }
        }
        layers.add(layer);

        layer = new Layer("Asteroid", size, size);
        altLayer = new Layer("Ore", size, size);
        float oreThresh = (asteroidHi - asteroidLow) * oreDepth / 2; // ore depth gives us the depth we need to
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                value = level.getInterpolation(Level.ASTEROID)[x][y];
                    if (value > asteroidLow + oreThresh && value < asteroidHi - oreThresh) {
						altLayer.setRGB(x, y, color((float) value, 0.0f, 0.0f, 1.0f));
					}
                    layer.setRGB(x, y, color((float) value, 0.5f, 0.5f, 0.5f));
	       }
        }
        layers.add(layer);
        layers.add(altLayer);

        layer = new Layer("Value", size, size);
        altLayer = new Layer("Value (Contour)", size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                value = level.getInterpolation(Level.VALUE)[x][y];
                if (value % valueBracket < 0.005f) {
                    altLayer.setRGB(x, y, color(valueAlpha * ((float) value - ((float) value % valueBracket)), 1.0f, 1.0f, 0.0f));
                }
                layer.setRGB(x, y, color(valueAlpha * ((float) value - ((float) value % valueBracket)), 1.0f, 1.0f, 0.0f));
            }
        }
        layers.add(layer);
        layers.add(altLayer);

		Random rand = new Random(level.getSeed());
		PointProcessor.Point p;
		List<PointProcessor.Point> points;
		int r;

		layer = new Layer("Asteroids", size, size);
		points = PointProcessor.get(size, size, new PointProcessor.Predicate() {
			@Override
			public float weigh(int x, int y) {
				if (level.getInterpolation(Level.ASTEROID)[x][y] > 0.0f) {
					return 1.0f;
				} else {
					return IGNORE;
				}
			}
		});

		Collections.shuffle(points, rand);
		Collections.sort(points);

		for (int i = 0; i < 3 * (float) points.size() / (float) size ; i++) {
			p = points.get(rand.nextInt(points.size() - 1));
			r = rand.nextInt((int) (10 * (level.getInterpolation(Level.VALUE)[p.x][p.y] + 1)));
			drawCircle(layer, p.x, p.y, r, Color.GRAY);
		}
		layers.add(layer);


		layer = new Layer("Enemies", size, size);
		points = PointProcessor.get(size, size, new PointProcessor.Predicate() {
			@Override
			public float weigh(int x, int y) {
				if (level.getInterpolation(Level.HOSTILE)[x][y] > 0.0f) {
					return (float) level.getInterpolation(Level.VALUE)[x][y];
				}
				return IGNORE;
			}
		});

		// Compute the total weight of all items together
		double totalWeight = 0.0d;
		for (int i = 0; i < 2 * ((float) points.size() / size); i++) {
			for (PointProcessor.Point point : points)
			{
				totalWeight += point.weight;
			}
			// Now choose a random item
			int randomIndex = -1;
			double random = rand.nextDouble() * totalWeight;
			for (int ii = 0; i < points.size(); ++ii)
			{
				random -= points.get(ii).weight;
				if (random <= 0.0d)
				{
					randomIndex = ii;
					break;
				}
			}
			PointProcessor.Point randomPoint = points.get(randomIndex);
			drawCircle(layer, randomPoint.x, randomPoint.y, 2, Color.RED);
		}
		layers.add(layer);


//		layer = new Layer("Enemies", size, size);
//		points = PointProcessor.get(level.getInterpolation(Level.HOSTILE), new PointProcessor.Predicate() {
//			@Override
//			public boolean matches(int x, int y, double value) {
//				return (value > 0.0);
//			}
//		});
//		for (int i = 0; i < 2 * (float) points.size() / (float) size; i++) {
//			p = points.get(rand.nextInt(points.size() - 1));
//			drawCircle(layer, p.x, p.y, 2, Color.RED);
//		}
//		layers.add(layer);
//
//		layer = new Layer("More Enemies", size, size);
//		points = PointProcessor.get(level.getInterpolation(Level.HOSTILE), new PointProcessor.Predicate() {
//			double v;
//
//			@Override
//			public boolean matches(int x, int y, double value) {
//				LevelConfiguration config = level.getConfig();
//				float asteroidLow = config.getFloat("asteroidLow", 0.1f);
//				float asteroidHi = config.getFloat("asteroidHi", 0.5f);
//				float oreDepth = config.getFloat("oreDepth", 0.9f);
//				float oreThresh = (asteroidHi - asteroidLow) * oreDepth / 2; // ore depth gives us the depth we need to
//				if (value > 0.0) {
//					v = level.getInterpolation(Level.ASTEROID)[x][y];
//					return (v > asteroidLow + oreThresh && v < asteroidHi - oreThresh);
//				}
//				return false;
//			}
//		});
//		for (int i = 0; i < 2 * (float) points.size() / (float) size; i++) {
//			p = points.get(rand.nextInt(points.size() - 1));
//			drawCircle(layer, p.x, p.y, 3, Color.RED.brighter());
//		}
//		layers.add(layer);


		long renderEnd = System.currentTimeMillis();

		messageHandler.setMessage(String.format("Generated: %s Rendered: %s Total: %s",
				Main.convertMillis(genEnd - genStart),
				Main.convertMillis(renderEnd - renderStart),
				Main.convertMillis(renderEnd - genStart)));
		return layers;
	}

	private void drawCircle(Layer layer, int x, int y, int radius, Paint paint) {
		Graphics2D g = layer.createGraphics();
		g.setPaint(paint);
		g.fillOval(x, y, radius * 2, radius * 2);
		g.dispose();
	}

}
