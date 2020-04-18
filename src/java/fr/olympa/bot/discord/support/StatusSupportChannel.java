package fr.olympa.bot.discord.support;

import java.util.Arrays;

public enum StatusSupportChannel {

	OPEN(1),
	WAITING(2),
	PROGRESS(3),
	CLOSE(4);

	public static StatusSupportChannel get(int id) {
		return Arrays.stream(StatusSupportChannel.values()).filter(s -> s.getId() == id).findFirst().orElse(null);
	}

	int id;

	private StatusSupportChannel(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
