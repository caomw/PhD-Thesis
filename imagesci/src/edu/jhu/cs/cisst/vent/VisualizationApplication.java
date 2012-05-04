/**
 * ImageSci Toolkit
 *
 * Center for Computer-Integrated Surgical Systems and Technology &
 * Johns Hopkins Applied Physics Laboratory &
 * The Johns Hopkins University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 * @author Blake Lucas (blake@cs.jhu.edu)
 */
package edu.jhu.cs.cisst.vent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import processing.core.PImage;
import edu.jhu.cs.cisst.vent.resources.PlaceHolder;
import edu.jhu.cs.cisst.vent.video.GenericMovieMaker;
import edu.jhu.cs.cisst.vent.video.ProcessingMovieMaker;
import edu.jhu.ece.iacl.jist.io.FileExtensionFilter;
import edu.jhu.ece.iacl.jist.io.FileReaderWriter;
import edu.jhu.ece.iacl.jist.pipeline.factory.ParamFactory;
import edu.jhu.ece.iacl.jist.pipeline.parameter.ParamCollection;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

// TODO: Auto-generated Javadoc
/**
 * The Class VisualizationApplication creates a window to display the
 * visualization.
 */
public class VisualizationApplication extends JFrame implements ActionListener {
	/** The cache file. */
	protected File cacheFile;

	/** The movie maker. */
	protected GenericMovieMaker movieMaker;

	/** The step. */
	protected Action play, stop, pause, step;

	/** The play listeners. */
	protected List<PlayPauseStopEventListener> playListeners = new LinkedList<PlayPauseStopEventListener>();

	/** The show pause button. */
	protected boolean showPauseButton = false;

	/** The show tool bar. */
	protected boolean showToolBar = false;
	/** The visual. */
	protected Visualization visual;
	/** The visualization parameters. */
	protected ParamCollection visualizationParameters;

	/**
	 * Instantiates a new visualization application.
	 * 
	 * @param cacheFile
	 *            the cache file
	 * @param visual
	 *            the visual
	 */
	public VisualizationApplication(File cacheFile, Visualization visual) {
		this.visual = visual;
		this.cacheFile = cacheFile;
		this.movieMaker = new ProcessingMovieMaker();
	}

	/**
	 * Instantiates a new visualization application.
	 * 
	 * @param visual
	 *            the visual
	 */
	public VisualizationApplication(Visualization visual) {
		this.visual = visual;
		this.movieMaker = new ProcessingMovieMaker();
	}

	/**
	 * Action performed.
	 * 
	 * @param e
	 *            the e
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Exit")) {
			this.dispose();
		} else if (e.getActionCommand().equals("Save Screenshot")) {
			File f = selectImageToSave(this);
			if (f != null) {
				PImage img = new PImage(visual.getScreenshot());
				img.parent = ((VisualizationProcessing) visual);
				(img).save(f.getAbsolutePath());
			}
		} else if (e.getActionCommand().equals("Load Cache File")) {
			File tmp = selectCacheToLoad(this);
			// Import cached visualization parameters if they exist
			if (tmp != null) {
				ParamCollection params = ((ParamCollection) ParamFactory
						.fromXML(tmp));
				if (params != null) {
					visualizationParameters.importParameter(params);
					visualizationParameters.getInputView().update();
					visual.updateVisualizationParameters();
				}
			}
		} else if (e.getActionCommand().equals("Save Cache File")) {
			File tmp = selectCacheToSave(this);
			if (tmp != null) {
				visualizationParameters.write(tmp);
			}
		}
	}

	/**
	 * Select screenshot to save.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the file
	 */

	private static File selectImageToSave(Component parent) {
		JFileChooser loadDialog = new JFileChooser("Save Screenshot");
		loadDialog.setDialogType(JFileChooser.SAVE_DIALOG);
		loadDialog
				.setFileFilter(new FileExtensionFilter(new String[] { "png" }));
		loadDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = loadDialog.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = loadDialog.getSelectedFile();
			FileReaderWriter.getFileName(f);
			f = new File(f.getParent(), FileReaderWriter.getFileName(f)
					+ ".png");
			return f;
		} else {
			return null;
		}
	}

	/**
	 * Select cache file to load.
	 * 
	 * @param parent
	 *            the parent
	 * @return the file
	 */

	private File selectCacheToLoad(Component parent) {
		JFileChooser loadDialog = new JFileChooser("Load Cache");
		loadDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		loadDialog.setFileFilter(new FileExtensionFilter(
				new String[] { "cache" }));
		loadDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		loadDialog.setSelectedFile(cacheFile);
		int returnVal = loadDialog.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = loadDialog.getSelectedFile();
			FileReaderWriter.getFileName(f);
			f = new File(f.getParent(), FileReaderWriter.getFileName(f)
					+ ".cache");
			return f;
		} else {
			return null;
		}
	}

	/**
	 * Select cache file to save.
	 * 
	 * @param parent
	 *            the parent
	 * @return the file
	 */

	private File selectCacheToSave(Component parent) {
		JFileChooser loadDialog = new JFileChooser("Save Cache");
		loadDialog.setDialogType(JFileChooser.SAVE_DIALOG);
		loadDialog.setFileFilter(new FileExtensionFilter(
				new String[] { "cache" }));
		loadDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		loadDialog.setSelectedFile(cacheFile);
		int returnVal = loadDialog.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = loadDialog.getSelectedFile();
			FileReaderWriter.getFileName(f);
			f = new File(f.getParent(), FileReaderWriter.getFileName(f)
					+ ".cache");
			return f;
		} else {
			return null;
		}
	}

	/**
	 * Capture.
	 * 
	 * @param screenshotFile
	 *            the screenshot file
	 */
	public void capture(File screenshotFile) {
		capture(screenshotFile, cacheFile);
	}

	/**
	 * Capture.
	 * 
	 * @param screenshotFile
	 *            the screenshot file
	 * @param tmp
	 *            the tmp
	 * @return the p image
	 */
	public PImage capture(File screenshotFile, File tmp) {
		visualizationParameters = visual.create();
		this.cacheFile = tmp;
		if (cacheFile != null) {
			// Import cached visualization parameters if they exist
			ParamCollection params = ((ParamCollection) ParamFactory
					.fromXML(cacheFile));
			if (params != null) {
				visualizationParameters.importParameter(params);
			}
		}
		setTitle(visual.getName());
		ParamInputView inputView = visualizationParameters.getInputView();
		visual.updateVisualizationParameters();
		inputView.addObserver(visual);
		inputView.update();
		JPanel smallPane = new JPanel();
		smallPane.add(visual.getComponent());
		getContentPane().add(smallPane);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (cacheFile != null && visualizationParameters != null) {
					visualizationParameters.write(cacheFile);
				}
				if (visual != null) {
					visual.dispose();
				}
			}
		});

		((VisualizationProcessing) visual).init();
		pack();
		setVisible(false);
		PImage screenshot = new PImage(visual.getScreenshot());

		if (visual instanceof VisualizationProcessing) {
			screenshot.parent = (VisualizationProcessing) visual;
		}
		/*
		 * try { Thread.sleep(5000); } catch (InterruptedException e1) { // TODO
		 * Auto-generated catch block e1.printStackTrace(); }
		 */
		// screenshot.resize(1024, 768);\
		if (screenshotFile != null) {
			System.out
					.println("SCREENSHOT " + screenshotFile.getAbsolutePath());
			screenshot.save(screenshotFile.getAbsolutePath());
		}
		// visual.dispose();
		setVisible(false);
		return screenshot;
	}

	/**
	 * Run and wait.
	 */
	public void runAndWait() {
		runAndWait(false);
	}

	/**
	 * Run and wait.
	 * 
	 * @param autoplay
	 *            the autoplay
	 */
	public void runAndWait(boolean autoplay) {

		execute();
		if (autoplay) {
			pressPlay();
		}
		while (this.isVisible()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Execute.
	 */
	public void execute() {
		visualizationParameters = visual.create();
		if (cacheFile != null) {
			// Import cached visualization parameters if they exist
			ParamCollection params = ((ParamCollection) ParamFactory
					.fromXML(cacheFile));
			if (params != null) {
				visualizationParameters.importParameter(params);
			}
		}
		this.init();
		setVisible(true);
	}

	/**
	 * Press play.
	 */
	public void pressPlay() {
		play.setEnabled(false);
		stop.setEnabled(true);
		step.setEnabled(false);
		for (PlayPauseStopEventListener listener : playListeners) {
			listener.playEvent();
		}
	}

	/**
	 * Adds the listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(PlayPauseStopEventListener listener) {
		playListeners.add(listener);
	}

	/**
	 * Load cache.
	 * 
	 * @param tmp
	 *            the tmp
	 */
	public void loadCache(File tmp) {
		ParamCollection params = ((ParamCollection) ParamFactory.fromXML(tmp));
		if (params != null) {
			visualizationParameters.importParameter(params);
			visualizationParameters.getInputView().update();
			visual.updateVisualizationParameters();
		}
	}

	/**
	 * Sets the movie maker.
	 * 
	 * @param movieMaker
	 *            the new movie maker
	 */
	public void setMovieMaker(GenericMovieMaker movieMaker) {
		this.movieMaker = movieMaker;
	}

	/**
	 * Sets the show pause button.
	 * 
	 * @param showPauseButton
	 *            the new show pause button
	 */
	public void setShowPauseButton(boolean showPauseButton) {
		this.showPauseButton = showPauseButton;
	}

	/**
	 * Sets the show tool bar.
	 * 
	 * @param showToolBar
	 *            the new show tool bar
	 */
	public void setShowToolBar(boolean showToolBar) {
		this.showToolBar = showToolBar;
	}

	/**
	 * Inits the.
	 */
	protected void init() {
		setTitle(visual.getName());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setPreferredSize(new Dimension(300, 40));
		URL url = PlaceHolder.class.getResource("stop.gif");
		ImageIcon stopIcon = new ImageIcon(url, "Stop");
		stop = new AbstractAction("Stop", stopIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop.setEnabled(false);
				play.setEnabled(true);
				step.setEnabled(true);
				for (PlayPauseStopEventListener listener : playListeners) {
					listener.stopEvent();
				}
			}
		};
		stop.putValue(Action.SHORT_DESCRIPTION, "Stop");
		stop.setEnabled(false);

		url = PlaceHolder.class.getResource("pause.gif");
		ImageIcon pauseIcon = new ImageIcon(url, "Pause");
		pause = new AbstractAction("Pause", pauseIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				step.setEnabled(!play.isEnabled());
				play.setEnabled(!play.isEnabled());
				for (PlayPauseStopEventListener listener : playListeners) {
					listener.pauseEvent();
				}
			}
		};
		pause.putValue(Action.SHORT_DESCRIPTION, "pause");
		pause.setEnabled(true);

		url = PlaceHolder.class.getResource("play.gif");
		ImageIcon playIcon = new ImageIcon(url, "Play");
		playIcon.setDescription("Play");

		play = new AbstractAction("Play", playIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				play.setEnabled(false);
				stop.setEnabled(true);
				step.setEnabled(false);
				for (PlayPauseStopEventListener listener : playListeners) {
					listener.playEvent();
				}
			}
		};
		play.putValue(Action.SHORT_DESCRIPTION, "play");
		play.setEnabled(true);

		url = PlaceHolder.class.getResource("step.gif");
		ImageIcon stepIcon = new ImageIcon(url, "Step");
		stepIcon.setDescription("Play");
		step = new AbstractAction("Step", stepIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (PlayPauseStopEventListener listener : playListeners) {

					listener.stepEvent();
				}
			}
		};
		step.putValue(Action.SHORT_DESCRIPTION, "step");
		step.setEnabled(true);

		toolbar.add(play);
		toolbar.add(step);
		if (showPauseButton) {
			toolbar.add(pause);
		}
		toolbar.add(stop);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		ParamInputView inputView = visualizationParameters.getInputView();
		visual.updateVisualizationParameters();
		inputView.addObserver(visual);
		inputView.update();
		splitPane.setLeftComponent(inputView);
		splitPane.setDividerLocation(350);
		// setPreferredSize(new Dimension(1600, 1200));
		JPanel smallPane = new JPanel();
		smallPane.add(visual.getComponent());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(smallPane);
		splitPane.setRightComponent(scrollPane);
		splitPane.setOneTouchExpandable(true);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);
		if (showToolBar) {
			getContentPane().add(toolbar, BorderLayout.NORTH);
		}
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setJMenuBar(createMenuBar());
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (cacheFile != null && visualizationParameters != null) {
					visualizationParameters.write(cacheFile);
				}
				// if (visual != null)visual.dispose();
			}
		});
		pack();
	}

	/**
	 * Creates the menu bar.
	 * 
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenuItem menuItem;
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);
		menuItem = new JMenuItem("Save Screenshot");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuFile.add(menuItem);
		menuItem.addActionListener(this);

		menuItem = new JMenuItem("Save Cache File");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuFile.add(menuItem);
		menuItem.addActionListener(this);

		menuItem = new JMenuItem("Load Cache File");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_L);
		menuFile.add(menuItem);

		menuItem.addActionListener(this);

		menuItem = new JMenuItem("Exit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic(KeyEvent.VK_X);
		menuFile.add(menuItem);
		menuItem.addActionListener(this);
		menuFile.add(menuItem);
		menuBar.add(menuFile);
		return menuBar;
	}

	/**
	 * Save video.
	 * 
	 * @param f
	 *            the f
	 * @param images
	 *            the images
	 * @param frameRate
	 *            the frame rate
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	protected void saveVideo(File f, Image[] images, int frameRate, int width,
			int height) {
		if (images == null) {
			return;
		}
		movieMaker.save(f, images, frameRate, width, height);
	}

	/**
	 * Select video to save.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the file
	 */

	private static File selectVideoToSave(Component parent) {
		JFileChooser loadDialog = new JFileChooser("Save Video");
		loadDialog.setDialogType(JFileChooser.SAVE_DIALOG);
		loadDialog.setFileFilter(new FileExtensionFilter(new String[] { "mov",
				"avi" }));
		loadDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = loadDialog.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = loadDialog.getSelectedFile();
			FileReaderWriter.getFileName(f);
			String ext = FileReaderWriter.getFileExtension(f);
			if (!ext.equalsIgnoreCase("mov") && !ext.equalsIgnoreCase("avi")) {
				f = new File(f.getParent(), FileReaderWriter.getFileName(f)
						+ ".mov");
			}
			return f;
		} else {
			return null;
		}
	}
}
