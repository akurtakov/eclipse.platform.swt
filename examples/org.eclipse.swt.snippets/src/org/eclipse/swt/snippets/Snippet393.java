/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * Shell example snippet: demonstrate that a dialog opened while a splash screen
 * (SWT.ON_TOP shell) is visible appears on top of the splash, not behind it.
 *
 * On GTK4 the part of the dialog that overlaps the splash screen was not rendered
 * (appeared blank) because Shell.bringToTop() used gdk_toplevel_focus() which only
 * requests focus without raising the window.  The fix is to call gtk_window_present()
 * instead when force=true (i.e. called from Shell.open()).
 *
 * Expected behavior: after the splash appears, a "workspace chooser" dialog opens on
 * top of it (overlapping the splash).  The dialog must be fully visible and interactive.
 *
 * For a list of all SWT example snippets see
 * https://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet393 {

public static void main(String[] args) {
	final Display display = new Display();

	/* --- Splash screen (SWT.ON_TOP mimics Eclipse's splash) --- */
	final Shell splash = new Shell(SWT.ON_TOP);
	splash.setLayout(new FillLayout(SWT.VERTICAL));

	// Paint the splash with a distinctive cyan background so it is easy to spot.
	splash.addListener(SWT.Paint, e -> {
		Rectangle r = splash.getClientArea();
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		e.gc.fillRectangle(r);
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		e.gc.setFont(display.getSystemFont());
		e.gc.drawText("Splash Screen (SWT.ON_TOP)", 20, r.height / 2 - 10, true);
	});

	splash.setSize(500, 300);
	Rectangle displayRect = display.getBounds();
	splash.setLocation(
		(displayRect.width - 500) / 2,
		(displayRect.height - 300) / 2);
	splash.open();

	/*
	 * Simulate the Eclipse startup sequence: while the splash is still showing,
	 * open a "workspace chooser" dialog that overlaps the splash.
	 *
	 * On GTK4 (before the fix) the area of the dialog covered by the splash was
	 * not rendered – it appeared blank or showed through to the splash content.
	 */
	display.timerExec(1500, () -> {
		if (splash.isDisposed()) return;

		Shell dialog = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText("Select Workspace");
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 15;
		layout.marginHeight = 15;
		layout.verticalSpacing = 10;
		dialog.setLayout(layout);

		Label infoLabel = new Label(dialog, SWT.WRAP);
		infoLabel.setText(
			"This dialog simulates the Eclipse workspace chooser.\n\n"
			+ "It should be fully visible and interactive even though\n"
			+ "it overlaps the cyan SWT.ON_TOP splash screen behind it.\n\n"
			+ "On GTK4 (before the fix) the overlapping region appeared\n"
			+ "blank / unrendered.");
		GridData labelData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		labelData.widthHint = 380;
		infoLabel.setLayoutData(labelData);

		Label pathLabel = new Label(dialog, SWT.NONE);
		pathLabel.setText("Workspace:");

		Text workspacePath = new Text(dialog, SWT.BORDER);
		workspacePath.setText("/home/user/workspace");
		workspacePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Spacer
		new Label(dialog, SWT.NONE);

		Composite buttons = new Composite(dialog, SWT.NONE);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		Button ok = new Button(buttons, SWT.PUSH);
		ok.setText("Launch");
		ok.addListener(SWT.Selection, e -> {
			splash.close();
			dialog.close();
		});
		dialog.setDefaultButton(ok);

		Button cancel = new Button(buttons, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, e -> {
			splash.close();
			dialog.close();
		});

		dialog.pack();

		// Position the dialog so it visibly overlaps the splash screen.
		Rectangle splashRect = splash.getBounds();
		Rectangle dialogRect = dialog.getBounds();
		dialog.setLocation(
			splashRect.x + (splashRect.width - dialogRect.width) / 2,
			splashRect.y + (splashRect.height - dialogRect.height) / 2);

		dialog.open();
	});

	while (!splash.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
	display.dispose();
}

}
