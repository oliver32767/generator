package io.firstwave.generator.viewer;

import io.firstwave.generator.Level;
import io.firstwave.generator.LevelConfiguration;
import io.firstwave.generator.LevelGenerator;

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
        altLayer = new Layer("Value (Contour)", size, size, BufferedImage.TYPE_INT_ARGB);
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


		long renderEnd = System.currentTimeMillis();

		messageHandler.setMessage(String.format("Generated: %s Rendered: %s Total: %s",
				Main.convertMillis(genEnd - genStart),
				Main.convertMillis(renderEnd - renderStart),
				Main.convertMillis(renderEnd - genStart)));
		return layers;
	}

}
