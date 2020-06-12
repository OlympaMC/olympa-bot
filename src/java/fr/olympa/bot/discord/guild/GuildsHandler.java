package fr.olympa.bot.discord.guild;

import java.util.List;

import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.entities.Guild;

public class GuildsHandler {

	public static List<OlympaGuild> guilds = null;
	
	public static OlympaGuild getOlympaGuild(Guild guild) {
		return getOlympaGuildByDiscordId(guild.getIdLong());
	}

	public static OlympaGuild getOlympaGuildByDiscordId(long discordGuildId) {
		return guilds.stream().filter(g -> discordGuildId != 0 && g.getDiscordId() == discordGuildId).findFirst().orElse(null);
	}

	public static OlympaGuild getOlympaGuildByOlympaId(long guildId) {
		return guilds.stream().filter(g -> guildId != 0 && g.getId() == guildId).findFirst().orElse(null);
	}

	public static void updateGuild(OlympaGuild olympaGuild) {
		guilds.remove(getOlympaGuildByOlympaId(olympaGuild.getId()));
		guilds.add(olympaGuild);
	}
	
	public static OlympaGuild getOlympaGuild(DiscordGuildType type) {
		return guilds.stream().filter(g -> g.getType() == type).findFirst().orElse(null);
	}
}
