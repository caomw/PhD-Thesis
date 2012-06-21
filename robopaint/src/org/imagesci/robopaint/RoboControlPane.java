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

		// Image item
		Composite composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Filename.img");
		label = new Label(composite, SWT.NONE);
		label = new Label(composite, SWT.NONE);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Row");
		Scale scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Col");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Slice");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Contrast");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Brightness");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Transparency");
		scale = new Scale(composite, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("Image");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);

		// Geometry item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		label = new Label(composite, SWT.NONE);
		label.setText("Show Iso-surface");
		Button button = new Button(composite, SWT.CHECK);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Name");
		Combo combo = new Combo(composite, SWT.NONE);
		combo.add("Label [#1]");
		combo.add("Label [#2]");
		
		label = new Label(composite, SWT.NONE);
		label.setText("Color");
		Composite composite2 = new Composite(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		composite2.setLayout(layout);
		Color color = new Color(parent.getDisplay(), new RGB(0, 255, 0));
		label = new Label(composite2, SWT.BORDER);
		label.setText("     ");
		label.setBackground(color);
		button = new Button(composite2, SWT.PUSH);
		button.setText("Change color");
		
		label = new Label(composite, SWT.NONE);
		label.setText("Transparency");
		composite2 = new Composite(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		composite2.setLayout(layout);
		scale = new Scale(composite2, SWT.NONE);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		label = new Label(composite2, SWT.NONE);
		label.setText(Integer.toString(scale.getSelection()));
		label.setAlignment(SWT.RIGHT);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Visible");
		button = new Button(composite, SWT.CHECK);
		
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("Geometry");
		item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite);
		
		// Paint item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		ExpandItem item2 = new ExpandItem(bar, SWT.NONE, 2);
		item2.setText("Paint");
		item2.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item2.setControl(composite);
		
		// Auto-segment item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		ExpandItem item3 = new ExpandItem(bar, SWT.NONE, 3);
		item3.setText("Sculpt");
		item3.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item3.setControl(composite);
		
		// Sculpt item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		ExpandItem item4 = new ExpandItem(bar, SWT.NONE, 3);
		item4.setText("Auto-segment");
		item4.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item4.setControl(composite);
	}
}
