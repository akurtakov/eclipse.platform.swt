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
 * the user clicks elsewhere.
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
	static Composite[] fastViewPanels;
	static ToolItem[] toolItems;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Snippet 393");
		shell.setLayout(new FormLayout());

		// Vertical toolbar on the left acts as the fast view bar
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.VERTICAL);

		String[] viewNames = { "Package Explorer", "Console", "Outline" };
		fastViewPanels = new Composite[viewNames.length];
		toolItems = new ToolItem[viewNames.length];

		// Main content area: null layout so fast view overlays can be placed
		// inside it via setBounds() without a layout manager interfering.
		Composite main = new Composite(shell, SWT.NONE);
		Text editor = new Text(main, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		editor.setText("Main editor area.\n\n"
				+ "Click the buttons in the left toolbar to show fast views.\n"
				+ "Fast views auto-hide when you click elsewhere.");
		// Keep the editor filling main whenever main is resized
		main.addListener(SWT.Resize, e -> {
			Rectangle c = main.getClientArea();
			editor.setBounds(0, 0, c.width, c.height);
		});

		for (int i = 0; i < viewNames.length; i++) {
			final int index = i;
			ToolItem item = new ToolItem(toolBar, SWT.CHECK);
			item.setText(viewNames[i].substring(0, 2));
			item.setToolTipText(viewNames[i]);
			toolItems[i] = item;

			// Fast view panel as an overlay Composite inside 'main'.
			// Being a child of 'main' (which has no layout manager) means
			// the shell's FormLayout never touches these panels, so manual
			// setBounds() positioning is preserved. This also avoids any
			// dependency on Shell.setLocation(), which is a no-op on GTK4.
			Composite panel = new Composite(main, SWT.BORDER);
			panel.setVisible(false);
			panel.setLayout(new GridLayout());
			fastViewPanels[i] = panel;

			// View title
			Label titleLabel = new Label(panel, SWT.NONE);
			titleLabel.setText(viewNames[i]);

			// Separator below title
			Label sep = new Label(panel, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// View-specific content
			if (i == 0) {
				Tree tree = new Tree(panel, SWT.BORDER | SWT.V_SCROLL);
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
				Text text = new Text(panel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
				text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				text.setText("Console output:\nBuild successful.\n> Running tests...");
			} else {
				List list = new List(panel, SWT.BORDER | SWT.V_SCROLL);
				list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				list.add("class MyClass");
				list.add("  field: name : String");
				list.add("  method: run()");
				list.add("  method: stop()");
			}

			// Toggle the fast view panel on toolbar button click
			item.addListener(SWT.Selection, e -> {
				if (item.getSelection()) {
					showFastView(toolBar, main, index);
				} else {
					fastViewPanels[index].setVisible(false);
				}
			});
		}

		// Auto-hide: when the user clicks outside a fast view panel, hide it.
		// The panel is hidden synchronously; the ToolItem deselection is deferred
		// with asyncExec so it does not interfere with SWT's own CHECK-item toggle.
		display.addFilter(SWT.MouseDown, e -> {
			if (!(e.widget instanceof Control clicked)) return;
			for (int i = 0; i < fastViewPanels.length; i++) {
				Composite panel = fastViewPanels[i];
				if (panel.isDisposed() || !panel.isVisible()) continue;
				if (isDescendant(clicked, panel)) continue;
				panel.setVisible(false);
				final int idx = i;
				display.asyncExec(() -> {
					if (!toolItems[idx].isDisposed()) toolItems[idx].setSelection(false);
				});
			}
		});

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

	/** Returns true if {@code control} is within (or is) the given {@code ancestor}. */
	static boolean isDescendant(Control control, Composite ancestor) {
		while (control != null) {
			if (control == ancestor) return true;
			control = control.getParent();
		}
		return false;
	}

	/**
	 * Position and reveal the fast view panel for the given index.
	 * <p>
	 * The panel is a child of {@code main}, so coordinates are relative to
	 * {@code main}'s client area — no {@code toDisplay()} call is needed and
	 * the code works correctly on both GTK3 and GTK4.
	 */
	static void showFastView(ToolBar toolBar, Composite main, int index) {
		// Close any other open fast views
		for (int i = 0; i < fastViewPanels.length; i++) {
			if (i != index) {
				fastViewPanels[i].setVisible(false);
				toolItems[i].setSelection(false);
			}
		}
		// Toolbar y-offset of the clicked item, converted to main's coordinate space.
		// Both toolBar and main share the same parent (the shell), so the y-origin
		// of main within the shell is subtracted to get main-relative y.
		Rectangle tbBounds = toolBar.getBounds();
		Rectangle mainBounds = main.getBounds();
		Rectangle itemBounds = toolItems[index].getBounds();
		int x = 0; // left edge of main
		int y = (tbBounds.y + itemBounds.y) - mainBounds.y;
		fastViewPanels[index].setBounds(x, y, FAST_VIEW_WIDTH, FAST_VIEW_HEIGHT);
		fastViewPanels[index].moveAbove(null); // raise above editor and other siblings
		fastViewPanels[index].setVisible(true);
		fastViewPanels[index].setFocus();
	}
}
