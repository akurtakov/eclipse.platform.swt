package org.eclipse.swt.snippets;

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
/*
 * example snippet: Hello World
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.widgets.*;

public class SnippetScreenshot {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Snippet Screenshot");
		shell.open();

		long[] error = new long[1];
		int sessionManagerFlags = OS.G_DBUS_PROXY_FLAGS_DO_NOT_AUTO_START | OS.G_DBUS_PROXY_FLAGS_DO_NOT_LOAD_PROPERTIES
				| OS.G_DBUS_PROXY_FLAGS_DO_NOT_CONNECT_SIGNALS;

		long proxy = OS.g_dbus_proxy_new_for_bus_sync(OS.G_BUS_TYPE_SESSION, sessionManagerFlags, 0,
				Converter.javaStringToCString("org.freedesktop.portal.Desktop"), /* name */
				Converter.javaStringToCString("/org/freedesktop/portal/desktop"), /* object path */
				Converter.javaStringToCString("org.freedesktop.portal.Screenshot"), /* interface */
				0, error);

		long builder = OS.g_variant_builder_new(OS.g_variant_type_new(Converter.javaStringToCString("a{sv}")));

		OS.g_variant_builder_add(builder, Converter.javaStringToCString("{sv}"),
				Converter.javaStringToCString("interactive"), OS.g_variant_new_boolean(false));

		long[] variants = new long[2];
		variants[0] = OS.g_variant_new(Converter.javaStringToCString("sa{sv}"), Converter.javaStringToCString(""));
		variants[1] = OS.g_variant_builder_end(builder);

		OS.g_dbus_proxy_call_sync(proxy, Converter.javaStringToCString("Screenshot"),
				OS.g_variant_new_tuple(variants, 2), OS.G_DBUS_CALL_FLAGS_NONE, -1, 0, error);

		if (error.length > 0)
			System.out.println(extractFreeGError(error[0]));

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static String extractFreeGError(long errorPtr) {
		if (errorPtr == 0)
			return "No GError!";
		long errorMessageC = OS.g_error_get_message(errorPtr);
		String errorMessageStr = Converter.cCharPtrToJavaString(errorMessageC, false);
		OS.g_error_free(errorPtr);
		return errorMessageStr;
	}
}
