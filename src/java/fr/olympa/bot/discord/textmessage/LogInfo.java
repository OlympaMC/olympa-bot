package fr.olympa.bot.discord.textmessage;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Member;

public class LogInfo<T extends Member> {

	private Function<T, String> value;

	public LogInfo(Function<T, String> value) {
		this.value = value;
	}

	public String getValue(T messageCache) {
		return value.apply(messageCache);
	}
}
