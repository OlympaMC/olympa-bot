package fr.olympa.bot.discord.api;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class DiscordIds {
	
	public static Guild getStaffGuild() {
		return getStaffGuild(OlympaBots.getInstance().getDiscord().getJda());
	}

	public static Guild getStaffGuild(JDA jda) {
		return jda.getGuildById(541605430397370398L);
	}
}
