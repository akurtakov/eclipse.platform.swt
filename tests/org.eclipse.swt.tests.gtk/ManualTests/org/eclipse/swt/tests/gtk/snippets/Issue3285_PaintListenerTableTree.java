/*******************************************************************************
 * Copyright (c) 2025 Contributors and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Contributors - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Test for https://github.com/eclipse-platform/eclipse.platform.swt/issues/3285
 *
 * PaintListener should work for Table and Tree widgets on GTK:
 * - Text drawn by the paint listener should be visible on top of table/tree items.
 * - Items themselves should remain visible underneath the painted text.
 *
 * Expected: All four widgets (empty table, table with items, empty tree, tree with items)
 * should show the centered text "Paint listener works!" on top of the widget content.
 */
public class Issue3285_PaintListenerTableTree {

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, true));
		shell.setText("Issue 3285 - PaintListener on Table/Tree");

		// --- Empty Table ---
		new Label(shell, SWT.NONE).setText("Empty Table (should show paint text):");
		new Label(shell, SWT.NONE).setText("Table with items (paint text on top of items):");

		final Table emptyTable = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		emptyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableColumn col1 = new TableColumn(emptyTable, SWT.LEFT);
		col1.setText("Column");
		col1.setWidth(120);
		emptyTable.setHeaderVisible(true);
		addPaintText(emptyTable);

		// --- Table with items ---
		final Table filledTable = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		filledTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableColumn col2 = new TableColumn(filledTable, SWT.LEFT);
		col2.setText("Column");
		col2.setWidth(120);
		filledTable.setHeaderVisible(true);
		for (int i = 0; i < 5; i++) {
			TableItem item = new TableItem(filledTable, SWT.NONE);
			item.setText("Item " + i);
		}
		addPaintText(filledTable);

		// --- Empty Tree ---
		new Label(shell, SWT.NONE).setText("Empty Tree (should show paint text):");
		new Label(shell, SWT.NONE).setText("Tree with items (paint text on top of items):");

		final Tree emptyTree = new Tree(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		emptyTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TreeColumn treeCol1 = new TreeColumn(emptyTree, SWT.LEFT);
		treeCol1.setText("Column");
		treeCol1.setWidth(120);
		emptyTree.setHeaderVisible(true);
		addPaintText(emptyTree);

		// --- Tree with items ---
		final Tree filledTree = new Tree(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		filledTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TreeColumn treeCol2 = new TreeColumn(filledTree, SWT.LEFT);
		treeCol2.setText("Column");
		treeCol2.setWidth(120);
		filledTree.setHeaderVisible(true);
		for (int i = 0; i < 5; i++) {
			TreeItem item = new TreeItem(filledTree, SWT.NONE);
			item.setText("Item " + i);
		}
		addPaintText(filledTree);

		shell.setSize(600, 500);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void addPaintText(Table table) {
		final String text = "Paint listener works!";
		table.addListener(SWT.Paint, event -> {
			Rectangle clientArea = table.getClientArea();
			Point size = event.gc.stringExtent(text);
			event.gc.drawString(text,
					clientArea.x + (clientArea.width - size.x) / 2,
					clientArea.y + (clientArea.height - size.y) / 2,
					true);
		});
	}

	private static void addPaintText(Tree tree) {
		final String text = "Paint listener works!";
		tree.addListener(SWT.Paint, event -> {
			Rectangle clientArea = tree.getClientArea();
			Point size = event.gc.stringExtent(text);
			event.gc.drawString(text,
					clientArea.x + (clientArea.width - size.x) / 2,
					clientArea.y + (clientArea.height - size.y) / 2,
					true);
		});
	}
}
