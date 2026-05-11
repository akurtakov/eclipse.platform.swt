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
 * Tree example snippet: reproduce tiny owner-drawn tree content on GTK4
 * (Package Explorer-like custom drawing)
 *
 * Run with SWT_GTK4=1 and a HiDPI scale factor (for example 200%) to observe
 * the issue where owner-drawn tree content can appear too small.
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

	Tree tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
	for (int i = 0; i < 30; i++) {
		TreeItem item = new TreeItem(tree, SWT.NONE);
		item.setText("Project " + i + " - src - org.eclipse.swt.snippets");
	}

	Image image = new Image(display, 16, 16);
	GC imageGC = new GC(image);
	imageGC.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
	imageGC.fillRectangle(1, 3, 14, 11);
	imageGC.setBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
	imageGC.fillRectangle(1, 1, 8, 3);
	imageGC.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
	imageGC.drawRectangle(0, 2, 15, 13);
	imageGC.dispose();

	Listener ownerDraw = event -> {
		TreeItem item = (TreeItem) event.item;
		switch (event.type) {
			case SWT.MeasureItem: {
				Point textExtent = event.gc.stringExtent(item.getText());
				event.width = image.getBounds().width + 6 + textExtent.x;
				event.height = Math.max(event.height, image.getBounds().height + 2);
				break;
			}
			case SWT.EraseItem:
				event.detail &= ~SWT.FOREGROUND;
				break;
			case SWT.PaintItem: {
				int imageY = event.y + (event.height - image.getBounds().height) / 2;
				event.gc.drawImage(image, event.x, imageY);
				Point textExtent = event.gc.stringExtent(item.getText());
				int textY = event.y + (event.height - textExtent.y) / 2;
				event.gc.drawText(item.getText(), event.x + image.getBounds().width + 6, textY, true);
				break;
			}
		}
	};
	tree.addListener(SWT.MeasureItem, ownerDraw);
	tree.addListener(SWT.EraseItem, ownerDraw);
	tree.addListener(SWT.PaintItem, ownerDraw);

	shell.setSize(560, 420);
	shell.open();
	while (!shell.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
	image.dispose();
	display.dispose();
}
}
