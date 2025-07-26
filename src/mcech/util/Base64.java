package mcech.util;

public class Base64 {
	public static String encode(byte[] b) {
		StringBuilder output = new StringBuilder();
		
		for (int i = 0; i < b.length - 2; i += 3) {
			int byte1 = b[i] & 0xFF;
			int byte2 = b[i + 1] & 0xFF;
			int byte3 = b[i + 2] & 0xFF;
			output.append(ENCODING_TABLE[(byte1 >> 2) & 0x3F]);
			output.append(ENCODING_TABLE[((byte1 << 4) | (byte2 >> 4)) & 0x3F]);
			output.append(ENCODING_TABLE[((byte2 << 2) | (byte3 >> 6)) & 0x3F]);
			output.append(ENCODING_TABLE[byte3 & 0x3F]);
		}
		switch (b.length % 3) {
			case 1 -> {
				int byte1 = b[b.length - 1] & 0xFF;
				output.append(ENCODING_TABLE[(byte1 >> 2) & 0x3F]);
				output.append(ENCODING_TABLE[(byte1 << 4) & 0x3F]);
			}
			case 2 -> {
				int byte1 = b[b.length - 2] & 0xFF;
				int byte2 = b[b.length - 1] & 0xFF;
				output.append(ENCODING_TABLE[(byte1 >> 2) & 0x3F]);
				output.append(ENCODING_TABLE[((byte1 << 4) | (byte2 >> 4)) & 0x3F]);
				output.append(ENCODING_TABLE[(byte2 << 2) & 0x3F]);
			}
		}
		
		return output.toString();
	}
	
	public static byte[] decode(String str) {
		if (!str.matches("^[A-Za-z0-9+/]*={0,2}$")) {
			throw new IllegalArgumentException();
		}
		
		int len = str.length();
		if (str.charAt(len - 1) == '=') {
			--len;
		}
		byte[] output = new byte[3 * len / 4];
		int index = 0;
		for (int i = 0; i < len - 3; i += 4) {
			int char1 = DECODING_TABLE[str.charAt(i)];
			int char2 = DECODING_TABLE[str.charAt(i + 1)];
			int char3 = DECODING_TABLE[str.charAt(i + 2)];
			int char4 = DECODING_TABLE[str.charAt(i + 3)];
			output[index++] = (byte) ((char1 << 2) | (char2 >> 4));
			output[index++] = (byte) ((char2 << 4) | (char3 >> 2));
			output[index++] = (byte) ((char3 << 6) | char4);
		}
		switch (len % 4) {
			case 1 -> {
				throw new IllegalArgumentException();
			}
			case 2 -> {
				int char1 = DECODING_TABLE[str.charAt(len - 2)];
				int char2 = DECODING_TABLE[str.charAt(len - 1)];
				output[index++] = (byte) ((char1 << 2) | (char2 >> 4));
			}
			case 3 -> {
				int char1 = DECODING_TABLE[str.charAt(len - 3)];
				int char2 = DECODING_TABLE[str.charAt(len - 2)];
				int char3 = DECODING_TABLE[str.charAt(len - 1)];
				output[index++] = (byte) ((char1 << 2) | (char2 >> 4));
				output[index++] = (byte) ((char2 << 4) | (char3 >> 2));
			}
		}
		return output;
	}
	
	private static final char[] ENCODING_TABLE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '+', '/'
	};
	
	private static final int[] DECODING_TABLE = {
		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 62,  0,  0,  0, 63,
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61,  0,  0,  0,  0,  0,  0,
		 0,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,  0,  0,  0,  0,  0,
		 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
		41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,  0,  0,  0,  0,  0
	};
}
