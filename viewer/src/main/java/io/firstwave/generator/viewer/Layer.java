package io.firstwave.generator.viewer;

import java.awt.image.BufferedImage;

/**
 * Created by waxwing on 2/2/14.
 */
public class Layer extends BufferedImage {

	private final String name;
	public Layer(String name, int width, int height, int imageType) {
		super(width, height, imageType);
		this.name = name;
	}

    public Layer(String name, int width, int height) {
        super(width, height, TYPE_INT_ARGB);
        this.name = name;
    }

	public Layer(String name, BufferedImage img) {
		super(img.getWidth(), img.getHeight(), img.getType());
		this.name = name;
		getGraphics().drawImage(img, 0 ,0, null);
	}

	public String getName() {
		return name;
	}
}
