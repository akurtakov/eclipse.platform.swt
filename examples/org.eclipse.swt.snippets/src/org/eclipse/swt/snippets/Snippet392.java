/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform Contributors and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Platform Contributors - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

import java.nio.file.Path;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Snippet to draw an SVG file with height/width of 16/16 as 64/64 image
 * <p>
 * This snippet demonstrates how to properly scale an SVG image without pixelation
 * by using ImageFileNameProvider to render the SVG at the target size (64x64)
 * rather than scaling a rasterized 16x16 image.
 * <p>
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 *
 * @see org.eclipse.swt.graphics.Image#Image(org.eclipse.swt.graphics.Device,
 *      org.eclipse.swt.graphics.ImageFileNameProvider)
 */
public class Snippet392 {

	public static void main(String[] args) throws Exception {
		final Display display = new Display();

		final Shell shell = new Shell(display);
		shell.setText("Snippet 392");
		shell.setLayout(new FillLayout());
		shell.setBounds(250, 50, 500, 250);

		String imgPath = "eclipse.svg";
		Path fullPath = Path.of(Snippet392.class.getResource(imgPath).toURI());

		// Load SVG at 16x16 (native size) - will be pixelated when scaled
		Image svgImage16 = new Image(display, Snippet392.class.getResourceAsStream(imgPath));

		// Load SVG using ImageFileNameProvider to render at 400% zoom (64x64)
		// This ensures the SVG is rasterized at the target size, avoiding pixelation
		Image svgImage64 = new Image(display, (ImageFileNameProvider) zoom -> {
			// Return the SVG path for 400% zoom (64x64 from 16x16)
			return zoom == 400 ? fullPath.toString() : null;
		});

		shell.addPaintListener(e -> {
			// Draw pixelated version (16x16 SVG scaled up)
			e.gc.drawImage(svgImage16, 0, 0, 16, 16, 20, 20, 64, 64);
			e.gc.drawText("Pixelated (16x16 scaled)", 20, 90);

			// Draw crisp version (SVG rendered at 64x64)
			e.gc.drawImage(svgImage64, 150, 20, 64, 64);
			e.gc.drawText("Crisp (rendered at 64x64)", 150, 90);
		});

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		svgImage16.dispose();
		svgImage64.dispose();
		display.dispose();
	}
}
