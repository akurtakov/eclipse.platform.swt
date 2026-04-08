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
 * Shell example snippet: reproduce Eclipse "workspace chooser dialog rendered
 * behind / partially blank while splash screen is visible" on GTK4.
 *
 * Two distinct GTK4 issues are demonstrated:
 *
 * Issue 1 – Z-order: the dialog appears BEHIND the SWT.ON_TOP splash shell.
 *   Fixed by calling gtk_window_present() (not gdk_toplevel_focus()) in
 *   Shell.bringToTop() when force=true.
 *
 * Issue 2 – Content rendering: even after the dialog is on top, its interior
 *   is partially blank.  The Combo widget shows only the first character, the
 *   recent-workspaces Table is empty-looking, and buttons are clipped.  This
 *   matches the screenshot reported against the GTK4 workspace-chooser.
 *
 * Expected behavior (after both fixes): the dialog opens fully on top of the
 * splash, the Combo shows the full path, the Table lists recent workspaces,
 * and the Launch / Cancel buttons are completely visible.
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

	/* --- Splash screen: SWT.ON_TOP shell that covers the screen center --- */
	final Shell splash = new Shell(SWT.ON_TOP);

	splash.addListener(SWT.Paint, e -> {
		Rectangle r = splash.getClientArea();
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		e.gc.fillRectangle(r);
		e.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
		e.gc.setFont(display.getSystemFont());
		e.gc.drawText("Splash Screen (SWT.ON_TOP)", 20, r.height / 2 - 10, true);
	});

	final int splashW = 600, splashH = 400;
	splash.setSize(splashW, splashH);
	Rectangle displayRect = display.getBounds();
	splash.setLocation(
		(displayRect.width  - splashW) / 2,
		(displayRect.height - splashH) / 2);
	splash.open();

	/*
	 * After a short delay (mimicking Eclipse startup), open the workspace-chooser
	 * dialog.  It is intentionally sized and structured to match the real Eclipse
	 * ChooseWorkspaceDialog so that both GTK4 rendering issues are observable:
	 *
	 *   - The dialog uses setSize() (not pack()) just as the real dialog does.
	 *   - A Combo is used for the workspace path (not a plain Text widget).
	 *   - A Table lists recent workspaces below the combo row.
	 *   - The dialog is centered over the splash so it overlaps it completely.
	 */
	display.timerExec(1500, () -> {
		if (splash.isDisposed()) return;

		Shell dialog = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText("Eclipse SDK Launcher");

		GridLayout dialogLayout = new GridLayout();
		dialogLayout.marginWidth  = 0;
		dialogLayout.marginHeight = 0;
		dialog.setLayout(dialogLayout);

		/* ---- header area ---- */
		Composite header = new Composite(dialog, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout headerLayout = new GridLayout();
		headerLayout.marginWidth  = 12;
		headerLayout.marginHeight = 8;
		header.setLayout(headerLayout);

		Label title = new Label(header, SWT.NONE);
		title.setText("Select a directory as workspace");
		FontData[] fd = title.getFont().getFontData();
		for (FontData f : fd) f.setStyle(SWT.BOLD);
		Font boldFont = new Font(display, fd);
		title.setFont(boldFont);
		title.addListener(SWT.Dispose, e -> boldFont.dispose());

		Label desc = new Label(header, SWT.WRAP);
		desc.setText(
			"Eclipse SDK uses the workspace directory to store its preferences " +
			"and development artifacts.");
		GridData descData = new GridData(SWT.FILL, SWT.TOP, true, false);
		descData.widthHint = 560;
		desc.setLayoutData(descData);

		/* ---- workspace path row (Combo + Browse button) ---- */
		Composite pathRow = new Composite(dialog, SWT.NONE);
		pathRow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout pathLayout = new GridLayout(2, false);
		pathLayout.marginWidth  = 12;
		pathLayout.marginHeight = 4;
		pathRow.setLayout(pathLayout);

		Combo workspaceCombo = new Combo(pathRow, SWT.DROP_DOWN | SWT.BORDER);
		workspaceCombo.add("Eclipse SDK workspace");
		workspaceCombo.add("/home/user/eclipse-workspace-2");
		workspaceCombo.add("/home/user/eclipse-workspace-3");
		workspaceCombo.select(0);
		workspaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button browse = new Button(pathRow, SWT.PUSH);
		browse.setText("Browse...");

		/* ---- recent workspaces group ---- */
		Group recentGroup = new Group(dialog, SWT.NONE);
		recentGroup.setText("Recent Workspaces");
		recentGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		recentGroup.setLayout(new FillLayout());

		Table recentTable = new Table(recentGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		recentTable.setHeaderVisible(false);
		recentTable.setLinesVisible(false);
		String[] recentPaths = {
			"Eclipse SDK workspace",
			"/home/user/eclipse-workspace-2",
			"/home/user/eclipse-workspace-3",
		};
		for (String path : recentPaths) {
			TableItem item = new TableItem(recentTable, SWT.NONE);
			item.setText(path);
		}

		/* ---- button bar ---- */
		Composite buttonBar = new Composite(dialog, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth  = 12;
		buttonLayout.marginHeight = 8;
		buttonBar.setLayout(buttonLayout);

		// fill spacer pushes buttons to the right
		Label spacer = new Label(buttonBar, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button launch = new Button(buttonBar, SWT.PUSH);
		launch.setText("Launch");
		launch.addListener(SWT.Selection, e -> {
			splash.close();
			dialog.close();
		});
		dialog.setDefaultButton(launch);

		Button cancel = new Button(buttonBar, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, e -> {
			splash.close();
			dialog.close();
		});

		/* Use setSize (not pack) to match the real dialog and expose the bug */
		dialog.setSize(700, 500);

		// Center over the splash screen so the overlap is maximal.
		Rectangle splashRect = splash.getBounds();
		Rectangle dialogRect = dialog.getBounds();
		dialog.setLocation(
			splashRect.x + (splashRect.width  - dialogRect.width)  / 2,
			splashRect.y + (splashRect.height - dialogRect.height) / 2);

		dialog.open();
	});

	while (!splash.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
	display.dispose();
}

}
