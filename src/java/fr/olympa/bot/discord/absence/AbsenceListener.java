package fr.olympa.bot.discord.absence;

import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AbsenceListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();

		channel.getName();
		Member member = event.getMember();
		if (member == null || member.getUser().isBot() || !DiscordUtils.isStaffGuild(guild) || !DiscordUtils.isStaffGuild(guild)) {
			return;
		}

	}
}
