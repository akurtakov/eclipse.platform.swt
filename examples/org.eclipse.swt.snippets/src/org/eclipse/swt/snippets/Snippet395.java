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

		final int itemCount = 1;
		// Keep track of per-item images so they can be disposed on exit.
		final List<Image> images = new ArrayList<>();

		Tree tree = new Tree(shell, SWT.VIRTUAL);
		tree.addListener(SWT.SetData, event -> {
			TreeItem item = (TreeItem) event.item;
			item.setText(0, "A");
			// A new Image is created on every SetData call – this is the
			// critical pattern from issue 678 that can trigger a SIGSEGV in
			// Tree.cellDataProc on GTK3.
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
		tree.setItemCount(itemCount);

		shell.setSize(400, 300);
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
