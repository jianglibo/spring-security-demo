package com.example.springsecurity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class LangTest {
	

	@Test
	void tStartswithemptystring() {
		Assertions.assertThat("hello".startsWith("")).isTrue();
	}
}
