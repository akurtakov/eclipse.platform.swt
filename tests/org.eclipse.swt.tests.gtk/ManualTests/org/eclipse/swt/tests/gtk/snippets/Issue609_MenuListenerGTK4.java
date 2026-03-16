/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for Issue 609: MenuListener on GTK4 not called for menus in menu bar.
 *
 * <p>Expected: Opening the "File" menu should print "Menu shown" to stdout, and
 * closing it should print "Menu hidden". Both events are fired for both the
 * popup menu (right-click) and the menu-bar submenu.
 */
public class Issue609_MenuListenerGTK4 {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("MenuListener Test (GTK4)");

		// --- Menubar submenu ---
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

		MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText("File");

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(fileMenu);

		fileMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				System.out.println("Menu bar submenu shown");
			}
			@Override
			public void menuHidden(MenuEvent e) {
				System.out.println("Menu bar submenu hidden");
			}
		});

		new MenuItem(fileMenu, SWT.PUSH).setText("Item 1");

		// --- Popup menu ---
		Menu popupMenu = new Menu(shell, SWT.POP_UP);
		shell.setMenu(popupMenu);

		popupMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				System.out.println("Popup menu shown");
			}
			@Override
			public void menuHidden(MenuEvent e) {
				System.out.println("Popup menu hidden");
			}
		});

		new MenuItem(popupMenu, SWT.PUSH).setText("Popup Item 1");

		shell.setSize(300, 200);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}
