package fr.olympa.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SwearHandler {
	private static List<Pattern> regexSwear = new ArrayList<>();

	public static List<Pattern> getSwearHandler() {
		return regexSwear;
	}

	public SwearHandler(List<String> swearList) {
		for (String swear : swearList) {
			String swears = "";
			String b = "\\b";
			if (swear.startsWith("|")) {
				b = "";
				swear = swear.substring(1);
			}
			for (char s : swear.toCharArray()) {
				swears += s + "+(\\W|\\d|_)*";
			}
			regexSwear.add(Pattern.compile("(?iu)" + b + "(" + swears + ")" + b));
		}
	}
}
