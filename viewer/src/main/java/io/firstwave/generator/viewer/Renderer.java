package io.firstwave.generator.viewer;

import java.util.List;
import java.util.Properties;

/**
 * Created by waxwing on 2/1/14.
 */
public abstract class Renderer {
	private final MessageHandler messageHandler;
	public Renderer() {
		messageHandler = new MessageHandler() {
			@Override
			public void setMessage(String msg) {
				System.out.println(msg);
			}

			@Override
			public void setError(String msg) {
				System.err.println(msg);
			}
		};
	}

	public Renderer(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	protected MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public abstract List<Layer> render(Properties properties, MessageHandler messageHandler);

	protected int color(float a, float r, float g, float b) {
		int aa = Math.round(255 * constrain(a));
		int rr = Math.round(255 * constrain(r));
		int gg = Math.round(255 * constrain(g));
		int bb = Math.round(255 * constrain(b));
		return ((aa & 0xFF) << 24) |
				((rr & 0xFF) << 16) |
				((gg & 0xFF) << 8)  |
				((bb & 0xFF) << 0);
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
}
