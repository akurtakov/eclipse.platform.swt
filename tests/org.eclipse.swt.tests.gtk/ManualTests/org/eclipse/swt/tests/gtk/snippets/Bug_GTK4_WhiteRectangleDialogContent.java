/*******************************************************************************
 * Copyright (c) 2025 Yatta Solutions and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Yatta Solutions - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;

/*
 * Regression test for: On GTK4/Wayland, decorated dialog shells (DIALOG_TRIM,
 * SHELL_TRIM) show a large white rectangle where the dialog content should be.
 * Only edge content (e.g. partial widgets at the left/right edge of the dialog)
 * is sometimes visible, and the center is white.
 *
 * This is caused by forcefully installing a GtkHeaderBar for decorated shells,
 * which switches from server-side decorations (SSD, used by GNOME Wayland) to
 * client-side decorations (CSD). The resulting coordinate-offset mismatches in
 * forceResize() and resizeBounds() prevent the dialog content from rendering.
 *
 * Steps to reproduce:
 *   1. Run on GTK4/Wayland (set SWT_GTK4=1)
 *   2. Click the "Open Dialog" button to open a modal dialog.
 *
 * Expected: All dialog content (colored composites, labels, buttons) is visible.
 * Actual (before fix): A large white rectangle covers the dialog's content area.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Bug_GTK4_WhiteRectangleDialogContent {

public static void main(String[] args) {
	Display display = new Display();
	Shell mainShell = new Shell(display, SWT.SHELL_TRIM);
	mainShell.setText("GTK4 Dialog White Rectangle Test");
	mainShell.setLayout(new FillLayout());

	Button openBtn = new Button(mainShell, SWT.PUSH);
	openBtn.setText("Open Dialog (must show content, not white rectangle)");
	openBtn.addListener(SWT.Selection, e -> openTestDialog(display, mainShell));

	mainShell.setSize(450, 150);
	mainShell.open();

	while (!mainShell.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
	display.dispose();
}

static void openTestDialog(Display display, Shell parent) {
	// Use APPLICATION_MODAL | DIALOG_TRIM – the typical style for Eclipse workspace
	// chooser, run configuration dialogs, etc. which showed the white rectangle.
	Shell dialog = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
	dialog.setText("Dialog Content Rendering Test");
	dialog.setLayout(new GridLayout(1, false));

	// Title area (simulates JFace TitleAreaDialog title bar)
	Composite titleArea = new Composite(dialog, SWT.NONE);
	titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	titleArea.setLayout(new GridLayout(2, false));
	Color titleBg = display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	titleArea.setBackground(titleBg);
	Label title = new Label(titleArea, SWT.NONE);
	title.setText("Select a directory as workspace");
	title.setBackground(titleBg);
	title.setFont(display.getSystemFont());
	GridData titleLabelGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
	title.setLayoutData(titleLabelGd);

	// Separator
	Label sep = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
	sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	// Main content area (simulates the workspace path input group)
	Composite content = new Composite(dialog, SWT.NONE);
	content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	content.setLayout(new GridLayout(2, false));

	Label workspaceLabel = new Label(content, SWT.NONE);
	workspaceLabel.setText("Workspace path:");
	workspaceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

	Text workspacePath = new Text(content, SWT.BORDER);
	workspacePath.setText("/home/user/workspace");
	workspacePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	// Colored composites to make rendering issues obvious
	Composite coloredArea = new Composite(content, SWT.NONE);
	GridData coloredGd = new GridData(SWT.FILL, SWT.FILL, true, true);
	coloredGd.horizontalSpan = 2;
	coloredGd.heightHint = 120;
	coloredArea.setLayoutData(coloredGd);
	coloredArea.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
	coloredArea.setLayout(new FillLayout());
	Label coloredLabel = new Label(coloredArea, SWT.CENTER | SWT.WRAP);
	coloredLabel.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
	coloredLabel.setText("PASS: This cyan area and all other content must be visible.\n"
		+ "FAIL: If you see a white rectangle instead of this text.");

	// Separator
	Label sep2 = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
	sep2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	// Button bar
	Composite buttonBar = new Composite(dialog, SWT.NONE);
	buttonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
	buttonBar.setLayout(new GridLayout(2, true));
	Button cancelBtn = new Button(buttonBar, SWT.PUSH);
	cancelBtn.setText("Cancel");
	cancelBtn.addListener(SWT.Selection, ev -> dialog.close());
	Button launchBtn = new Button(buttonBar, SWT.PUSH);
	launchBtn.setText("Launch");
	launchBtn.addListener(SWT.Selection, ev -> dialog.close());
	dialog.setDefaultButton(launchBtn);

	dialog.setSize(550, 400);
	dialog.setLocation(
		parent.getLocation().x + (parent.getSize().x - 550) / 2,
		parent.getLocation().y + (parent.getSize().y - 400) / 2);
	dialog.open();

	while (!dialog.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
}

}
