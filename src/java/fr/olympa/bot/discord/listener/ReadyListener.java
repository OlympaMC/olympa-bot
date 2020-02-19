package fr.olympa.bot.discord.listener;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent event) {
		JDA jda = event.getJDA();
		List<Guild> guilds = jda.getGuilds();
		System.out.println("Connected on :");
		for (Guild guild : guilds) {
			Member owner = guild.getOwner();
			System.out.println(guild.getName() + " " + guild.getMemberCount() + " by " + owner.getEffectiveName() + "(" + owner.getId() + ")");
		}
	}
}
