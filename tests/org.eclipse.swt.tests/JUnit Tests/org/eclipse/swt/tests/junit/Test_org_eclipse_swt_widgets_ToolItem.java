/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
package org.eclipse.swt.tests.junit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Automated Test Suite for class org.eclipse.swt.widgets.ToolItem
 *
 * @see org.eclipse.swt.widgets.ToolItem
 */
public class Test_org_eclipse_swt_widgets_ToolItem extends Test_org_eclipse_swt_widgets_Item {

@Override
@BeforeEach
public void setUp() {
	super.setUp();
	toolBar = new ToolBar(shell, 0);
	toolItem = new ToolItem(toolBar, 0);
	setWidget(toolItem);
}

@Test
public void test_ConstructorLorg_eclipse_swt_widgets_ToolBarI() {
	assertThrows(IllegalArgumentException.class, () -> new ToolItem(null, SWT.NULL),
			"No exception thrown for parent == null");
}

@Test
public void test_getToolTipText() {
	toolItem.setToolTipText("fred");
	assertEquals("fred", toolItem.getToolTipText());
	toolItem.setToolTipText("fredttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
	assertEquals("fredttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt", toolItem.getToolTipText());
}

@Override
@Test
public void test_setImageLorg_eclipse_swt_graphics_Image() {
}

@Test
public void test_setDisabledImage() {
		toolItem.setImage(images[0]);
		toolItem.setDisabledImage(images[1]);
		toolItem.setEnabled(false);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setEnabled(true);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setDisabledImage(images[0]);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setEnabled(false);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setImage(images[0]);
		toolItem.setEnabled(true);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setDisabledImage(images[2]);
		toolItem.setEnabled(false);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setEnabled(true);
		toolItem.setDisabledImage(null);
		toolItem.setEnabled(false);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setEnabled(true);
		toolItem.setDisabledImage(null);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setEnabled(false);
		toolItem.setDisabledImage(null);
		assertEquals(images[0], toolItem.getImage());

		toolItem.setImage(null);
		assertEquals(null, toolItem.getImage());
		toolItem.setEnabled(false);
		toolItem.setDisabledImage(images[1]);
		toolItem.setImage(images[1]);
		assertEquals(images[1], toolItem.getImage());
		toolItem.setImage(null);
		assertEquals(null, toolItem.getImage());
		assertEquals(images[1], toolItem.getDisabledImage());
}

@Test
public void test_separatorControl_isSizedAfterShellOpen() {
	ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL);
	new ToolItem(bar, SWT.PUSH).setText("left");

	Composite launchbar = new Composite(bar, SWT.NONE);
	launchbar.setLayout(new RowLayout());
	new Button(launchbar, SWT.PUSH).setText("Launch");

	ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);
	separator.setControl(launchbar);
	separator.setWidth(launchbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

	new ToolItem(bar, SWT.PUSH).setText("right");

	shell.pack();
	shell.open();
	while (shell.getDisplay().readAndDispatch()) {
	}

	assertTrue(separator.getBounds().width > 0);
	assertTrue(launchbar.getBounds().width > 0);
	assertTrue(launchbar.getBounds().height > 0);
}

@Test
public void test_separatorControl_isSizedAfterShellSetSizeOpen() {
	shell.setLayout(new FillLayout());

	ToolBar bar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.BORDER);
	new ToolItem(bar, SWT.PUSH).setText("left");

	Composite launchbar = new Composite(bar, SWT.BORDER);
	launchbar.setLayout(new RowLayout(SWT.HORIZONTAL));
	new Button(launchbar, SWT.PUSH).setText("Launch");

	ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);
	separator.setControl(launchbar);
	separator.setWidth(launchbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

	new ToolItem(bar, SWT.PUSH).setText("right");

	shell.setSize(600, 80);
	shell.open();
	while (shell.getDisplay().readAndDispatch()) {
	}

	assertTrue(separator.getBounds().width > 0);
	assertTrue(launchbar.getBounds().width > 0);
	assertTrue(launchbar.getBounds().height > 0);
}

@Override
@Test
public void test_setTextLjava_lang_String() {
}

/* custom */
ToolBar toolBar;
ToolItem toolItem;
}
