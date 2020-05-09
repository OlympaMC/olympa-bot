package fr.olympa.bot.discord.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum NumberEmoji {

	ONE("1️⃣", 1),
	TWO("2️⃣", 2),
	TREE("3️⃣", 3),
	FOUR("4️⃣", 4),
	FIVE("5️⃣", 5),
	SIX("6️⃣", 6),
	SEVEN("7️⃣", 7),
	EIGHT("8️⃣", 8),
	NINE("9️⃣", 9),
	TEN("🔟", 10);

	public static List<String> get() {
		return Arrays.stream(NumberEmoji.values()).map(NumberEmoji::getEmoji).collect(Collectors.toList());
	}

	public static NumberEmoji get(int id) {
		return Arrays.stream(NumberEmoji.values()).filter(e -> e.getId() == id).findFirst().orElse(null);
	}

	public static String parseNumber(String description) {
		int i = NumberEmoji.values().length;
		NumberEmoji nd = NumberEmoji.get(1);
		while (i > 0 && description.contains("%ne")) {
			description = description.replaceFirst("%ne", nd.getEmoji() + " ");
			nd = nd.getNext();
			i--;
		}
		description.replace("%ne", "");
		return description;
	}

	String emoji;

	int id;

	private NumberEmoji(String emoji, int id) {
		this.emoji = emoji;
		this.id = id;
	}

	public String getEmoji() {
		return emoji;
	}

	public int getId() {
		return id;
	}

	public NumberEmoji getNext() {
		return get(getId() + 1);
	}
}
