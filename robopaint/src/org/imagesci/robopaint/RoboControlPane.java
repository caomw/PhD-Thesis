package org.imagesci.robopaint;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

public class RoboControlPane {
	public RoboControlPane(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		Display display = parent.getDisplay();
		Image image = display.getSystemImage(SWT.ICON_QUESTION);

		// Image item
		Composite composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Filename.img");
		label = new Label(composite, SWT.NONE);
		label.setText("Row");
		Scale scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText("Col");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText("Slice");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText("Contrast");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText("Brightness");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText("Transparency");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		
		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("Image");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);
		item0.setImage(image);

		// Geometry item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		Button button = new Button(composite, SWT.NONE);
		button.setText("Show Iso-surface");
		// Place label and color drop-down menu here.
		
		
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("Geometry");
		item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite);
		item1.setImage(image);
	}
}
