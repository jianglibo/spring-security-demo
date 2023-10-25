package com.example.springsecurity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

public class LangTest {

	@Test
	void tStartswithemptystring() {
		Assertions.assertThat("hello".startsWith("")).isTrue();
	}

	/**
	 * match regex pattern in string and replace one by one
	 */
	@Test
	void matchRegexPatternInStringAndReplaceOneByOne() {
		Pattern ptn = Pattern.compile("\\{\\{--(.*?)--\\}\\}");
		String str = "This is a {{--authentication.filter.matcher.description--}}, {{--another.match--}}";
		Matcher matcher = ptn.matcher(str);
		StringBuffer sb = new StringBuffer();
		int lastEnd = 0;
		while (matcher.find()) {
			int start = matcher.start();
			String messagekey = matcher.group(1);
			sb.append(str.substring(lastEnd, start));
			sb.append(messagekey.toUpperCase());
			lastEnd = matcher.end();
		}
		if (lastEnd < str.length()) {
			sb.append(str.substring(lastEnd));
		}

		Assertions.assertThat(sb.toString())
				.isEqualTo("This is a AUTHENTICATION.FILTER.MATCHER.DESCRIPTION, ANOTHER.MATCH");

	}

	@Test
	void tp(){
		PathPattern pathPattern = new PathPatternParser().parse("/*/custom-login-page");
		PathPattern pathPattern1 = new PathPatternParser().parse("/custom-login-page");
		PathPattern pt = pathPattern.combine(pathPattern1);
		PathContainer pc = PathContainer.parsePath("/skipcsrf/custom-login-page");
		Assertions.assertThat(pt.matches(pc)).isTrue();
	}

}
