package io.firstwave.generator.renderer;

import io.firstwave.generator.*;
import io.firstwave.generator.viewer.ConfigurationUtil;
import io.firstwave.generator.viewer.Layer;
import io.firstwave.generator.viewer.Main;
import io.firstwave.generator.viewer.MessageHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by waxwing on 2/1/14.
 */
public class LevelRenderer extends Renderer {

	public LevelRenderer(MessageHandler messageHandler) {
		super(messageHandler);
	}

	@Override
	public List<Layer> render(Properties properties) {
		Configuration config = ConfigurationUtil.fromProperties(properties);

		// Level Generation
		long genStart = System.currentTimeMillis();
		final Level level = LevelGenerator.configure(config).generate();
		long genEnd = System.currentTimeMillis();
		long renderStart = genEnd;

		// Level Configuration
		int size = config.getInteger("size", 1024);


		final float asteroidLow = config.getFloat("asteroidLow", 0.1f);
		final float asteroidHi = config.getFloat("asteroidHi", 0.5f);
		final float oreDepth = config.getFloat("oreDepth", 0.9f);

		final float valueBracket = config.getFloat("valueBracket", 0.1f);
		final float valueAlpha = config.getFloat("valueAlpha", 0.5f);

		final float enemyDensity = config.getFloat("enemyDensity", 2f);
		final float asteroidDensity = config.getFloat("asteroidDensity", 3f);
		final float oreValue = config.getFloat("oreValue", 0.5f);
		final float oreDensity = config.getFloat("oreDensity", 1f);
		final float oreThresh = (asteroidHi - asteroidLow) * oreDepth / 2; // ore depth gives us the depth we need to

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
        altLayer = new Layer("Ore Depth", size, size);

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

		// Render entities /////////////////////////////////////////////////////////////////////////////////////////////

		layer = new Layer("Asteroids", size, size);
		points = PointProcessor.get(size, size, new PointProcessor.Predicate() {
			@Override
			public boolean match(int x, int y) {
				return (level.getInterpolation(Level.ASTEROID)[x][y] > 0.0f);
			}
		});
		for (int i = 0; i < asteroidDensity * (float) points.size() / (float) size ; i++) {
			p = points.get(rand.nextInt(points.size() - 1));
			r = rand.nextInt((int) (10 * (level.getInterpolation(Level.VALUE)[p.x][p.y] + 1)));
			drawCircle(layer, p.x, p.y, r, Color.GRAY);
		}
		layers.add(layer);

		layer = new Layer("Enemies", size, size);
		points = PointProcessor.get(size, size, new PointProcessor.Predicate() {
			@Override
			public boolean match(int x, int y) {
				return (level.getInterpolation(Level.HOSTILE)[x][y] > 0.0f);
			}
		});
		for (int i = 0; i < enemyDensity * (float) points.size() / (float) size; i++) {
			p = points.get(rand.nextInt(points.size() - 1));
			drawCircle(layer, p.x, p.y, 2, Color.RED);
		}
		layers.add(layer);

		layer = new Layer("Ore", size, size);
		points = PointProcessor.get(size, size, new PointProcessor.Predicate() {
			double a, b;
			@Override
			public boolean match(int x, int y) {
				a = level.getInterpolation(Level.ASTEROID)[x][y];
				b = level.getInterpolation(Level.VALUE)[x][y];
				if (a > asteroidLow + oreThresh && a < asteroidHi - oreThresh) {
					// then we are in acceptable ore depth zone
					return (b >= oreValue);
				}
				return false;
			}
		});
		for (int i = 0; i < oreDensity * (float) points.size() / (float) size; i++) {
			p = points.get(rand.nextInt(points.size() - 1));
			drawCircle(layer, p.x, p.y, 3, Color.CYAN);
		}
		layers.add(layer);


		long renderEnd = System.currentTimeMillis();

		getMessageHandler().setMessage(String.format("Generated: %s Rendered: %s Total: %s",
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
