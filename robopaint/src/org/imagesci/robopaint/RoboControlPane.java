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

		// First item
		Composite composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		Button button = new Button(composite, SWT.PUSH);
		button.setText("SWT.PUSH");
		button = new Button(composite, SWT.RADIO);
		button.setText("SWT.RADIO");
		button = new Button(composite, SWT.CHECK);
		button.setText("SWT.CHECK");
		button = new Button(composite, SWT.TOGGLE);
		button.setText("SWT.TOGGLE");
		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("What is your favorite button");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);
		item0.setImage(image);

		// Second item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_ERROR));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_ERROR");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_INFORMATION));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_INFORMATION");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_WARNING));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_WARNING");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_QUESTION));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_QUESTION");
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("What is your favorite icon");
		item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite);
		item1.setImage(image);

		// Third item
		composite = new Composite(bar, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		label = new Label(composite, SWT.NONE);
		label.setText("Scale");
		new Scale(composite, SWT.NONE);
		label = new Label(composite, SWT.NONE);
		label.setText("Spinner");
		new Spinner(composite, SWT.BORDER);
		label = new Label(composite, SWT.NONE);
		label.setText("Slider");
		new Slider(composite, SWT.NONE);
		ExpandItem item2 = new ExpandItem(bar, SWT.NONE, 2);
		item2.setText("What is your favorite range widget");
		item2.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item2.setControl(composite);
		item2.setImage(image);

		item1.setExpanded(true);
		bar.setSpacing(8);
	}
}
