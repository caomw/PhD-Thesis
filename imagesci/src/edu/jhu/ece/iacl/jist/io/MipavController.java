/**
 * Java Image Science Toolkit (JIST)
 *
 * Image Analysis and Communications Laboratory &
 * Laboratory for Medical Image Computing &
 * The Johns Hopkins University
 * 
 * http://www.nitrc.org/projects/jist/
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.  The license is available for reading at:
 * http://www.gnu.org/copyleft/lgpl.html
 *
 */
package edu.jhu.ece.iacl.jist.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import edu.jhu.ece.iacl.jist.structures.image.ImageData;

// TODO: Auto-generated Javadoc
/**
 * Image Manipulation Utilities that require ViewUserInterface.
 * 
 * @author Blake Lucas
 */

public class MipavController {

	/** The registry. */
	// private static Vector<ModelImage> registry=new Vector<ModelImage>();

	/*
	 * private static ImageVariableTable imgTable=null;
	 * 
	 * public static void setImageVariableTable(ImageVariableTable imgTable){
	 * MipavController.imgTable=imgTable; }
	 * 
	 * public static ImageVariableTable getImageVariableTable(){ return
	 * imgTable; }
	 */
	/**
	 * Clear reigstry.
	 */
	/*
	 * public static void clearReigstry(){ for(ModelImage img:registry){ //
	 * img.disposeLocal(); MipavController.removeFromReigstry(img); }
	 * registry.clear(); }
	 */

	/****** Extracted from MedicUtil *****/
	/** The quiet. */
	protected static boolean quiet = false;

	/** Removes the from reigstry. */
	/*
	 * public static boolean removeFromReigstry(ModelImage img){
	 * if(registry.remove(img)){ //
	 * MipavViewUserInterface.getReference().unRegisterImage(img);
	 * img.disposeLocal(); return true; } else { return false; } }
	 */

	/**
	 * Creates the unique name.
	 * 
	 * @param img
	 *            the img
	 * 
	 * @return true, if successful
	 */
	/*
	 * protected static boolean createUniqueName(ModelImage img){ int
	 * instanceCount=0; String label=img.getImageName(); for(int
	 * i=0;i<registry.size();i++){ ModelImage p=registry.get(i);
	 * if(p!=img&&p.getImageName().equals(label)){ instanceCount++;
	 * label=img.getImageName()+""+(instanceCount+1); i=-1; } }
	 * if(instanceCount>0){ img.setImageNam ePrivate(label); return true; } else
	 * { return false; } }
	 */

	/** The relative path. */
	protected static File relativePath = null;

	/** Register image. */
	// public static /*synchronized*/ void registerImage(ModelImage img){
	/*
	 * if(quiet) { boolean contains=false; for(ModelImage im:registry){ if(
	 * FileReaderWriter.getFullFileName(im)!=null&&
	 * FileReaderWriter.getFullFileName(img)!=null&&
	 * FileReaderWriter.getFullFileName
	 * (im).equals(FileReaderWriter.getFullFileName(img))){ contains=true;
	 * break; } } createUniqueName(img); if(!contains)registry.add(img); } else
	 */
	// MipavViewUserInterface.getReference().registerImage(img);
	// }

	/** The true path. */
	protected static File truePath = null;

	/** The working directory. */
	protected static File workingDirectory = new File(System.getProperties()
			.getProperty("user.dir"));

	/**
	 * Display error.
	 * 
	 * @param message
	 *            the message
	 */
	public static void displayError(String message) {
		// GUI output
		// console output
		System.err.print(message);
		System.err.flush();
	}

	/**
	 * Get all registered MIPAV images.
	 *
	 * @param message the message
	 * @return list of registered images
	 */

	/*
	 * public static Vector<ModelImage> getImages(){
	 * 
	 * if(getUI()!=null){ Vector<ModelImage> imageList=new Vector<ModelImage>();
	 * Enumeration images=getUI().getRegisteredImages();
	 * 
	 * while (images.hasMoreElements()) { imageList.add((ModelImage)
	 * images.nextElement()); } return imageList; } else { throw new
	 * RuntimeException("MIPAV core not running?"); }
	 * 
	 * }
	 */

	/**
	 * Display message.
	 * 
	 * @param message
	 *            the message
	 */
	public static void displayMessage(String message) {
		// GUI output
		// console output
		System.out.print(message);
		System.out.flush();
	}

	/**
	 * Gets the default working directory.
	 * 
	 * @return the default working directory
	 */
	public static File getDefaultWorkingDirectory() {
		// if(isQuiet()){
		return workingDirectory;
		// } else {
		// String s=MipavViewUserInterface.getReference().getDefaultDirectory();
		// if(s!=null)return new File(s); else {
		// setDefaultWorkingDirectory(workingDirectory);
		// return workingDirectory;
		// }
		// }
	}

	/**
	 * Select an image from the registry by image name.
	 *
	 * @return image
	 */
	/*
	 * public static ModelImage getImageByName(String name){ if(getUI()!=null){
	 * try { return getUI().getRegisteredImageByName(name); }catch(Exception e){
	 * return null; } } else { throw new
	 * RuntimeException("MIPAV core not running"); } }
	 */
	/**
	 * Get the names of all registered MIPAV images.
	 * 
	 * @return list of image names
	 */

	public static Vector<String> getImageNames() {
		/*
		 * Vector<String> imageNameList=new Vector<String>(); if(getUI()!=null){
		 * Enumeration images=getUI().getRegisteredImages(); while
		 * (images.hasMoreElements()) { ModelImage img=(ModelImage)
		 * images.nextElement();
		 * if(img!=null)imageNameList.add(img.getImageName()); } } else { //
		 * for(ModelImage img:registry){ //
		 * imageNameList.add(img.getImageName()); // } throw new
		 * RuntimeException("MIPAV core not running?"); }
		 * //Collections.sort(imageNameList); return imageNameList;
		 */
		return null;
	}

	/**
	 * Inits the.
	 */
	public static void init() {
		/*
		 * if(isQuiet()){ MipavViewUserInterface.create(); // userInterface=;
		 * if(MipavViewUserInterface.getReference()==null){ throw new
		 * RuntimeException
		 * ("MipavController: Unable to initialize MIPAV object."); } }
		 */
	}

	/**
	 * Checks if is quiet.
	 * 
	 * @return true, if is quiet
	 */
	public static boolean isQuiet() {
		return quiet;
	}

	/**
	 * Create open file dialog to select either RAW or XML file types.
	 * 
	 * @param oldFile
	 *            the old file
	 * 
	 * @return selected image
	 */
	public static ArrayList<ImageData> openFileDialog(File oldFile) {
		JFileChooser openDialog = new JFileChooser();
		openDialog.setDialogTitle("Select Image File");
		openDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		openDialog.setSelectedFile(oldFile);

		openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		openDialog.setMultiSelectionEnabled(true);
		openDialog.setAcceptAllFileFilterUsed(false);
		openDialog.setFileFilter(NIFTIReaderWriter.getInstance()
				.getExtensionFilter());
		int returnVal = openDialog.showOpenDialog(new JPanel());
		File files[] = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			files = openDialog.getSelectedFiles();
			ArrayList<ImageData> imgs = new ArrayList<ImageData>();
			for (int i = 0; i < files.length; i++) {
				ImageData vol = NIFTIReaderWriter.getInstance().read(
						files[i]);
				imgs.add(vol);
			}

			return imgs;
		} else {
			return null;
		}
	}

	/**
	 * Sets the default working directory.
	 * 
	 * @param f
	 *            the new default working directory
	 */
	public static void setDefaultWorkingDirectory(File f) {
		// if(isQuiet()){
		workingDirectory = f;
		// } else {
		// if(f!=null){
		// MipavViewUserInterface.getReference().setDefaultDirectory(f.toString());
		// }
		// }
	}

	/**
	 * Sets the quiet.
	 * 
	 * @param q
	 *            the new quiet
	 */
	public static void setQuiet(boolean q) {
		quiet = q;
	}

	/**
	 * Sets the relative path.
	 * 
	 * @param relativeP
	 *            the new relative path
	 */
	public static void setRelativePath(File relativeP) {
		if (!relativePath.exists()) {
			return;
		}
		relativePath = relativeP;
		truePath = new File(System.getProperty("user.dir"));
	}

	/**
	 * Sets the relative path.
	 * 
	 * @param relativeP
	 *            the relative p
	 * @param trueP
	 *            the true p
	 */
	public static void setRelativePath(File relativeP, File trueP) {
		relativePath = relativeP;
		truePath = trueP;
	}

	/**
	 * Translate path.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the file
	 */
	public static File translatePath(File f) {
		if (f == null) {
			return null;
		}
		if (relativePath == null) {
			return f;
		}
		String orig = f.getAbsolutePath();
		String rel = relativePath.getAbsolutePath() + File.separator;
		String tru = truePath.getAbsolutePath() + File.separator;
		File newPath = f;
		if (orig.length() > rel.length()) {
			if (orig.substring(0, rel.length()).equalsIgnoreCase(rel)) {
				newPath = new File(tru, orig.substring(rel.length(),
						orig.length()));
				System.out.println("jist.io" + "\t" + "Translated Path:" + orig
						+ "->" + newPath);
			}
		}

		return newPath;
	}

}