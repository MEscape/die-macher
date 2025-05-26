package com.die_macher.common.util;

import com.die_macher.common.util.HexDump;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class HexDumpTest {
	@Test
	void testHexDumpOutputFormat() {
		byte[] data = new byte[] {
				0x00, 0x1f, 0x7f, 0x23, 0x41, 0x41, 0x41, 0x41,
				0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42
		};

		String hexDump = HexDump.hexDump(data);

		// Basic format checks
		assertNotNull(hexDump);
		String[] lines = hexDump.split("\n");
		assertEquals(1, lines.length, "Expected one line for 16 bytes");

		String line = lines[0];
		assertTrue(line.startsWith("00000: "), "Line should start with offset");
		assertTrue(line.contains("00 1F 7F 23"), "Should contain hex values");
		assertTrue(line.endsWith("...#AAAABBBBBBBB"), "Should end with ASCII representation");
	}

	@Test
	void testHexDumpOutputFormatWithIncompleteLine() {
		byte[] data = new byte[] {
				0x03, 0x00, 0x00, 0x23, 0x41, 0x41, 0x41, 0x41,
				0x42, 0x42, 0x42, 0x42, 0x42, 0x42
		};

		String hexDump = HexDump.hexDump(data);

		// Basic format checks
		assertNotNull(hexDump);
		String[] lines = hexDump.split("\n");
		assertEquals(1, lines.length, "Expected one line for 16 bytes");

		String line = lines[0];
		assertTrue(line.startsWith("00000: "), "Line should start with offset");
		assertTrue(line.contains("03 00 00 23 41 41 41 41 42 42 42 42 42 42      "), "Should contain hex values");
		assertTrue(line.endsWith("...#AAAABBBBBB"), "Should end with ASCII representation");
	}

	@Test
	void testPartialLastLine() {
		byte[] data = new byte[18];
		for (int i = 0; i < 18; i++) {
			data[i] = (byte) i;
		}

		String hexDump = HexDump.hexDump(data);
		String[] lines = hexDump.split("\n");

		// 18 bytes should be 2 lines
		assertEquals(2, lines.length);
		assertTrue(lines[1].startsWith("00010:"), "Second line should start with correct offset");
	}

	@Test
	void testPrivateConstructorThrowsException() throws Exception {
		Constructor<HexDump> constructor = HexDump.class.getDeclaredConstructor();
		constructor.setAccessible(true); // bypass private

		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

		Throwable cause = thrown.getCause();
		assertInstanceOf(IllegalStateException.class, cause);
		assertEquals("Utility class", cause.getMessage());
	}
}
