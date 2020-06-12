package fr.olympa.bot.discord.sanctions;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

public class SanctionHandler {
	
	public static void mute(Member target) {
		SelfUser me = OlympaBots.getInstance().getDiscord().getJda().getSelfUser();
		mute(target, me, GuildHandler.getOlympaGuild(target.getGuild()).getLogChannel());
	}
	
	public static void mute(Member target, User author, MessageChannel channel) {
		Guild guild = target.getGuild();
		guild.addRoleToMember(target, DiscordGroup.MUTED.getRole(guild)).queue();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Mute");
		em.setDescription(target.getAsMention() + " a été mute par " + author.getAsMention());
		channel.sendMessage(em.build()).queue();
	}
}
