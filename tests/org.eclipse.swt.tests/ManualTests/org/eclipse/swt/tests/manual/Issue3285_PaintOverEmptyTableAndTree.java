/*******************************************************************************
 * Copyright (c) 2026 Contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     tmssngr - initial report (issue #3285)
 *******************************************************************************/

package org.eclipse.swt.tests.manual;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Manual test for issue #3285: [GTK] Can't draw inside (empty) table.
 * <p>
 * Expected: "Nothing to see here :(" text is centered in the body of both the
 * Table and the Tree on all platforms.
 * <p>
 * Bug: On Linux/GTK3 the text was invisible because SWT.Paint fired before
 * GTK rendered the widget background, so GTK's background fill covered the
 * custom drawing. Fixed by moving the gtk_draw() call to the EXPOSE_EVENT
 * handler (after GTK renders).
 */
public class Issue3285_PaintOverEmptyTableAndTree {

	private static void addPaintListener(Control control) {
		control.addListener(SWT.Paint, event -> {
			Rectangle clientArea = control.getClientArea();
			String s = "Nothing to see here :(";
			Point size = event.gc.stringExtent(s);
			event.gc.drawString(s,
					clientArea.x + (clientArea.width - size.x) / 2,
					clientArea.y + (clientArea.height - size.y) / 2);
		});
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Issue #3285 - SWT.Paint on empty Table/Tree");
		shell.setLayout(new GridLayout(2, true));

		final Label tableLabel = new Label(shell, SWT.NONE);
		tableLabel.setText("Empty Table (SWT.Paint):");

		final Label treeLabel = new Label(shell, SWT.NONE);
		treeLabel.setText("Empty Tree (SWT.Paint):");

		final Table table = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText("Column");
		column.setWidth(100);
		table.setHeaderVisible(true);
		addPaintListener(table);

		final Tree tree = new Tree(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
		treeColumn.setText("Column");
		treeColumn.setWidth(100);
		tree.setHeaderVisible(true);
		addPaintListener(tree);

		shell.setSize(600, 400);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}
