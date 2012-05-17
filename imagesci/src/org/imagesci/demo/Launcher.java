/**
 *       Java Image Science Toolkit
 *                  --- 
 *     Multi-Object Image Segmentation
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
package org.imagesci.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

// TODO: Auto-generated Javadoc
/**
 * The Class Launcher.
 */
public class Launcher extends JFrame implements TreeSelectionListener,
		ActionListener, MouseListener {

	/**
	 * The Class Example Info.
	 */
	protected class ExampleInfo {

		/** The example. */
		public AbstractExample example;

		/** The example url. */
		public URL exampleURL;

		/** The name. */
		public String name;

		/**
		 * Instantiates a new example info.
		 * 
		 * @param name
		 *            the name
		 * @param filename
		 *            the filename
		 * @param example
		 *            the example
		 */
		public ExampleInfo(String name, URL filename, AbstractExample example) {
			exampleURL = filename;
			this.example = example;
			this.name = name;
			if (exampleURL == null) {
				System.err.println("Couldn't find file: " + filename);
			}
		}

		/**
		 * Gets the example.
		 * 
		 * @return the example
		 */
		public AbstractExample getExample() {
			return example;
		}

		/**
		 * Gets the uRL.
		 * 
		 * @return the uRL
		 */
		public URL getURL() {
			return exampleURL;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

	}

	/** The data directory. */
	protected static File dataDirectory;

	/** The documentation directory. */
	protected static File documentationDirectory;

	/** The start url. */
	protected static URL startURL;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		File workingDirectory = new File("./");
		if (args.length > 0) {
			workingDirectory = new File(args[0]);
		}
		new Launcher(new File(workingDirectory, "docs/"), new File(
				workingDirectory, "data/"));
	}

	/** The examples. */
	protected AbstractExample examples[] = new AbstractExample[] {
			new Example0a_image2d(), new Example0b_image3d(),
			new Example0c_image4d(), new Example0d_mesh2springls(),
			new Example0e_mesh2muscle(), new Example1a_gac2d(),
			new Example1b_gac2d(), new Example1c_gac3d(),
			new Example1d_gac3d(), new Example2a_mogac2d(),
			new Example2b_mogac2d(), new Example2c_wemogac2d(),
			new Example2d_wemogac2d(), new Example2e_mogac3d(),
			new Example2f_mogac3d(), new Example2g_wemogac3d(),
			new Example2h_wemogac3d(), new Example2i_macwe2d(),
			new Example2j_macwe3d(), new Example3a_springls2d(),
			new Example3b_springls2d(), new Example3c_springls3d(),
			new Example3d_springls3d(), new Example3e_springls3d(),
			new Example3f_enright128(), new Example3g_enright256(),
			new Example4a_muscle2d(), new Example4b_muscle3d(),
			new Example4c_muscle3d(), new Example4d_muscle_acwe3d(),
			new Example4e_muscle_deform() };

	/** The html pane. */
	protected JEditorPane htmlPane;

	/** The launch button. */
	protected JButton launchButton;

	/** The tree. */
	protected JTree tree;

	/**
	 * Instantiates a new launcher.
	 * 
	 * @param docDir
	 *            the doc dir
	 * @param dataDir
	 *            the data dir
	 */
	public Launcher(File docDir, File dataDir) {
		documentationDirectory = docDir;
		dataDirectory = dataDir;
		try {
			startURL = (new File(documentationDirectory, "start.html")).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createWindow();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		if (node == null) {
			return;
		}

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			ExampleInfo ex = (ExampleInfo) nodeInfo;
			this.dispose();
			AbstractExample aex = ex.getExample();
			System.out.println("Launching example, please wait ...");
			aex.setWorkingDirectory(dataDirectory);
			Thread th = new Thread(aex);
			th.start();
		}
	}

	/**
	 * Creates the info.
	 * 
	 * @param ex
	 *            the ex
	 * @return the example info
	 */
	private ExampleInfo createInfo(AbstractExample ex) {
		String name = (ex.getName() != null) ? ex.getName() : ex.getClass()
				.getSimpleName().split("_")[0];
		return new ExampleInfo(name, createTmpFile(ex), ex);
	}

	/**
	 * Creates the nodes.
	 * 
	 * @param top
	 *            the top
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		for (AbstractExample ex : examples) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					createInfo(ex));
			top.add(node);
		}
	}

	/**
	 * Creates the tmp file.
	 * 
	 * @param ex
	 *            the ex
	 * @return the uRL
	 */
	private URL createTmpFile(AbstractExample ex) {
		File f = new File(documentationDirectory, ex.getClass().getSimpleName()
				+ ".html");
		try {
			String shortname = ex.getClass().getSimpleName().split("_")[0];
			BufferedWriter outStream = new BufferedWriter(new FileWriter(f));
			outStream.write("<HTML><BODY><CENTER><H1>" + ex.getName()
					+ "</H1><img src='" + shortname
					+ ".jpg'></CENTER><BR><B>Description: </B>"
					+ ex.getDescription() + "</BODY></HTML>");
			outStream.close();
			return f.toURL();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Creates the window.
	 */
	protected void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}
		setTitle("ImageSci Demo Launcher");
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				"ImageSci Examples");
		createNodes(top);
		// Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(this);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(new ImageIcon(getClass().getResource("dot.gif")));
		renderer.setIconTextGap(4);
		tree.setCellRenderer(renderer);
		tree.addMouseListener(this);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JScrollPane treeView = new JScrollPane(tree);

		splitPane.setDividerLocation(320);

		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(htmlView, BorderLayout.CENTER);
		pane.add(launchButton = new JButton("Launch Example"),
				BorderLayout.SOUTH);
		Font newButtonFont = new Font("Tahoma", launchButton.getFont()
				.getStyle(), 20);
		launchButton.setPreferredSize(new Dimension(200, 50));
		launchButton.setFont(newButtonFont);
		launchButton.setBorder(BorderFactory.createLineBorder(
				Color.GREEN.darker(), 3));
		launchButton.setEnabled(false);
		launchButton.addActionListener(this);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(pane);

		setPreferredSize(new Dimension(800, 600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - 800) / 2, 100);
		pack();

		setVisible(true);
		add(splitPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			htmlPane.setPage(startURL);
		} catch (IOException e1) {
			htmlPane.setText("Could not display page " + startURL + ".");
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent evt) {
		if (SwingUtilities.isLeftMouseButton(evt)) {
			if ((evt.getClickCount() == 2) && (tree.getSelectionPath() != null)) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					ExampleInfo ex = (ExampleInfo) nodeInfo;
					this.dispose();
					AbstractExample aex = ex.getExample();
					System.out.println("Launching example, please wait ...");
					aex.setWorkingDirectory(dataDirectory);
					Thread th = new Thread(aex);
					th.start();
				}
			}
		}
	}

	/**
	 * Required by TreeSelectionListener interface.
	 * 
	 * @param e
	 *            the e
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		if (node == null) {
			launchButton.setEnabled(false);
			return;
		}

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			launchButton.setEnabled(true);
			ExampleInfo ex = (ExampleInfo) nodeInfo;
			URL url = ex.getURL();
			if (url != null) {
				try {
					htmlPane.setPage(url);
				} catch (IOException e1) {
					htmlPane.setText("Could not display page " + url + ".");
				}
			} else {
				htmlPane.setText("Could not display page.");
			}
		} else {
			launchButton.setEnabled(false);
			try {
				htmlPane.setPage(startURL);
			} catch (IOException e1) {
				htmlPane.setText("Could not display page " + startURL + ".");
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}
