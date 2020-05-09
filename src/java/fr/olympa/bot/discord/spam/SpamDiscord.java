package fr.olympa.bot.discord.spam;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class SpamDiscord {

	final long userId;
	Map<Member, Integer> data = new HashMap<>();

	public SpamDiscord(long userId) {
		this.userId = userId;
	}

	public boolean addMention(Member member) {
		int i = 0;
		if (data.containsKey(member)) {
			i = data.get(member);
		}
		data.put(member, ++i);
		if (i > 2) {
			TextChannel channel = member.getGuild().getTextChannelById(558356359805009931L);
			if (channel != null) {
				channel.sendMessage("?mute " + member.getUser().getAsTag() + " 1h Spam Mention").queue();
			}
		}
		return i > 1;
	}

	public void delMention(Member member) {
		data.remove(member);
	}

	public Map<Member, Integer> getData() {
		return data;
	}

	public Integer getData(Member member) {
		return data.get(member);
	}

	public long getUserId() {
		return userId;
	}

	public void remove(Member member) {
		data.remove(member);
	}
}
