package org.imagesci.robopaint;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

public class RoboControlPane {
	public RoboControlPane(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		
		// Image item.
		Composite composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		Text text = new Text(composite, SWT.NONE);
		text.setText("Filename");
		text = new Text(composite, SWT.NONE);
		text.setText("row");
		Scale scale = new Scale(composite, SWT.BORDER); // "row"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		text = new Text(composite, SWT.NONE);
		text.setText("col");
		scale = new Scale(composite, SWT.BORDER); // "col"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		text = new Text(composite, SWT.NONE);
		text.setText("slice");
		scale = new Scale(composite, SWT.BORDER); // "slice"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		text = new Text(composite, SWT.NONE);
		text.setText("contrast");
		scale = new Scale(composite, SWT.BORDER); // "contrast"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		text = new Text(composite, SWT.NONE);
		text.setText("brightness");
		scale = new Scale(composite, SWT.BORDER); // "brightness"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		text = new Text(composite, SWT.NONE);
		text.setText("transparency");
		scale = new Scale(composite, SWT.BORDER); // "transparency"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		
		// Geometry item.
		composite = new Composite(bar, SWT.NONE);
		composite.setLayout(layout);
		Button button = new Button(composite, SWT.TOGGLE);
		button.setText("Show Iso-Surface");
		// Place label id and color dropdown here.
		text = new Text(composite, SWT.NONE);
		text.setText("transparency");
		scale = new Scale(composite, SWT.BORDER); // "transparency"
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(5);
		button = new Button(composite, SWT.TOGGLE);
		button.setText("Visible");
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 0);
		item1.setText("Geometry");
		item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite);
	}
}
