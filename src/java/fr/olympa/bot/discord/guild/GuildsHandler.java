package fr.olympa.bot.discord.guild;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;

public class GuildsHandler {

	public static List<OlympaGuild> guilds = null;
	
	public static OlympaGuild getGuild(Guild guild) {
		return getGuild(guild.getIdLong());
	}

	public static OlympaGuild getGuild(long guildId) {
		return guilds.stream().filter(g -> guildId != 0 && g.getGuildId() == guildId).findFirst().orElse(null);
	}

	public static void updateGuild(OlympaGuild olympaGuild) {
		guilds.remove(getGuild(olympaGuild.getId()));
		guilds.add(olympaGuild);
	}
}
