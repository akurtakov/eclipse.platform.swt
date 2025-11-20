#!/bin/bash
#*******************************************************************************
# Copyright (c) 2025 Contributors to the Eclipse Foundation
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#*******************************************************************************

# Compile and run the Issue #2794 ComboBox transparency demo
# This script compiles for both GTK3 and GTK4 (if available)

echo "Building GTK3 version..."
if pkg-config --exists gtk+-3.0; then
    gcc -o combo_transparency_gtk3 Issue_2794_ComboPopupTransparency.c \
        `pkg-config --cflags --libs gtk+-3.0` -Wall
    if [ $? -eq 0 ]; then
        echo "GTK3 build successful. Running..."
        ./combo_transparency_gtk3
    else
        echo "GTK3 build failed"
    fi
else
    echo "GTK3 not found"
fi

# Cleanup
rm -f combo_transparency_gtk3 combo_transparency_gtk4
