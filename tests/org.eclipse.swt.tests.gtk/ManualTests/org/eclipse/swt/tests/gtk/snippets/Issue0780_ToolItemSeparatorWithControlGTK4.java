/*******************************************************************************
 * Copyright (c) 2025 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Contributors to Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/*
 * Title: Issue 780: GTK4 ToolItem SEPARATOR with setControl() renders empty
 *        and popup appears in wrong place (CDT Launchbar scenario).
 *
 * How to run: Launch snippet and observe the ToolBar inside the Shell.
 *
 * Bug description:
 *   On GTK4, a SEPARATOR ToolItem hosting a custom Control via setControl()
 *   renders as empty (invisible). The hosted control has zero size because
 *   resizeHandle() was called with 0-dimension itemRect (GTK4 unshown widget
 *   default allocation is {0,0,0,0}, unlike GTK3's {0,0,1,1}), which
 *   permanently set the GtkSeparator's size-request to 0.
 *
 * Expected results:
 *   - The ToolBar should show: [Button1] [---- Launchbar Composite ----] [Button3]
 *   - The Launchbar composite (with green border) must be visible and properly sized.
 *   - Clicking the "Open Popup" button inside the composite should show a popup
 *     at the correct location (bottom-left of the button, not the toolbar origin).
 *
 * GTK Version(s): GTK4 (also valid on GTK3)
 */
public class Issue0780_ToolItemSeparatorWithControlGTK4 {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Issue 780 - ToolItem SEPARATOR with Control on GTK4");
		shell.setLayout(new FillLayout());

		ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.BORDER);

		ToolItem button1 = new ToolItem(bar, SWT.PUSH);
		button1.setText("Button1");

		// The "Launchbar-like" composite: parent is the ToolBar
		Composite launchbar = new Composite(bar, SWT.BORDER);
		launchbar.setLayout(new RowLayout(SWT.HORIZONTAL));

		Label label = new Label(launchbar, SWT.NONE);
		label.setText("Launchbar:");

		Button openPopup = new Button(launchbar, SWT.PUSH);
		openPopup.setText("Open Popup");

		// SEPARATOR ToolItem that hosts the composite – CDT Launchbar pattern
		ToolItem separatorItem = new ToolItem(bar, SWT.SEPARATOR);
		separatorItem.setControl(launchbar);
		separatorItem.setWidth(launchbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

		ToolItem button3 = new ToolItem(bar, SWT.PUSH);
		button3.setText("Button3");

		// Clicking the popup button should open a small shell near the separator
		openPopup.addListener(SWT.Selection, e -> {
			Rectangle bounds = separatorItem.getBounds();
			Point pt = bar.toDisplay(bounds.x, bounds.y + bounds.height);

			Shell popup = new Shell(shell, SWT.ON_TOP | SWT.TOOL | SWT.BORDER);
			popup.setLayout(new FillLayout());
			popup.setLocation(pt);
			popup.setSize(200, 60);
			new Label(popup, SWT.NONE).setText("Popup at (" + pt.x + "," + pt.y + ")");
			popup.open();
			// Auto-close after 3 seconds
			display.timerExec(3000, popup::close);
		});

		shell.setSize(600, 80);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
