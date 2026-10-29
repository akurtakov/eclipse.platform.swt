/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * SWT OpenGL snippet: draw to a GLCanvas without an OpenGL binding library,
 * calling OpenGL through the Foreign Function & Memory API
 *
 * Needs Java 22 or newer. Run with --enable-native-access=ALL-UNNAMED, and on
 * macOS also with -XstartOnFirstThread.
 *
 * The drawing needs OpenGL 2.0. It uses GLSL 1.50 and a vertex array object on
 * OpenGL 3.2 and later, which includes core profile contexts (Wayland, GTK4),
 * and GLSL 1.20 without a vertex array object below that, as on macOS, where
 * GLCanvas creates a legacy OpenGL 2.1 context.
 *
 * For a list of all SWT example snippets see
 * https://eclipse.dev/eclipse/swt/snippets/
 */
import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.opengl.*;
import org.eclipse.swt.widgets.*;

public class Snippet407 {

	static final int GL_VERSION = 0x1F02;
	static final int GL_COLOR_BUFFER_BIT = 0x4000, GL_TRIANGLES = 0x0004, GL_FLOAT = 0x1406;
	static final int GL_ARRAY_BUFFER = 0x8892, GL_STATIC_DRAW = 0x88E4;
	static final int GL_VERTEX_SHADER = 0x8B31, GL_FRAGMENT_SHADER = 0x8B30;
	static final int GL_COMPILE_STATUS = 0x8B81, GL_LINK_STATUS = 0x8B82;

	/*
	 * Finds OpenGL functions. Linux (glvnd) and macOS export them all from one
	 * library. Windows exports only OpenGL 1.1 from opengl32.dll; everything
	 * newer comes from wglGetProcAddress, which needs a current context.
	 */
	static class Loader {
		final Linker linker = Linker.nativeLinker();
		final SymbolLookup library;
		final MethodHandle wglGetProcAddress;

		Loader() {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("windows")) {
				library = SymbolLookup.libraryLookup("opengl32.dll", Arena.global());
				wglGetProcAddress = linker.downcallHandle(library.find("wglGetProcAddress").orElseThrow(), FunctionDescriptor.of(ADDRESS, ADDRESS));
			} else if (os.startsWith("mac")) {
				library = SymbolLookup.libraryLookup("/System/Library/Frameworks/OpenGL.framework/OpenGL", Arena.global());
				wglGetProcAddress = null;
			} else {
				library = openFirst("libOpenGL.so.0", "libGL.so.1");
				wglGetProcAddress = null;
			}
		}

		static SymbolLookup openFirst(String... names) {
			IllegalArgumentException failure = null;
			for (String name : names) {
				try {
					return SymbolLookup.libraryLookup(name, Arena.global());
				} catch (IllegalArgumentException e) {
					failure = e;
				}
			}
			throw failure;
		}

		MethodHandle get(String name, MemoryLayout result, MemoryLayout... args) {
			MemorySegment address = find(name);
			FunctionDescriptor descriptor = result == null ? FunctionDescriptor.ofVoid(args) : FunctionDescriptor.of(result, args);
			return linker.downcallHandle(address, descriptor);
		}

		MemorySegment find(String name) {
			if (wglGetProcAddress != null) {
				try (Arena arena = Arena.ofConfined()) {
					MemorySegment address = (MemorySegment) wglGetProcAddress.invokeExact(arena.allocateFrom(name));
					long value = address.address();
					/* Some drivers return 1, 2, 3 or -1 instead of NULL for functions they do not have */
					if (value != 0 && value != 1 && value != 2 && value != 3 && value != -1) return address;
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}
			return library.find(name).orElseThrow(() -> new UnsatisfiedLinkError(name));
		}
	}

	/* The OpenGL functions used here, looked up while the context of the canvas is current */
	static class GL {
		final MethodHandle glGetString, glViewport, glClearColor, glClear, glDrawArrays;
		final MethodHandle glCreateShader, glShaderSource, glCompileShader, glGetShaderiv;
		final MethodHandle glCreateProgram, glAttachShader, glBindAttribLocation, glLinkProgram, glGetProgramiv, glUseProgram;
		final MethodHandle glGetUniformLocation, glUniform1f;
		final MethodHandle glGenBuffers, glBindBuffer, glBufferData, glVertexAttribPointer, glEnableVertexAttribArray;
		final MethodHandle glGenVertexArrays, glBindVertexArray;
		final int major, minor;

		GL(Loader loader) throws Throwable {
			glGetString = loader.get("glGetString", ADDRESS, JAVA_INT);
			MemorySegment version = (MemorySegment) glGetString.invokeExact(GL_VERSION);
			String text = version.reinterpret(256).getString(0);
			Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(text);
			if (!matcher.find()) throw new IllegalStateException("Unknown OpenGL version: " + text);
			major = Integer.parseInt(matcher.group(1));
			minor = Integer.parseInt(matcher.group(2));
			if (major < 2) throw new IllegalStateException("OpenGL 2.0 or newer is required, found " + text);

			glViewport = loader.get("glViewport", null, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT);
			glClearColor = loader.get("glClearColor", null, JAVA_FLOAT, JAVA_FLOAT, JAVA_FLOAT, JAVA_FLOAT);
			glClear = loader.get("glClear", null, JAVA_INT);
			glDrawArrays = loader.get("glDrawArrays", null, JAVA_INT, JAVA_INT, JAVA_INT);
			glCreateShader = loader.get("glCreateShader", JAVA_INT, JAVA_INT);
			glShaderSource = loader.get("glShaderSource", null, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS);
			glCompileShader = loader.get("glCompileShader", null, JAVA_INT);
			glGetShaderiv = loader.get("glGetShaderiv", null, JAVA_INT, JAVA_INT, ADDRESS);
			glCreateProgram = loader.get("glCreateProgram", JAVA_INT);
			glAttachShader = loader.get("glAttachShader", null, JAVA_INT, JAVA_INT);
			glBindAttribLocation = loader.get("glBindAttribLocation", null, JAVA_INT, JAVA_INT, ADDRESS);
			glLinkProgram = loader.get("glLinkProgram", null, JAVA_INT);
			glGetProgramiv = loader.get("glGetProgramiv", null, JAVA_INT, JAVA_INT, ADDRESS);
			glUseProgram = loader.get("glUseProgram", null, JAVA_INT);
			glGetUniformLocation = loader.get("glGetUniformLocation", JAVA_INT, JAVA_INT, ADDRESS);
			glUniform1f = loader.get("glUniform1f", null, JAVA_INT, JAVA_FLOAT);
			glGenBuffers = loader.get("glGenBuffers", null, JAVA_INT, ADDRESS);
			glBindBuffer = loader.get("glBindBuffer", null, JAVA_INT, JAVA_INT);
			glBufferData = loader.get("glBufferData", null, JAVA_INT, JAVA_LONG, ADDRESS, JAVA_INT);
			glVertexAttribPointer = loader.get("glVertexAttribPointer", null, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_BYTE, JAVA_INT, ADDRESS);
			glEnableVertexAttribArray = loader.get("glEnableVertexAttribArray", null, JAVA_INT);
			boolean vertexArrays = atLeast(3, 0);
			glGenVertexArrays = vertexArrays ? loader.get("glGenVertexArrays", null, JAVA_INT, ADDRESS) : null;
			glBindVertexArray = vertexArrays ? loader.get("glBindVertexArray", null, JAVA_INT) : null;
		}

		boolean atLeast(int major, int minor) {
			return this.major > major || (this.major == major && this.minor >= minor);
		}
	}

	/* The shader sources without the #version line, which compile() adds */
	static final String VERTEX_SHADER = """
			IN vec2 position;
			IN vec3 color;
			uniform float angle;
			uniform float aspect;
			OUT vec3 vertexColor;
			void main() {
				float c = cos(angle), s = sin(angle);
				vec2 p = vec2(c * position.x - s * position.y, s * position.x + c * position.y);
				gl_Position = vec4(p.x / aspect, p.y, 0.0, 1.0);
				vertexColor = color;
			}
			""";
	static final String FRAGMENT_SHADER = """
			IN vec3 vertexColor;
			void main() {
				FRAG_COLOR = vec4(vertexColor, 1.0);
			}
			""";

	static GL gl;
	static int program, vertexArray, buffer, angleLocation, aspectLocation;
	static float angle;

	static int compile(int type, String source) throws Throwable {
		/* GLSL 1.50 for OpenGL 3.2 and later, which includes core profiles, GLSL 1.20 for OpenGL 2.x */
		boolean glsl150 = gl.atLeast(3, 2);
		String header;
		if (type == GL_VERTEX_SHADER) {
			header = glsl150 ? "#version 150\n#define IN in\n#define OUT out\n" : "#version 120\n#define IN attribute\n#define OUT varying\n";
		} else {
			header = glsl150 ? "#version 150\n#define IN in\nout vec4 fragColor;\n#define FRAG_COLOR fragColor\n" : "#version 120\n#define IN varying\n#define FRAG_COLOR gl_FragColor\n";
		}
		try (Arena arena = Arena.ofConfined()) {
			int shader = (int) gl.glCreateShader.invokeExact(type);
			/* glShaderSource takes an array of C strings; a null length array means they are NUL terminated */
			MemorySegment sources = arena.allocateFrom(ADDRESS, arena.allocateFrom(header + source));
			gl.glShaderSource.invokeExact(shader, 1, sources, MemorySegment.NULL);
			gl.glCompileShader.invokeExact(shader);
			MemorySegment status = arena.allocate(JAVA_INT);
			gl.glGetShaderiv.invokeExact(shader, GL_COMPILE_STATUS, status);
			if (status.get(JAVA_INT, 0) == 0) throw new IllegalStateException("shader does not compile");
			return shader;
		}
	}

	static void init() throws Throwable {
		gl = new GL(new Loader());
		try (Arena arena = Arena.ofConfined()) {
			program = (int) gl.glCreateProgram.invokeExact();
			gl.glAttachShader.invokeExact(program, compile(GL_VERTEX_SHADER, VERTEX_SHADER));
			gl.glAttachShader.invokeExact(program, compile(GL_FRAGMENT_SHADER, FRAGMENT_SHADER));
			gl.glBindAttribLocation.invokeExact(program, 0, arena.allocateFrom("position"));
			gl.glBindAttribLocation.invokeExact(program, 1, arena.allocateFrom("color"));
			gl.glLinkProgram.invokeExact(program);
			MemorySegment status = arena.allocate(JAVA_INT);
			gl.glGetProgramiv.invokeExact(program, GL_LINK_STATUS, status);
			if (status.get(JAVA_INT, 0) == 0) throw new IllegalStateException("program does not link");
			angleLocation = (int) gl.glGetUniformLocation.invokeExact(program, arena.allocateFrom("angle"));
			aspectLocation = (int) gl.glGetUniformLocation.invokeExact(program, arena.allocateFrom("aspect"));

			float[] vertices = {
				// x, y, r, g, b
				0.0f, 0.8f, 1, 0, 0,
				-0.7f, -0.4f, 0, 1, 0,
				0.7f, -0.4f, 0, 0, 1,
			};
			MemorySegment id = arena.allocate(JAVA_INT);
			/* A core profile only draws with a vertex array object bound; OpenGL 2.x has none */
			if (gl.glGenVertexArrays != null) {
				gl.glGenVertexArrays.invokeExact(1, id);
				vertexArray = id.get(JAVA_INT, 0);
				gl.glBindVertexArray.invokeExact(vertexArray);
			}
			gl.glGenBuffers.invokeExact(1, id);
			buffer = id.get(JAVA_INT, 0);
			gl.glBindBuffer.invokeExact(GL_ARRAY_BUFFER, buffer);
			MemorySegment data = arena.allocateFrom(JAVA_FLOAT, vertices);
			gl.glBufferData.invokeExact(GL_ARRAY_BUFFER, data.byteSize(), data, GL_STATIC_DRAW);
			int stride = 5 * Float.BYTES;
			/* The last argument is an offset into the buffer, passed as a pointer */
			gl.glVertexAttribPointer.invokeExact(0, 2, GL_FLOAT, (byte) 0, stride, MemorySegment.ofAddress(0));
			gl.glEnableVertexAttribArray.invokeExact(0);
			gl.glVertexAttribPointer.invokeExact(1, 3, GL_FLOAT, (byte) 0, stride, MemorySegment.ofAddress(2 * Float.BYTES));
			gl.glEnableVertexAttribArray.invokeExact(1);
		}
	}

	static void draw(GLCanvas canvas) {
		if (canvas.isDisposed()) return;
		canvas.setCurrent();
		Rectangle area = canvas.getClientArea();
		/* The framebuffer is in pixels, except on macOS, where GLCanvas is not a high resolution view */
		int zoom = "cocoa".equals(SWT.getPlatform()) ? 100 : canvas.getMonitor().getZoom();
		try {
			gl.glViewport.invokeExact(0, 0, area.width * zoom / 100, area.height * zoom / 100);
			gl.glClearColor.invokeExact(0.3f, 0.5f, 0.8f, 1.0f);
			gl.glClear.invokeExact(GL_COLOR_BUFFER_BIT);
			gl.glUseProgram.invokeExact(program);
			gl.glUniform1f.invokeExact(angleLocation, angle);
			gl.glUniform1f.invokeExact(aspectLocation, area.height == 0 ? 1f : (float) area.width / area.height);
			if (gl.glBindVertexArray != null) gl.glBindVertexArray.invokeExact(vertexArray);
			gl.glBindBuffer.invokeExact(GL_ARRAY_BUFFER, buffer);
			gl.glDrawArrays.invokeExact(GL_TRIANGLES, 0, 3);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		canvas.swapBuffers();
	}

	public static void main(String[] args) throws Throwable {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("SWT GLCanvas with FFM");
		shell.setLayout(new FillLayout());
		GLData data = new GLData();
		data.doubleBuffer = true;
		GLCanvas canvas = new GLCanvas(shell, SWT.NONE, data);

		/* On Windows the functions can only be looked up while a context is current */
		canvas.setCurrent();
		init();
		System.out.println("OpenGL " + gl.major + "." + gl.minor + ", GLSL " + (gl.atLeast(3, 2) ? "1.50" : "1.20") + (gl.glGenVertexArrays != null ? " with" : " without") + " vertex array object");

		canvas.addListener(SWT.Paint, e -> draw(canvas));
		Runnable animate = new Runnable() {
			@Override
			public void run() {
				if (canvas.isDisposed()) return;
				angle += 0.02f;
				draw(canvas);
				display.timerExec(16, this);
			}
		};
		display.timerExec(16, animate);

		shell.setSize(640, 480);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}
