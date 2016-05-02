package datou.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MyFirstTest {
	@Test
	public void testAppAdd() {
		App app = new App();
		String s = app.add("Hello", "World");
		assertEquals("HelloWorld ", s);
	}
}
