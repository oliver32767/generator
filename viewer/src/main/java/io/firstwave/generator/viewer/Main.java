package io.firstwave.generator.viewer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by waxwing on 1/31/14.
 */
public class Main {
	private static int lastSeed = 0;
	private static MainForm form;
	private static RenderPanel renderPanel;
	private static MessageHandler messageHandler;
	private static CheckBoxList layerList;
	private static List<Layer> layers;

	public static void main(String args[]) throws Exception {
		// take the menu bar off the jframe
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// set the name of the application menu item
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Generator Viewer");

		// set the look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final JFrame frame = new JFrame("Generator Viewer");
		form = new MainForm();
		renderPanel = new RenderPanel();

		// set up menu
		final JMenu actions = new JMenu("Actions");
		actions.setMnemonic('A');

		final JMenuItem renderItem = new JMenuItem("Render");
		renderItem.setMnemonic('R');
		renderItem.setAccelerator(KeyStroke.getKeyStroke('R', KeyEvent.CTRL_DOWN_MASK));
		renderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRender();
			}
		});
		actions.add(renderItem);

		final JMenuItem copySeedItem = new JMenuItem("Copy last seed to clipboard");
		copySeedItem.setMnemonic('S');
		copySeedItem.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));
		copySeedItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection selection = new StringSelection(String.valueOf(lastSeed));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
		actions.add(copySeedItem);

		final JMenuItem copyConfigItem = new JMenuItem("Copy config to clipboard");
		copyConfigItem.setMnemonic('C');
		copyConfigItem.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		copyConfigItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection selection = new StringSelection(form.getConfig().getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
		actions.add(copyConfigItem);

		final JMenuItem pasteConfigItem = new JMenuItem("Paste clipboard to config");
		pasteConfigItem.setMnemonic('P');
		pasteConfigItem.setAccelerator(KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		pasteConfigItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				try {
					String contents = (String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor);
					form.getConfig().setText(contents);
					form.getTabbedPane().setSelectedIndex(1);
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		final JMenuItem reloadConfig = new JMenuItem("Reload default config");
		reloadConfig.setMnemonic('D');
		reloadConfig.setAccelerator(KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		reloadConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadConfig();
				form.getTabbedPane().setSelectedIndex(1);
			}
		});
		actions.add(reloadConfig);



		actions.add(pasteConfigItem);
		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(actions);

		reloadConfig();
		form.getResetButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadConfig();
			}
		});

		// Set up zoom slider
		final int zoomDefault = 25;
		final float zoomScale = 4f;
		form.getZoom().setValue(zoomDefault);
		form.getZoom().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				renderPanel.setZoom(zoomScale * form.getZoom().getValue() / form.getZoom().getMaximum());
				form.getResetZoom().setText(String.format("%.2f", zoomScale * form.getZoom().getValue() / form.getZoom().getMaximum()));
			}
		});
		form.getResetZoom().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				form.getZoom().setValue(zoomDefault);
			}
		});

		// Go button
		form.getButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRender();
			}
		});

		form.getRendererSelector().addItem("LevelRenderer");

		form.getScrollPane().setViewportView(renderPanel);
		form.getScrollPane().getViewport().setBackground(Color.BLACK);
		frame.setSize(640, 480);
		frame.add(form.getRoot());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int width = frame.getContentPane().getWidth();
				width -= form.getRendererSelector().getWidth();
				width -= form.getButton().getWidth();
				form.getMessage().setPreferredSize(new Dimension(width, form.getMessage().getHeight()));
			}
		});

		messageHandler = new MessageHandler() {
			final Color messageForeground = form.getMessage().getForeground();
			@Override
			public void setMessage(String msg) {
				form.getMessage().setForeground(messageForeground);
				form.getMessage().setText(String.format("Seed: %d %s", lastSeed, msg));
				System.out.println(msg);
			}

			@Override
			public void setError(String msg) {
				form.getMessage().setForeground(Color.RED);
				form.getMessage().setText(msg);
				System.err.println(msg);
			}
		};

		// layer list
		layerList = new CheckBoxList();
		layerList.setCallback(new CheckBoxList.Callback() {
			@Override
			public void onSelectionChanged(JCheckBox source) {
				drawLayers();
			}
		});
		layers = new ArrayList<Layer>();
		updateLayerList();
		form.getChecklist().setViewportView(layerList);
		form.getSplitPane().setDividerLocation(form.getSplitPane().getSize().width - 200);
		form.getSplitPane().setResizeWeight(1.0);

		doRender();
	}

	static void doRender() {
		form.getTabbedPane().setSelectedIndex(0);
		Thread renderThread = new Thread() {
			@Override
			public void run() {
				try {

					form.getButton().setEnabled(false);

					Properties p = parsePropertiesString(form.getConfig().getText());
					if (p.containsKey("seed")) {
						lastSeed = Integer.valueOf(p.getProperty("seed"));
					} else {
						lastSeed = new Random().nextInt();
						p.setProperty("seed", String.valueOf(lastSeed));
					}
					Renderer renderer = getRenderer();
					messageHandler.setMessage(String.format(p.getProperty("ui.working", "%s started..."), renderer.getClass().getSimpleName()));


					layers = getRenderer().render(p, messageHandler);
					updateLayerList();
					drawLayers();

				} catch (Exception e) {
					e.printStackTrace();
					try {
						StackTraceElement ste = e.getStackTrace()[1];
						String source = String.format("%s.%s()@ln:%d", ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
						messageHandler.setError(String.format("E: %s [%s:%s]", source, e.getClass().getSimpleName(), e.getMessage()));
					} catch (Exception x) {
						messageHandler.setError(e.getMessage());
					}
				} finally {
					form.getButton().setEnabled(true);
				}
			}
		};
		renderThread.start();
	}

	static void updateLayerList() {
		Vector<JCheckBox> chks = new Vector<JCheckBox>();
		JCheckBox chk;
		for (int i = 0; i < layers.size(); i++) {
			chk = new JCheckBox(layers.get(i).getName());
			if (i >= layerList.getModel().getSize()) {
				chk.setSelected(true);
			} else {
				chk.setSelected(layerList.isSelected(i));
			}
			chks.add(chk);
		}
		layerList.setListData(chks);
	}

	static void drawLayers() {
		// step through each layer and draw one on top of the other. index 0 = bottom layer
		if (layers == null || layers.size() == 0) return;
		BufferedImage img = new BufferedImage(layers.get(0).getWidth(), layers.get(0).getHeight(), layers.get(0).getType());
		Graphics g = img.getGraphics();
		for (int i = 0; i < layers.size(); i++) {
			if (layerList.isSelected(i)) {
				g.drawImage(layers.get(i), 0, 0, null);
			}
		}
		renderPanel.setImage(img);
	}

	static Renderer getRenderer() {
		switch (form.getRendererSelector().getSelectedIndex()) {
			case 0:
			default:
				return new LevelRenderer(messageHandler);
		}
	}

	static void reloadConfig() {
		form.getConfig().setText(convertStreamToString(ClassLoader.getSystemResourceAsStream("default.properties")));
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static Properties parsePropertiesString(String s) {
		// grr at load() returning void rather than the Properties object
		// so this takes 3 lines instead of "return new Properties().load(...);"
		final Properties p = new Properties();
		try {
			p.load(new StringReader(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static String convertMillis(long millis) {
		return String.format("%02d:%02d.%04d",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
				millis - TimeUnit.SECONDS.toMillis(TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
	}
}
