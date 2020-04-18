package fr.olympa.bot.discord.support;

import java.util.Arrays;
import java.util.List;

import com.vdurmont.emoji.EmojiParser;

public enum AutoResponse {

	FORUM("Forum", "📚", "Tu dois utiliser le forum pour ce genre de demande."),
	NOTENOUGH("Pas assez de détails", "🤷", "Merci de fournir plus de détails, tu dois nous donner le maximum d'information (pseudo Minecraft et/ou capture d'écran ...)"),
	CLOSE("Fermer la demande", "🚪", "La demande est terminé.");

	public static AutoResponse get(String emoji) {
		return Arrays.stream(AutoResponse.values()).filter(resp -> resp.getEmoji().equals(emoji)).findFirst().orElse(null);
	}

	String name;
	String emoji;

	String msg;

	private AutoResponse(String name, String emoji, String msg) {
		this.name = name;
		this.emoji = emoji;
		this.msg = msg;
	}

	public String getEmoji() {
		List<String> roleEmote = EmojiParser.extractEmojis(emoji);
		return roleEmote.get(0);
	}

	public String getMsg() {
		return msg;
	}

	public String getName() {
		return name;
	}
}
