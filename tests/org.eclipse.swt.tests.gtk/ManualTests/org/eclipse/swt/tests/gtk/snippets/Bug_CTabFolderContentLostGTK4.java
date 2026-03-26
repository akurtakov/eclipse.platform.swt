/*******************************************************************************
 * Copyright (c) 2024 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/*
 * Title: CTab content on Gtk 4 gets lost after going to another tab and back
 * How to run: Run this snippet on GTK4 (SWT_GTK4=1). Click "Tab 2", then click
 *             "Tab 1".
 * Bug description: After switching back to "Tab 1", the green content area
 *                  disappears (becomes blank/white).
 * Expected results: The green "Content of Tab 1" area remains visible after
 *                   switching tabs back and forth.
 * GTK Version(s): GTK4
 */
public class Bug_CTabFolderContentLostGTK4 {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(400, 300);
		shell.setText("CTabFolder Content Visibility Test");

		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);

		CTabItem tab1 = new CTabItem(folder, SWT.NONE);
		tab1.setText("Tab 1");
		Composite content1 = new Composite(folder, SWT.NONE);
		content1.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		content1.setLayout(new FillLayout());
		Label label1 = new Label(content1, SWT.NONE);
		label1.setText("Content of Tab 1 (should always be visible)");
		tab1.setControl(content1);

		CTabItem tab2 = new CTabItem(folder, SWT.NONE);
		tab2.setText("Tab 2");
		Composite content2 = new Composite(folder, SWT.NONE);
		content2.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
		content2.setLayout(new FillLayout());
		Label label2 = new Label(content2, SWT.NONE);
		label2.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		label2.setText("Content of Tab 2");
		tab2.setControl(content2);

		folder.setSelection(0);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
