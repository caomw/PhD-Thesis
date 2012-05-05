package imagesci.demo;

import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;

public abstract class AbstractExample implements Runnable {
	public static File defaultWorkingDirectory;
	protected File workingDirectory;
	static {
		try {
			defaultWorkingDirectory = new File(PlaceHolder.class.getResource(
					"./").toURI());
		} catch (URISyntaxException e) {
			defaultWorkingDirectory = new File("./");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public abstract String getName();

	public void run() {
		this.launch((workingDirectory == null) ? defaultWorkingDirectory
				: workingDirectory, new String[] {});
	}

	public abstract String getDescription();

	public abstract void launch(File workingDirectory, String args[]);

	public void launch(String[] args) {
		launch(AbstractExample.defaultWorkingDirectory, args);
	}

}
