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
 * Tree example: virtual Tree creates a new Image per item inside SWT.SetData.
 * Reproduces issue 678: SIGSEGV in Tree.cellDataProc when calling
 * TreeItem.setImage inside the SWT.SetData listener on GTK3.
 *
 * Root cause: the very first setImage call from SetData (when pixbufSizeSet
 * is still false) triggers Tree.createRenderers(), which calls
 * gtk_tree_view_column_clear() and rebuilds the column's cell renderers while
 * GTK is already iterating those same renderers inside
 * gtk_tree_view_column_cell_set_cell_data(). After createRenderers() returns,
 * cellDataProc() continues using the now-freed old cell pointer, causing a
 * use-after-free. GTK then also advances to the next renderer in its
 * (corrupted) iteration list – on GTK3 this triggers a SIGSEGV.
 *
 * How to maximise the crash probability:
 *  - Use SWT.CHECK so the column contains three renderers (check, pixbuf,
 *    text). After the pixbuf callback frees all three old renderers via
 *    createRenderers(), GTK still tries to invoke the text callback with a
 *    freed pointer.
 *  - Use many items and a large window so that many rows are rendered
 *    simultaneously, increasing memory pressure and the chance that freed
 *    renderer memory is reused before GTK dereferences it.
 *
 * For a list of all SWT example snippets see
 * https://eclipse.dev/eclipse/swt/snippets/
 */
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet395 {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Snippet 395");
		shell.setLayout(new FillLayout());

		// Keep track of per-item images so they can be disposed on exit.
		final List<Image> images = new ArrayList<>();

		/*
		 * SWT.CHECK adds a third renderer to the column (check, pixbuf, text).
		 * When createRenderers() is triggered from the first setImage() call
		 * inside SetData, all three old renderers are freed via
		 * gtk_tree_view_column_clear(). GTK's iteration then advances to the
		 * freed text (and check) renderer nodes, making a SIGSEGV more likely.
		 */
		Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.CHECK);
		tree.addListener(SWT.SetData, event -> {
			TreeItem item = (TreeItem) event.item;
			int index = tree.indexOf(item);
			item.setText("Item " + index);
			/*
			 * A brand-new Image is created on every SetData call. When
			 * pixbufSizeSet is still false (i.e. for the very first item),
			 * setImage() calls createRenderers(), which clears and rebuilds
			 * the column's cell renderers while cellDataProc() is on the call
			 * stack – this is the bug from issue 678.
			 */
			Image image = new Image(display, 20, 20);
			GC gc = new GC(image);
			try {
				gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
				gc.fillRectangle(image.getBounds());
			} finally {
				gc.dispose();
			}
			images.add(image);
			item.setImage(image);
		});

		// Many items shown in a large window maximise rendering concurrency
		// and memory pressure, making the use-after-free more likely to crash.
		tree.setItemCount(3000);

		shell.setSize(800, 1000);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		for (Image img : images) {
			img.dispose();
		}
		display.dispose();
	}
}
