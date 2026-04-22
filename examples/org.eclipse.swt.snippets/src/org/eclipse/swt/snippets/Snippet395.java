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
 * Tree example: virtual Tree sets image from SWT.SetData.
 * Useful for testing issue 678.
 *
 * For a list of all SWT example snippets see
 * https://eclipse.dev/eclipse/swt/snippets/
 */
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

		final int itemCount = 3000;
		final Image image = new Image(display, 20, 20);
		GC gc = new GC(image);
		try {
			gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(image.getBounds());
		} finally {
			gc.dispose();
		}

		Tree tree = new Tree(shell, SWT.BORDER | SWT.VIRTUAL);
		tree.addListener(SWT.SetData, event -> {
			TreeItem item = (TreeItem) event.item;
			int index = tree.indexOf(item);
			item.setText("Item " + index);
			item.setImage(image);
		});
		tree.setItemCount(itemCount);

		shell.setSize(500, 400);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		image.dispose();
		display.dispose();
	}
}
