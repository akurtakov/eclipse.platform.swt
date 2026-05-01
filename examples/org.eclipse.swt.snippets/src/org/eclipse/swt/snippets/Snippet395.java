/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * CTabFolder example: gradient background on tab header.
 *
 * Verifies that CTabFolder.setBackground(Color[], int[], boolean) paints a
 * smooth gradient on the tab-header bar and does not leak the solid end-colour
 * into the body area or the empty region to the right of the tabs (regression
 * test for the GTK4 CSS background-colour bleed, where the last gradient colour
 * flooded the entire widget bounds before the SWT paint event ran).
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet395 {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Snippet 395");
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 20;
		fillLayout.marginHeight = 20;
		shell.setLayout(fillLayout);

		CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.CLOSE);

		// Gradient from yellow → orange → red across the full tab-header height.
		// Expected: each unselected tab header shows the gradient; the body area
		// and the empty space to the right of the tabs must NOT be painted with
		// the solid end-colour (red/orange).
		folder.setBackground(
				new Color[] {
						display.getSystemColor(SWT.COLOR_YELLOW),
						display.getSystemColor(SWT.COLOR_RED) },
				new int[] { 100 },
				true);

		for (int i = 0; i < 3; i++) {
			CTabItem item = new CTabItem(folder, SWT.CLOSE);
			item.setText("Item " + i);
			Text text = new Text(folder, SWT.MULTI);
			text.setText("Content for Item " + i);
			item.setControl(text);
		}
		folder.setSelection(0);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
