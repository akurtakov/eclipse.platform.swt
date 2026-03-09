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
 *     Red Hat Inc. - Bug 462631
 *******************************************************************************/
package org.eclipse.swt.tests.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Automated Test Suite for class {@link org.eclipse.swt.SWT}.
 */
public class Test_org_eclipse_swt_SWT {

	@Test
	public void test_errorI() {
		// Test that we throw the expected kinds of errors for the given error types.
		assertThrows(IllegalArgumentException.class, () -> SWT.error(SWT.ERROR_NULL_ARGUMENT),
				"did not correctly throw exception for ERROR_NULL_ARGUMENT");
		assertThrows(SWTException.class, () -> SWT.error(SWT.ERROR_FAILED_EXEC),
				"did not correctly throw exception for ERROR_FAILED_EXEC");
		assertThrows(SWTError.class, () -> SWT.error(SWT.ERROR_NO_HANDLES),
				"did not correctly throw exception for ERROR_NO_HANDLES");
		assertThrows(SWTError.class, () -> SWT.error(-1),
				"did not correctly throw exception for error(-1)");
	}

	@Test
	public void test_errorILjava_lang_Throwable() {
		// Test that the causing throwable is filled in.
		Throwable cause = new RuntimeException("Just for testing");
		SWTException e1 = assertThrows(SWTException.class, () -> SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT, cause));

		assertTrue(e1.throwable == cause, "did not correctly throw exception for ERROR_UNSUPPORTED_FORMAT");
		SWTError e2 = assertThrows(SWTError.class, () -> SWT.error(SWT.ERROR_NOT_IMPLEMENTED, cause));
		assertTrue(e2.throwable == cause, "did not correctly throw exception for ERROR_NOT_IMPLEMENTED");
		SWTError e3 = assertThrows(SWTError.class, () -> SWT.error(-1, cause));
		assertTrue(e3.throwable == cause, "did not correctly throw exception for error(-1)");
	}

	@Test
	public void test_getMessageLjava_lang_String() {
		assertThrows(IllegalArgumentException.class, () -> SWT.getMessage(null),
				"did not correctly throw exception with null argument");
		assertNotNull(SWT.getMessage("SWT_Yes"));
		assertEquals(
				"_NOT_FOUND_IN_PROPERTIES_", SWT.getMessage("_NOT_FOUND_IN_PROPERTIES_"),
				"invalid key did not return as itself");

	}

	private String pathFromClass(Class<?> classValue) {
		URL location = classValue.getProtectionDomain().getCodeSource().getLocation();
		String url = location.toString();
		assertTrue(url.startsWith("file:"), "Expected a file: URL, got: " + url);
		try {
			return Paths.get(location.toURI()).toAbsolutePath().toString().replace('\\', '/');
		} catch (URISyntaxException e) {
			throw new AssertionError("Cannot convert URL to path: " + url, e);
		}
	}

	private List<String> signersFromClass(Class<?> classValue) {
		List<String> result = new ArrayList<>();

		CodeSigner[] signers = classValue.getProtectionDomain().getCodeSource().getCodeSigners();
		if (signers == null) {
			return result;
		}

		for (CodeSigner signer : signers) {
			Certificate cert = signer.getSignerCertPath().getCertificates().get(0);
			if (!(cert instanceof X509Certificate x509cert)) {
				continue;
			}

			result.add(x509cert.getSubjectX500Principal().getName());
		}

		return result;
	}

	/**
	 * Returns the path of the SWT platform JAR matching the current OS and architecture,
	 * by iterating all Library.class resources and checking each JAR's manifest for
	 * SWT-OS/SWT-Arch attributes. Falls back to SWT.class's JAR if no matching entry is
	 * found (e.g. single-JAR deployments without manifest attributes).
	 * Returns {@code null} when running in a development environment (class files, not JARs).
	 * <p>
	 * Note: the OS/arch mapping intentionally mirrors {@code Library.os()} and
	 * {@code Library.arch()} from the internal package; those methods are
	 * package-private and therefore cannot be called from test code.
	 * </p>
	 */
	private String pathFromPlatformSwtJar() {
		// Determine the current platform identifiers (mirrors Library.os() / Library.arch())
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
		if ("amd64".equals(osArch)) osArch = "x86_64"; //$NON-NLS-1$ //$NON-NLS-2$
		final String swtOs;
		if ("Linux".equals(osName)) swtOs = "linux"; //$NON-NLS-1$ //$NON-NLS-2$
		else if ("Mac OS X".equals(osName)) swtOs = "macosx"; //$NON-NLS-1$ //$NON-NLS-2$
		else swtOs = "win32"; //$NON-NLS-1$
		final String swtArch = osArch;

		try {
			Enumeration<URL> urls = SWT.class.getClassLoader().getResources("org/eclipse/swt/internal/Library.class"); //$NON-NLS-1$
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (!"jar".equals(url.getProtocol())) { //$NON-NLS-1$
					return null; // development environment (class files, not JARs)
				}
				URLConnection conn = url.openConnection();
				if (conn instanceof JarURLConnection jc) {
					Attributes attrs = jc.getMainAttributes();
					if (swtOs.equals(attrs.getValue("SWT-OS")) && swtArch.equals(attrs.getValue("SWT-Arch"))) { //$NON-NLS-1$ //$NON-NLS-2$
						URL jarFileUrl = jc.getJarFileURL();
						return Paths.get(jarFileUrl.toURI()).toAbsolutePath().toString().replace('\\', '/');
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			// fall through to default
		}
		// Fall back: single-JAR deployment or no platform-specific manifest attributes
		return pathFromClass(SWT.class);
	}

	/**
	 * Returns the code-signer names from the JAR at {@code jarPath} by reading
	 * the Library.class entry (which triggers JAR signature verification).
	 */
	private List<String> signersFromJarPath(String jarPath) {
		List<String> result = new ArrayList<>();
		try (JarFile jarFile = new JarFile(new java.io.File(jarPath), true)) {
			JarEntry entry = jarFile.getJarEntry("org/eclipse/swt/internal/Library.class"); //$NON-NLS-1$
			if (entry != null) {
				// Must read the entry bytes to trigger JAR signature verification
				try (java.io.InputStream is = jarFile.getInputStream(entry)) {
					is.readAllBytes();
				}
				CodeSigner[] signers = entry.getCodeSigners();
				if (signers != null) {
					for (CodeSigner signer : signers) {
						Certificate cert = signer.getSignerCertPath().getCertificates().get(0);
						if (cert instanceof X509Certificate x509) {
							result.add(x509.getSubjectX500Principal().getName());
						}
					}
				}
			}
		} catch (IOException e) {
			// If the JAR cannot be read or verified, treat it as unsigned.
			// This is the conservative default for test validation purposes.
		}
		return result;
	}

	/**
	 * Ensure that SWT being tested was built together with the tests. This
	 * is a test designed for build scripts, such as the one that checks
	 * GitHub Pull Requests.
	 */
	@Test
	@DisabledIfSystemProperty(named = "org.eclipse.swt.tests.junit.disable.test_isLocal", matches = "true")
	public void test_isLocal() {
		// When all platform binary JARs are on the classpath (e.g. in a local
		// multi-platform build), SWT.class may be loaded from a JAR for a
		// different platform (e.g. aarch64 on an x86_64 machine). Use the JAR
		// whose manifest identifies it as the current platform's fragment instead.
		String swtPath = pathFromPlatformSwtJar();
		if (swtPath == null) {
			return; // development environment (class files), nothing to check
		}
		String tstPath = pathFromClass(Test_org_eclipse_swt_SWT.class);
		List<String> swtSigners = signersFromJarPath(swtPath);

		// If SWT is signed by Eclipse, that's a good sign that it wasn't built locally
		for (String signer : swtSigners) {
			if (signer.toLowerCase().contains("cn=eclipse.org")) {
				fail("Tested SWT is signed by Eclipse: (SWT='" + swtPath + "' signer='" + signer + "')");
			}
		}

		// It's hard to devise a condition that would hold for all possible
		// locations of compiled classes. For example, my own project uses
		// output directories different from what Eclipse does by default.

		// SWT shouldn't be published in maven. Note that SWT's local build
		// may still be cached in maven. We observed the following paths:
		// GitHub, bad:
		// $HOME/.m2/repository/p2/osgi/bundle/org.eclipse.swt.gtk.linux.x86_64/3.121.0.v20220701-1002/org.eclipse.swt.gtk.linux.x86_64-3.121.0.v20220701-1002.jar
		// Jenkins, good:
		// $HOME/.m2/repository/org/eclipse/swt/org.eclipse.swt.gtk.linux.x86_64/3.121.0-SNAPSHOT/org.eclipse.swt.gtk.linux.x86_64-3.121.0-SNAPSHOT.jar
		// The difference here is '/p2/osgi'.
		if (swtPath.contains("/.m2/repository/p2/osgi/")) {
			fail("Tested SWT path is on maven OSGI: (SWT='" + swtPath + "' Test='" + tstPath + "'");
		}
	}

	@Test
	public void test_getPlatform() {
		// Can't test the list of platforms, since this may change,
		// so just test to see it returns something.
		assertNotNull(SWT.getPlatform(), "returned null platform name");
	}

	@Test
	public void test_getVersion() {
		// Test that the version number which is returned is reasonable.
		int ver = SWT.getVersion();
		assertTrue(ver > 0 && ver < 1000000, "unreasonable value returned:" + ver);
	}
}
