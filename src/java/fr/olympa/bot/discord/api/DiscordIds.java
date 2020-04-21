package fr.olympa.bot.discord.api;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordIds {

	public static TextChannel getChannelInfo() {
		return getDefaultGuild().getTextChannelById(558356359805009931L);
	}

	public static Guild getDefaultGuild() {
		return getDefaultGuild(OlympaBots.getInstance().getDiscord().getJda());
	}

	public static Guild getDefaultGuild(JDA jda) {
		return jda.getGuildById(544593846831415307L);
	}

	public static Role getMuteRole() {
		return getDefaultGuild().getRoleById(566627971276865576L);
	}

	public static Guild getStaffGuild() {
		return getStaffGuild(OlympaBots.getInstance().getDiscord().getJda());
	}

	public static Guild getStaffGuild(JDA jda) {
		return jda.getGuildById(541605430397370398L);
	}
}
