package fr.olympa.bot.discord.guild;

import java.util.List;

import javax.annotation.Nullable;

import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class GuildHandler {

	public static List<OlympaGuild> guilds = null;

	public static OlympaGuild getOlympaGuild(Guild guild) {
		return getOlympaGuildByDiscordId(guild.getIdLong());
	}

	public static OlympaGuild getOlympaGuildById(long olympaDiscordGuildId) {
		return guilds.stream().filter(g -> g.getId() == olympaDiscordGuildId).findFirst().orElse(null);
	}

	public static OlympaGuild getOlympaGuildByDiscordId(long discordGuildId) {
		return guilds.stream().filter(g -> g.getDiscordId() == discordGuildId).findFirst().orElse(null);
	}

	@Nullable
	public static Boolean isStaffChannel(TextChannel channel) {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? channel.getIdLong() == olympaGuild.getStaffChannelId() : null;
	}

	@Nullable
	public static Boolean isBugsChannel(TextChannel channel) {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? channel.getIdLong() == olympaGuild.getBugsChannelId() : null;
	}

	@Nullable
	public static Boolean isMinecraftChannel(TextChannel channel) {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? channel.getIdLong() == olympaGuild.getMinecraftChannelId() : null;
	}

	@Nullable
	public static TextChannel getStaffChannel() {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? olympaGuild.getStaffChannel() : null;
	}

	@Nullable
	public static TextChannel getBugsChannel() {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? olympaGuild.getBugsChannel() : null;
	}

	@Nullable
	public static TextChannel getMinecraftChannel() {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		return olympaGuild != null ? olympaGuild.getMinecraftChannel() : null;
	}

	public static OlympaGuild getOlympaGuildByOlympaId(long guildId) {
		return guilds.stream().filter(g -> g.getId() == guildId).findFirst().orElse(null);
	}

	public static void updateGuild(OlympaGuild olympaGuild) {
		guilds.remove(getOlympaGuildByOlympaId(olympaGuild.getId()));
		guilds.add(olympaGuild);
	}

	public static OlympaGuild getOlympaGuild(DiscordGuildType type) {
		return guilds.stream().filter(g -> g.getType() == type).findFirst().orElse(null);
	}

	public static Member getMember(DiscordGuildType type, long discordId) {
		return getOlympaGuild(type).getGuild().getMemberById(discordId);
	}

	public static Member getMember(DiscordGuildType type, User user) {
		return getOlympaGuild(type).getGuild().getMemberById(user.getIdLong());
	}
}
