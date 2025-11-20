/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Contributors - initial API and implementation
 *******************************************************************************/

/*
 * Issue #2794: ComboBox popup menu transparency issue
 * 
 * This snippet demonstrates the problem where setting a ComboBox background
 * color with transparency (alpha < 1.0) causes the popup menu to also be
 * transparent, making it difficult or impossible to read.
 * 
 * To compile and run (GTK3):
 *   gcc -o combo_transparency Issue_2794_ComboPopupTransparency.c `pkg-config --cflags --libs gtk+-3.0`
 *   ./combo_transparency
 * 
 * Expected behavior WITHOUT fix:
 *   - Clicking the combo will show a popup menu that is semi-transparent
 *   - The transparent popup makes text hard to read, especially over busy backgrounds
 * 
 * Expected behavior WITH fix:
 *   - The popup menu should be fully opaque (alpha = 1.0) even though the
 *     combo itself has a transparent background
 *   - Text in the popup should be easily readable
 */

#include <gtk/gtk.h>

static gboolean delete_event(GtkWidget*, GdkEvent*, gpointer);

int main(int argc, char *argv[]) {
	GtkWidget *window;
	GtkWidget *combobox;
	GtkWidget *main_vbox;
	GtkWidget *label;
	GtkCssProvider *css_provider;
	GtkStyleContext *context;
	
	gtk_init(&argc, &argv);

	window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
	g_signal_connect(window, "delete_event", G_CALLBACK(delete_event), NULL);
	gtk_window_set_title(GTK_WINDOW(window), "Issue #2794: Combo Popup Transparency");
	gtk_window_resize(GTK_WINDOW(window), 500, 400);

	main_vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 10);
	gtk_container_add(GTK_CONTAINER(window), main_vbox);
	gtk_container_set_border_width(GTK_CONTAINER(main_vbox), 20);

	// Add instruction label
	label = gtk_label_new(
		"This demonstrates the transparency issue:\n\n"
		"1. The combo box below has a semi-transparent red background (alpha=0.5)\n"
		"2. Click the combo to open the popup menu\n"
		"3. WITHOUT the fix: The popup menu is also transparent and hard to read\n"
		"4. WITH the fix: The popup menu should be opaque (alpha=1.0) for readability\n\n"
		"Note: This demo shows the PROBLEM. The fix in SWT ensures\n"
		"that 'menu {background: rgba(..., 1.0);}' is applied via CSS."
	);
	gtk_label_set_line_wrap(GTK_LABEL(label), TRUE);
	gtk_box_pack_start(GTK_BOX(main_vbox), label, FALSE, FALSE, 0);

	// Create combo box
	combobox = gtk_combo_box_text_new();
	gtk_widget_set_size_request(combobox, -1, 40);

	gtk_combo_box_text_append(GTK_COMBO_BOX_TEXT(combobox), NULL, "Item 1 - Should be readable");
	gtk_combo_box_text_append(GTK_COMBO_BOX_TEXT(combobox), NULL, "Item 2 - Should be readable");
	gtk_combo_box_text_append(GTK_COMBO_BOX_TEXT(combobox), NULL, "Item 3 - Should be readable");
	gtk_combo_box_text_append(GTK_COMBO_BOX_TEXT(combobox), NULL, "Item 4 - Should be readable");
	gtk_combo_box_text_append(GTK_COMBO_BOX_TEXT(combobox), NULL, "Item 5 - Should be readable");

	gtk_box_pack_start(GTK_BOX(main_vbox), combobox, FALSE, FALSE, 0);

	// Apply CSS to set a transparent red background
	// This demonstrates the PROBLEM: both the combo AND menu become transparent
	css_provider = gtk_css_provider_new();
	const char *css_data = 
		"* {"
		"    background: rgba(255, 0, 0, 0.5);"  // 50% transparent red
		"}";
	
	gtk_css_provider_load_from_data(css_provider, css_data, -1, NULL);
	context = gtk_widget_get_style_context(combobox);
	gtk_style_context_add_provider(context, GTK_STYLE_PROVIDER(css_provider),
	                                GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);

	// To demonstrate the FIX, uncomment the following CSS which shows
	// how SWT fixes the issue by explicitly setting the menu to be opaque:
	/*
	const char *css_data_with_fix = 
		"* {"
		"    background: rgba(255, 0, 0, 0.5);"  // Transparent for combo
		"}"
		"menu {"
		"    background: rgba(255, 0, 0, 1.0);"  // Opaque for menu (THE FIX)
		"}";
	gtk_css_provider_load_from_data(css_provider, css_data_with_fix, -1, NULL);
	*/

	gtk_widget_show_all(window);

	gtk_main();

	g_object_unref(css_provider);
	return 0;
}

static gboolean delete_event(GtkWidget *widget, GdkEvent *event, gpointer data) {
	gtk_main_quit();
	return FALSE;
}
