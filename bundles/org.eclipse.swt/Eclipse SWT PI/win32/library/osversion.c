/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

/* Note: This file was auto-generated by org.eclipse.swt.tools.internal.JNIGenerator */
/* DO NOT EDIT - your changes will be lost. */

#include "swt.h"
#include "osversion_structs.h"
#include "osversion_stats.h"

#ifndef OsVersion_NATIVE
#define OsVersion_NATIVE(func) Java_org_eclipse_swt_internal_win32_version_OsVersion_##func
#endif

#ifdef _WIN32
  /* Many methods don't use their 'env' and 'that' arguments */
  #pragma warning (disable: 4100)
#endif

#ifndef NO_OSVERSIONINFOEX_1sizeof
JNIEXPORT jint JNICALL OsVersion_NATIVE(OSVERSIONINFOEX_1sizeof)
	(JNIEnv *env, jclass that)
{
	jint rc = 0;
	OsVersion_NATIVE_ENTER(env, that, OSVERSIONINFOEX_1sizeof_FUNC);
	rc = (jint)OSVERSIONINFOEX_sizeof();
	OsVersion_NATIVE_EXIT(env, that, OSVERSIONINFOEX_1sizeof_FUNC);
	return rc;
}
#endif

#ifndef NO_RtlGetVersion
JNIEXPORT jint JNICALL OsVersion_NATIVE(RtlGetVersion)
	(JNIEnv *env, jclass that, jobject arg0)
{
	OSVERSIONINFOEX _arg0, *lparg0=NULL;
	jint rc = 0;
	OsVersion_NATIVE_ENTER(env, that, RtlGetVersion_FUNC);
	if (arg0) if ((lparg0 = getOSVERSIONINFOEXFields(env, arg0, &_arg0)) == NULL) goto fail;
/*
	rc = (jint)RtlGetVersion(lparg0);
*/
	{
		OsVersion_LOAD_FUNCTION(fp, RtlGetVersion)
		if (fp) {
			rc = (jint)((jint (CALLING_CONVENTION*)(OSVERSIONINFOEX *))fp)(lparg0);
		}
	}
fail:
	if (arg0 && lparg0) setOSVERSIONINFOEXFields(env, arg0, lparg0);
	OsVersion_NATIVE_EXIT(env, that, RtlGetVersion_FUNC);
	return rc;
}
#endif

