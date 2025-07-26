package mcech.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class Base64Test {
	@Test
	public void encode() {
		String encoded = Base64.encode(plain_.getBytes(UTF_8));
		assertEquals(encoded_, encoded);
	}
	
	@Test
	public void decode() {
		String plain = new String(Base64.decode(encoded_), UTF_8);
		assertEquals(plain_, plain);
	}
	
	@Test
	public void nonsense() {
		String nonsense = "!$%&?";
		assertThrows(IllegalArgumentException.class, () -> Base64.decode(nonsense));
	}
	
	private static final String plain_ = "Polyfon zwitschernd aßen Mäxchens Vögel Rüben, Joghurt und Quark";
	private static final String encoded_ = "UG9seWZvbiB6d2l0c2NoZXJuZCBhw59lbiBNw6R4Y2hlbnMgVsO2Z2VsIFLDvGJlbiwgSm9naHVydCB1bmQgUXVhcms";
}
