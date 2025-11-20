# Issue #2794: Combo Popup Transparency

This directory contains test materials for Issue #2794, which addresses a problem where setting a Combo widget's background color with transparency (alpha < 1.0) caused the popup menu to also be transparent, making it difficult or impossible to read.

## Files

### 1. JUnit Test: Test_org_eclipse_swt_widgets_Combo.java

Two new test methods were added to the existing test suite:

#### `test_setBackgroundAlpha_PopupNotTransparent()`
- **Purpose**: Validates that a Combo widget can accept a transparent background color (alpha < 1.0) without causing the popup menu to become transparent.
- **Behavior**: Sets a background color with 50% transparency (alpha = 128) and verifies the color is accepted.
- **Implementation Note**: The test validates the Java API behavior. The actual fix ensures the CSS rule `menu {background: rgba(..., 1.0);}` is applied internally.

#### `test_setBackgroundOpaque_PopupOpaque()`
- **Purpose**: Complements the above test by validating fully opaque backgrounds work correctly.
- **Behavior**: Sets a fully opaque background color (alpha = 255) and verifies it persists.

**Location**: `/tests/org.eclipse.swt.tests/JUnit Tests/org/eclipse/swt/tests/junit/Test_org_eclipse_swt_widgets_Combo.java`

**To Run**:
```bash
cd /path/to/eclipse.platform.swt
mvn test -Dtest=Test_org_eclipse_swt_widgets_Combo#test_setBackgroundAlpha_PopupNotTransparent
```

### 2. GTK Native C Snippet: Issue_2794_ComboPopupTransparency.c

A standalone C program that demonstrates the transparency problem using pure GTK3 APIs.

#### What it demonstrates:
- Creates a GtkComboBoxText widget
- Applies CSS with a semi-transparent red background (alpha = 0.5)
- Shows how the transparency affects the popup menu

#### Without the fix:
- Both the combo box AND the popup menu become transparent
- Text in the popup menu is hard to read over busy backgrounds

#### With the fix (commented code):
- The combo box has transparency
- The popup menu is forced to be opaque (alpha = 1.0) for readability

**To Compile and Run**:
```bash
cd /path/to/eclipse.platform.swt/tests/org.eclipse.swt.tests.gtk/ManualNativeCTests/BugSnippets

# Using the shell script
./Issue_2794_ComboPopupTransparency.sh

# Or manually
gcc -o combo_transparency Issue_2794_ComboPopupTransparency.c \
    `pkg-config --cflags --libs gtk+-3.0` -Wall
./combo_transparency
```

#### Expected behavior:
1. A window appears with instructions
2. A combo box is displayed with a semi-transparent red background
3. Click the combo box to open the popup
4. **WITHOUT the fix**: The popup menu is transparent and hard to read
5. **WITH the fix** (uncomment the fix code): The popup menu is opaque

### 3. Shell Script: Issue_2794_ComboPopupTransparency.sh

Convenience script to compile and run the C snippet.

## The Fix

The fix in SWT's `Combo.java` (GTK implementation) is in the `updateCss()` method:

```java
// Create a separate GdkRGBA for the menu with alpha forced to 1.0
GdkRGBA menuBackground = new GdkRGBA();
menuBackground.red = background.red;
menuBackground.green = background.green;
menuBackground.blue = background.blue;
menuBackground.alpha = 1.0;  // Force opaque

// Apply to CSS
css.append("menu {background: " + menuColorString + ";}\n");
```

This ensures that even if the developer sets a transparent background on the Combo widget, the popup menu itself will always be fully opaque, maintaining readability.

## Why This Matters

Transparent popup menus are generally undesirable because:
1. Text becomes difficult or impossible to read
2. The UI appears broken or unprofessional
3. No legitimate use case exists for transparent dropdown menus
4. Users expect dropdown menus to be opaque for clarity

Even "light" transparency (alpha > 0.85) is problematic and therefore the fix enforces alpha = 1.0 unconditionally.

## Related

- **GitHub Issue**: https://github.com/eclipse-platform/eclipse.platform.swt/issues/2794
- **Commit**: a526b6e - "[Gtk] Ignore transparency when setting Combo popup color"
- **Affected Platform**: GTK (both GTK3 and GTK4)
