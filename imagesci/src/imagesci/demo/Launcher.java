package imagesci.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import edu.jhu.cs.cisst.vent.PlayPauseStopEventListener;
import edu.jhu.cs.cisst.vent.resources.PlaceHolder;
import edu.jhu.ece.iacl.jist.pipeline.view.input.ParamInputView;

public class Launcher extends JFrame implements TreeSelectionListener {
	protected JTree tree;
	protected JEditorPane htmlPane;
	protected JButton launchButton;
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
			new Example4d_muscle_deform() };

	public static void main(String[] args) {
		Launcher launcher = new Launcher();
	}

	public Launcher() {
		createWindow();
	}

	private ExampleInfo createInfo(AbstractExample ex) {
		String name = (ex.getName() != null) ? ex.getName() : ex.getClass()
				.getSimpleName().split("_")[0];
		return new ExampleInfo(name, ex.getClass().getSimpleName());
	}

	private void createNodes(DefaultMutableTreeNode top) {
		for (AbstractExample ex : examples) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					createInfo(ex));
			top.add(node);
		}
	}

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
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JScrollPane treeView = new JScrollPane(tree);
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.add(treeView, BorderLayout.CENTER);
		pane.add(launchButton = new JButton("Launch"), BorderLayout.SOUTH);
		launchButton.setPreferredSize(new Dimension(200, 50));
		launchButton.setBorder(BorderFactory.createLineBorder(
				Color.GREEN.darker(), 3));
		splitPane.setDividerLocation(200);

		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		splitPane.setLeftComponent(pane);
		splitPane.setRightComponent(htmlView);

		setPreferredSize(new Dimension(800, 600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - 800) / 2, 100);
		pack();

		setVisible(true);
		add(splitPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	private class ExampleInfo {
		public String name;
		public URL exampleURL;

		public ExampleInfo(String book, String filename) {
			name = book;
			exampleURL = getClass().getResource(filename);
			if (exampleURL == null) {
				System.err.println("Couldn't find file: " + filename);
			}
		}

		public String toString() {
			return name;
		}
	}
}
