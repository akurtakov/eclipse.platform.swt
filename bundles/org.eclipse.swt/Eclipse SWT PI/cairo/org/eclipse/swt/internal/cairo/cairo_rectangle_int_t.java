/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Contributor(s):
 *
 * IBM
 * -  Binding to permit interfacing between Cairo and SWT
 * -  Copyright (C) 2005, 2018 Red Hat Inc. All Rights Reserved.
 *
 * ***** END LICENSE BLOCK ***** */
package org.eclipse.swt.internal.cairo;

import org.eclipse.swt.internal.gtk.*;

public class cairo_rectangle_int_t {
	/** @field cast=(int) */
	public int x;
	/** @field cast=(int) */
	public int y;
	/** @field cast=(int) */
	public int width;
	/** @field cast=(int) */
	public int height;
	public static final int sizeof = Cairo.cairo_rectangle_int_t_sizeof();

	public void convertFromGdkRectangle(GdkRectangle rect) {
		if (rect != null) {
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
		}
		return;
	}

	/**
	 * Checks if the rectangle is valid for use with Cairo/Pixman region functions.
	 * A rectangle is considered valid if both width and height are greater than zero.
	 * Pixman will emit a warning if an invalid rectangle is passed to region functions.
	 *
	 * @return true if the rectangle has positive width and height, false otherwise
	 */
	public boolean isValid() {
		return width > 0 && height > 0;
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the <code>cairo_rectangle_int_t</code>
	 */
	@Override
	public String toString() {
		return "cairo_rectangle_int_t {" + x + ", " + y + ", " + width + ", " + height + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
}
