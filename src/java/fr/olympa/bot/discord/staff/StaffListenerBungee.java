package fr.olympa.bot.discord.staff;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.staffchat.StaffChatEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.event.EventHandler;

public class StaffListenerBungee {

	@EventHandler
	public void onStaffChat(StaffChatEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String message = event.getMessage();
		CommandSender sender = event.getSender();

		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC);
		Guild guild = olympaGuild.getGuild();
		Member member = guild.getMembersByEffectiveName(olympaPlayer.getName(), true).get(0);
		WebHookHandler.send(message, guild.getTextChannelById(729534637466189955L), member);
	}
}
