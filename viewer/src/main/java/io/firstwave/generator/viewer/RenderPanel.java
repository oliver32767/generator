package io.firstwave.generator.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by waxwing on 1/31/14.
 */
public class RenderPanel extends JComponent {
	private double zoom = 1.0;
	BufferedImage img;

	public void setImage(BufferedImage img) {
		this.img = img;
		setZoom(zoom);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension dim = getPreferredSize();
		g.drawImage(img, 0, 0, dim.width, dim.height, this);
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
		if (img == null) return;
		int w = (int) (zoom * img.getWidth());
		int h = (int) (zoom * img.getHeight());
		setPreferredSize(new Dimension(w, h));
		revalidate();
		repaint();
	}

}
