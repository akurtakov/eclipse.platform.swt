package org.eclipse.swt.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.swt.SWT;

/**
 * This class is a placeholder for utility methods commonly
 * used on J2SE platforms but not supported on some J2ME
 * profiles.
 * <p>
 * It is part of our effort to provide support for both J2SE
 * and J2ME platforms.
 * </p>
 * <p>
 * IMPORTANT: some of the methods have been modified from their
 * J2SE parents. For example, exceptions thrown may differ since
 * J2ME's set of exceptions is a subset of J2SE's one. Refer to
 * the description of each method for specific changes.
 * </p>
 */
public final class Compatibility {

/**
 * Answers the double conversion of the most negative (i.e.
 * closest to negative infinity) integer value which is
 * greater than the argument.
 *
 * @param d the value to be converted
 * @return the ceiling of the argument.
 */
public static double ceil (double d) {
	return Math.ceil(d);
}

/**
 * Returns 2 raised to the power of the argument.
 *
 * @param n an int value between 0 and 30 (inclusive)
 * @return 2 raised to the power of the argument
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the argument is not between 0 and 30 (inclusive)</li>
 * </ul>
 */
public static int pow2(int n) {
	if (n >= 1 && n <= 30)
		return 2 << (n - 1);
	else if (n != 0) {
		SWT.error(SWT.ERROR_INVALID_RANGE);
	}
	return 1;
}

/**
 * Loads a library if the underlying platform supports this.
 * If not, it is assumed that the library in question was 
 * properly made available in some other fashion.
 *
 * @param name the name of the library to load
 *
 * @exception SecurityException
 *   if the library was not allowed to be loaded
 * @exception SWTError <ul>
 *    <li>ERROR_FAILED_LOAD_LIBRARY - if the library could not be loaded</li>
 * </ul>
 */
public static void loadLibrary(String name) {
	try {
		System.loadLibrary (name);
	} catch (UnsatisfiedLinkError e) {
		SWT.error(SWT.ERROR_FAILED_LOAD_LIBRARY,e);
	}
}

/**
 * Test if the character is a whitespace character.
 *
 * @param ch the character to test
 * @return true if the character is whitespace
 */
public static boolean isWhitespace(char ch) {
	return Character.isWhitespace(ch);
}

private static ResourceBundle msgs = null;

/**
 * Returns the NLS'ed message for the given argument. This is only being
 * called from SWT.
 * 
 * @param key the key to look up
 * @return the message for the given key
 * 
 * @see SWT#getMessage
 */
public static String getMessage(String key) {
	String answer = key;
	
	if (key == null) {
		SWT.error (SWT.ERROR_NULL_ARGUMENT);
	}	
	if (msgs == null) {
		try {
			msgs = ResourceBundle.getBundle("org.eclipse.swt.internal.SWTMessages");
		} catch (MissingResourceException ex) {
			answer = key + " (no resource bundle)";
		}
	}
	if (msgs != null) {
		try {
			answer = msgs.getString(key);
		} catch (MissingResourceException ex2) {}
	}
	return answer;
}
	
/**
 * Interrupt the current thread. 
 * <p>
 * Note that this is not available on CLDC.
 * </p>
 */
public static void interrupt() {
	Thread.currentThread().interrupt();
}

}
