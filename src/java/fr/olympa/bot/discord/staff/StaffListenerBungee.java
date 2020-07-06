package fr.olympa.bot.discord.staff;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.staffchat.StaffChatEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffListenerBungee implements Listener {

	@EventHandler
	public void onStaffChat(StaffChatEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String message = event.getMessage();

		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		Guild guild = olympaGuild.getGuild();
		if (olympaPlayer != null) {
			Member member = guild.getMembersByEffectiveName(olympaPlayer.getName(), true).get(0);
			if (member != null)
				WebHookHandler.send(message, guild.getTextChannelById(729534637466189955L), member);
		} else
			WebHookHandler.send(message, guild.getTextChannelById(729534637466189955L), event.getSender().getName(), "https://c7.uihere.com/files/250/925/132/computer-terminal-linux-console-computer-icons-command-line-interface-linux.jpg");
	}
}
