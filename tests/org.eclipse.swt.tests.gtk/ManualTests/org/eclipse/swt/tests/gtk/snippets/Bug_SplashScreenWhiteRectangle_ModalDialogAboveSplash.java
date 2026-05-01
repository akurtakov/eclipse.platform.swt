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
 * Regression test for: On GTK4, workspace chooser dialog shows a white rectangle
 * at the same position where the splash screen will appear.
 *
 * Steps to reproduce:
 *   1. Run on GTK4 (set SWT_GTK4=1)
 *   2. Observe: a white rectangle (the splash screen shell) appears at the center
 *      of the screen overlapping with the modal dialog content.
 *
 * Expected: The modal APPLICATION_MODAL dialog appears on top of the NO_TRIM
 *   splash screen shell. No white rectangle is visible inside the dialog.
 * Actual (before fix): On GTK4/Wayland, the NO_TRIM splash shell was on top of the
 *   dialog, showing as a white rectangle at the splash position.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Bug_SplashScreenWhiteRectangle_ModalDialogAboveSplash {

public static void main(String[] args) {
	Display display = new Display();

	// Simulate the splash screen: a NO_TRIM shell centered on screen
	Shell splashShell = new Shell(display, SWT.NO_TRIM);
	splashShell.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
	Rectangle displayBounds = display.getBounds();
	int splashWidth = 450;
	int splashHeight = 300;
	splashShell.setSize(splashWidth, splashHeight);
	splashShell.setLocation(
		displayBounds.x + (displayBounds.width - splashWidth) / 2,
		displayBounds.y + (displayBounds.height - splashHeight) / 2);
	Label splashLabel = new Label(splashShell, SWT.CENTER);
	splashLabel.setText("Splash Screen (should be BELOW the dialog)");
	splashLabel.setBounds(0, 0, splashWidth, splashHeight);
	splashShell.open();

	// Simulate the workspace chooser: APPLICATION_MODAL dialog opened
	// while the splash screen is already visible.
	// Expected: this dialog appears ON TOP of the splash screen, not behind it.
	Shell dialog = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
	dialog.setText("Workspace Chooser (must be on top of splash)");
	dialog.setLayout(new GridLayout(1, false));

	Label message = new Label(dialog, SWT.WRAP);
	message.setText("Pass: This dialog must be entirely visible above the cyan splash screen.\n\n"
		+ "Fail: If you see a white (or cyan) rectangle covering part of this dialog content,\n"
		+ "      the z-ordering is broken (the splash screen is on top of the dialog).\n\n"
		+ "Click OK to dismiss.");
	GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	gd.widthHint = 400;
	message.setLayoutData(gd);

	Button ok = new Button(dialog, SWT.PUSH);
	ok.setText("OK");
	ok.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
	ok.addListener(SWT.Selection, e -> dialog.close());

	dialog.setSize(500, 300);
	dialog.setLocation(
		displayBounds.x + (displayBounds.width - 500) / 2,
		displayBounds.y + (displayBounds.height - 300) / 2);
	dialog.open();

	while (!dialog.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}

	splashShell.dispose();
	display.dispose();
}

}
