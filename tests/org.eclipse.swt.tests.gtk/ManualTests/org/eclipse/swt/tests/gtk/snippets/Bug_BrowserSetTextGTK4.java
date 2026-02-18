/*******************************************************************************
 * Copyright (c) 2026 and others.
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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for Browser.setText on GTK4/WebkitGTK 6 with Wayland
 * 
 * Steps to reproduce:
 * 1. Run this snippet on GTK4/WebkitGTK 6 with Wayland compositor
 * 2. The browser should display "Hello World from setText!" with a heading and paragraph
 * 
 * Expected result:
 * - Content is rendered correctly in the Browser widget
 * 
 * Issue:
 * - Browser.setText() renders blank on Wayland but works fine on X11
 * - Root cause: webkit_web_view_load_html doesn't work properly with GTK4 sandbox on Wayland
 * - /dev/shm cannot be added to sandbox (explicitly disallowed by WebKitGTK)
 * 
 * Fix:
 * - Use webkit_web_view_load_bytes instead of webkit_web_view_load_html for GTK4
 * - This API handles shared memory requirements internally in a sandbox-compatible way
 */
public class Bug_BrowserSetTextGTK4 {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Browser.setText Test on GTK4");
		
		Browser browser = new Browser(shell, SWT.NONE);
		String html = """
				<html>
				<head>
					<title>Browser setText Test</title>
					<style>
						body { font-family: Arial, sans-serif; margin: 20px; }
						h1 { color: #0066cc; }
						p { color: #333; }
					</style>
				</head>
				<body>
					<h1>Hello World from setText!</h1>
					<p>This is a test of Browser.setText() on GTK4/WebkitGTK 6.</p>
					<p>If you can see this text, the fix is working correctly!</p>
					<ul>
						<li>setText with trusted=true works</li>
						<li>setText with trusted=false works</li>
						<li>HTML content is rendered properly</li>
					</ul>
				</body>
				</html>
				""";
		
		// Test with default trusted=true
		boolean success = browser.setText(html);
		System.out.println("setText returned: " + success);
		
		shell.setSize(800, 600);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
