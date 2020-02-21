package fr.olympa.bot.discord.listener;

import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		if (!DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		int usersTotal = 0;
		for (User user2 : event.getJDA().getUsers()) {
			if (!user2.isBot()) {
				usersTotal++;
			}
		}
		GuildChannel membersChannel = guild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
		membersChannel.getManager().setName("Membres : " + usersTotal).queue();
	}
	
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Guild guild = event.getGuild();
		if (!DiscordUtils.isDefaultGuild(guild)) {
			return;
		}
		int usersTotal = 0;
		for (User user2 : event.getJDA().getUserCache()) {
			if (!user2.isBot()) {
				usersTotal++;
			}
		}
		GuildChannel membersChannel = guild.getChannels().stream().filter(c -> c.getIdLong() == 589164145664851972L).findFirst().orElse(null);
		membersChannel.getManager().setName("Membres : " + usersTotal).queue();
	}
}
