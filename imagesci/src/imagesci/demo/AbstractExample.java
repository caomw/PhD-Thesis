package imagesci.demo;

import java.io.File;
import java.net.URISyntaxException;

import data.PlaceHolder;

public abstract class AbstractExample {
	public static File defaultWorkingDirectory;
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

	public abstract String getDescription();

	public abstract String getName();

	public abstract void launch(File workingDirectory, String args[]);

	public void launch(String[] args) {
		launch(AbstractExample.defaultWorkingDirectory, args);
	}

}
