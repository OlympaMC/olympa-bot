package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.commands.api.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

public class InstanceCommand extends DiscordCommand {

	public InstanceCommand() {
		super("instance", "credit", "info");
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		message.delete().queue();
		JDA jda = message.getJDA();
		SelfUser user = jda.getSelfUser();
		List<Guild> guilds = jda.getGuilds();
		int usersConnected = 0;
		int usersTotal = 0;
		for (User user2 : jda.getUsers()) {
			if (!user2.isBot()) {
				Guild firstGuild = user2.getMutualGuilds().get(0);
				Member member2 = firstGuild.getMember(user2);
				if (member2.getOnlineStatus() != OnlineStatus.OFFLINE) {
					usersConnected++;
				}
				usersTotal++;
			}
		}

		User author = jda.getUserById(450125243592343563L);

		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Informations")
				.addField("Nom", user.getName(), true)
				.addField("Prefix", DiscordCommand.prefix, true)
				.addField("Ping", String.valueOf(jda.getGatewayPing()), true)
				.addField("Clients", usersConnected + "/" + usersTotal, true)
				.addField("Serveurs Discord", String.valueOf(guilds.size()), true)
				.addField("Donnés envoyés", String.valueOf(jda.getResponseTotal()), true)
				.setFooter("Créateur " + author.getName(), author.getAvatarUrl());
		embed.setColor(Color.YELLOW);
		channel.sendMessage(embed.build()).queue(m -> m.delete().queueAfter(OlympaBots.getInstance().getDiscord().timeToDelete, TimeUnit.SECONDS));
	}

}
