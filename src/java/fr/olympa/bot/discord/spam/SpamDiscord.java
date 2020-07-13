package fr.olympa.bot.discord.spam;

import java.util.HashMap;
import java.util.Map;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
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
		if (data.containsKey(member))
			i = data.get(member);
		data.put(member, ++i);
		if (i > 2) {
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(member.getGuild());
			TextChannel channel = olympaGuild.getLogChannel();
			// TODO better mute
			if (channel != null)
				channel.sendMessage("$mute " + channel.getGuild().getMemberById(userId).getIdLong() + " 1h Spam Mention").queue();
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
