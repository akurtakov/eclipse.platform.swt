/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * Simulate Eclipse fast views: views minimized to icons in a vertical side
 * toolbar that expand as floating panels when clicked, and auto-hide when
 * the panel loses focus.
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet393 {

	static final int FAST_VIEW_WIDTH = 260;
	static final int FAST_VIEW_HEIGHT = 220;
	static Shell[] fastViewShells;
	static ToolItem[] toolItems;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Snippet 393");
		shell.setLayout(new FormLayout());

		// Vertical toolbar on the left acts as the fast view bar
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.VERTICAL);

		String[] viewNames = { "Package Explorer", "Console", "Outline" };
		fastViewShells = new Shell[viewNames.length];
		toolItems = new ToolItem[viewNames.length];

		for (int i = 0; i < viewNames.length; i++) {
			final int index = i;
			ToolItem item = new ToolItem(toolBar, SWT.CHECK);
			item.setText(viewNames[i].substring(0, 2));
			item.setToolTipText(viewNames[i]);
			toolItems[i] = item;

			// Floating fast view panel (child shell, no title bar)
			Shell fastView = new Shell(shell, SWT.BORDER | SWT.ON_TOP);
			fastView.setVisible(false);
			fastView.setLayout(new GridLayout());
			fastViewShells[i] = fastView;

			// View title
			Label titleLabel = new Label(fastView, SWT.NONE);
			titleLabel.setText(viewNames[i]);

			// Separator below title
			Label sep = new Label(fastView, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// View-specific content
			if (i == 0) {
				Tree tree = new Tree(fastView, SWT.BORDER | SWT.V_SCROLL);
				tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				TreeItem project = new TreeItem(tree, SWT.NONE);
				project.setText("MyProject");
				TreeItem src = new TreeItem(project, SWT.NONE);
				src.setText("src");
				new TreeItem(src, SWT.NONE).setText("Main.java");
				new TreeItem(src, SWT.NONE).setText("Utils.java");
				project.setExpanded(true);
				src.setExpanded(true);
			} else if (i == 1) {
				Text text = new Text(fastView, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
				text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				text.setText("Console output:\nBuild successful.\n> Running tests...");
			} else {
				List list = new List(fastView, SWT.BORDER | SWT.V_SCROLL);
				list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				list.add("class MyClass");
				list.add("  field: name : String");
				list.add("  method: run()");
				list.add("  method: stop()");
			}

			// Auto-hide the panel when it loses focus.
			// asyncExec defers the check so that a toolbar-button click (which
			// first triggers Deactivate and then Selection) can hide the panel
			// via the Selection listener without a race.
			fastView.addListener(SWT.Deactivate, e -> display.asyncExec(() -> {
				if (!fastViewShells[index].isDisposed() && fastViewShells[index].isVisible()) {
					fastViewShells[index].setVisible(false);
					toolItems[index].setSelection(false);
				}
			}));

			// Toggle the fast view panel on toolbar button click
			item.addListener(SWT.Selection, e -> {
				if (item.getSelection()) {
					showFastView(shell, toolBar, index);
				} else {
					fastViewShells[index].setVisible(false);
				}
			});
		}

		// Main content area filling the rest of the shell
		Composite main = new Composite(shell, SWT.NONE);
		main.setLayout(new FillLayout());
		Text editor = new Text(main, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		editor.setText("Main editor area.\n\n"
				+ "Click the buttons in the left toolbar to show fast views.\n"
				+ "Fast views auto-hide when you click elsewhere.");

		// Toolbar on the left; main content fills the remainder
		FormData tbData = new FormData();
		tbData.left = new FormAttachment(0);
		tbData.top = new FormAttachment(0);
		tbData.bottom = new FormAttachment(100);
		toolBar.setLayoutData(tbData);

		FormData mainData = new FormData();
		mainData.left = new FormAttachment(toolBar);
		mainData.right = new FormAttachment(100);
		mainData.top = new FormAttachment(0);
		mainData.bottom = new FormAttachment(100);
		main.setLayoutData(mainData);

		shell.setSize(600, 400);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/** Position and open the fast view panel for the given index. */
	static void showFastView(Shell parent, ToolBar toolBar, int index) {
		// Close any other open fast view
		for (int i = 0; i < fastViewShells.length; i++) {
			if (i != index) {
				fastViewShells[i].setVisible(false);
				toolItems[i].setSelection(false);
			}
		}
		// Place the panel to the right of the toolbar, aligned with the item
		Shell fastView = fastViewShells[index];
		Rectangle tbBounds = toolBar.getBounds();
		Point origin = parent.toDisplay(tbBounds.x + tbBounds.width, tbBounds.y);
		Rectangle itemBounds = toolItems[index].getBounds();
		fastView.setSize(FAST_VIEW_WIDTH, FAST_VIEW_HEIGHT);
		fastView.setLocation(origin.x, origin.y + itemBounds.y);
		fastView.open();
		fastView.forceFocus();
	}
}
