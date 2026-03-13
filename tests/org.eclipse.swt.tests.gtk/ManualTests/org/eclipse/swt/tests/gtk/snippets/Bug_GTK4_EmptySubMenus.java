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
package org.eclipse.swt.tests.gtk.snippets;

/*
 * Title: GTK4 Empty Sub-Menus Bug Reproduction
 * How to run: Run snippet and open File menu to see New submenu
 * Bug description: Sub-menus appear empty in GTK4 due to missing SWT.Show events
 * Expected result: File > New submenu should show "Project", "File", "Folder" items
 * GTK version(s): GTK4 only
 */

import static org.eclipse.swt.events.SelectionListener.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Bug_GTK4_EmptySubMenus {
	private static int submenuShowCount = 0;
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("GTK4 Empty Sub-Menu Reproduction");
		shell.setLayout(new FillLayout());
		
		// Create a label to show status
		final Label statusLabel = new Label(shell, SWT.WRAP);
		statusLabel.setText("Instructions:\n" +
				"1. Click on 'File' menu\n" +
				"2. Hover over 'New' item to see submenu\n" +
				"3. In GTK4, submenu appears empty (no items)\n" +
				"4. Expected: Should show 'Project', 'File', 'Folder' items\n\n" +
				"Status: Waiting for menu interaction...");
		
		// Create menu bar
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		// File menu
		MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuItem.setText("&File");
		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);
		
		// Add menu listener to track when File menu is shown
		fileMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				statusLabel.setText("✓ File menu shown (SWT.Show fired)\n" +
						"Sub-menu show count: " + submenuShowCount + "\n" +
						"Expected: Sub-menu show count should be > 0 when hovering over 'New'");
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
				// Reset for next test
			}
		});
		
		// New menu item (CASCADE with submenu)
		MenuItem newMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		newMenuItem.setText("&New");
		
		Menu newSubmenu = new Menu(shell, SWT.DROP_DOWN);
		newMenuItem.setMenu(newSubmenu);
		
		// Track when the submenu's SWT.Show event fires
		// In GTK4, this may never fire, causing the submenu to appear empty
		newSubmenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				submenuShowCount++;
				statusLabel.setText("✓ SUCCESS! Sub-menu shown (SWT.Show fired)\n" +
						"Sub-menu show count: " + submenuShowCount + "\n" +
						"The fix is working - lazy population can now happen!");
				
				// Simulate lazy population - add items when menu is shown
				if (newSubmenu.getItemCount() == 0) {
					MenuItem projectItem = new MenuItem(newSubmenu, SWT.PUSH);
					projectItem.setText("Project...");
					projectItem.addSelectionListener(widgetSelectedAdapter(e1 -> 
						System.out.println("New Project selected")));
					
					MenuItem fileItem = new MenuItem(newSubmenu, SWT.PUSH);
					fileItem.setText("File...");
					fileItem.addSelectionListener(widgetSelectedAdapter(e1 -> 
						System.out.println("New File selected")));
					
					MenuItem folderItem = new MenuItem(newSubmenu, SWT.PUSH);
					folderItem.setText("Folder...");
					folderItem.addSelectionListener(widgetSelectedAdapter(e1 -> 
						System.out.println("New Folder selected")));
					
					System.out.println("Lazy population: Added 3 items to New submenu");
				}
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
				// Clean up items to test lazy population again
				for (MenuItem item : newSubmenu.getItems()) {
					item.dispose();
				}
				System.out.println("Submenu hidden - items disposed for next test");
			}
		});
		
		// Add other File menu items
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("E&xit");
		exitItem.addSelectionListener(widgetSelectedAdapter(e -> shell.dispose()));
		
		// Add Edit menu to make it more realistic
		MenuItem editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		editMenuItem.setText("&Edit");
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		editMenuItem.setMenu(editMenu);
		
		MenuItem copyItem = new MenuItem(editMenu, SWT.PUSH);
		copyItem.setText("&Copy");
		
		MenuItem pasteItem = new MenuItem(editMenu, SWT.PUSH);
		pasteItem.setText("&Paste");
		
		shell.setSize(500, 300);
		shell.open();
		
		System.out.println("=== GTK4 Empty Sub-Menu Bug Reproduction ===");
		System.out.println("GTK Version: " + (System.getProperty("org.eclipse.swt.internal.gtk.version", "Unknown")));
		System.out.println("\nTest Instructions:");
		System.out.println("1. Open the File menu");
		System.out.println("2. Hover over 'New' to see submenu");
		System.out.println("3. WITHOUT FIX: Submenu appears empty (no SWT.Show event)");
		System.out.println("4. WITH FIX: Submenu shows items (SWT.Show event fired)");
		System.out.println("\nWatch console and status label for event notifications...\n");
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
