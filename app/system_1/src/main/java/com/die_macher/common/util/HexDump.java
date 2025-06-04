package com.die_macher.common.util;

public class HexDump {

  private HexDump() {
    throw new IllegalStateException("Utility class");
  }

  public static String hexDump(byte[] data) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < data.length; i += 16) {
      sb.append(String.format("%05X: ", i));

      // Hex section
      for (int j = 0; j < 16; j++) {
        if (i + j < data.length) {
          sb.append(String.format("%02X ", data[i + j]));
        } else {
          sb.append("   "); // padding for incomplete line
        }
      }

      sb.append(" ");

      // ASCII section
      for (int j = 0; j < 16 && i + j < data.length; j++) {
        byte b = data[i + j];
        sb.append((b >= 32 && b <= 126) ? (char) b : '.');
      }

      sb.append("\n");
    }

    return sb.toString();
  }
}
