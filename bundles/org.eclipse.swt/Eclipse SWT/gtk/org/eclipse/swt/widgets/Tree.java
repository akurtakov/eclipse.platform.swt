@Override
int gtk_gesture_press_event(long gesture, int n_press, double x, double y, long event) {
    int result = super.gtk_gesture_press_event(gesture, n_press, x, y, event);

    // Fix: Ensure double-click always focuses and selects the item under the mouse
    if (n_press == 2) {
        if (!isFocusControl()) setFocus();

        long[] path = new long[1];
        if (GTK.gtk_tree_view_get_path_at_pos(handle, (int)x, (int)y, path, null, null, null)) {
            if (path[0] != 0) {
                long selection = GTK.gtk_tree_view_get_selection(handle);
                OS.g_signal_handlers_block_matched(selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
                GTK.gtk_tree_view_set_cursor(handle, path[0], 0, false);
                OS.g_signal_handlers_unblock_matched(selection, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
                GTK.gtk_tree_path_free(path[0]);
            }
        }
    }

    if (n_press == 2 && rowActivated) {
        sendTreeDefaultSelection();
        rowActivated = false;
    }

    return result;
}