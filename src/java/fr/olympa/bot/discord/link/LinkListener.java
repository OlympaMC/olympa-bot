package fr.olympa.bot.discord.link;

import java.sql.SQLException;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LinkListener extends ListenerAdapter {

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		User user = event.getAuthor();
		if (user.isBot())
			return;
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		String msg = message.getContentRaw();
		String code = msg.split(" ")[0];
		try {
			ProxiedPlayer player = LinkHandler.getPlayer(code);
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(user);
			if (discordMember.getOlympaId() != 0)
				return;
			if (player == null) {
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Bonjour " + user.getName());
				embed.setDescription("Si tu souhaites relier ton compte Olympa et ton compte Discord, fais **/discord link** sur Minecraft et donne moi ici le code obtenu.");
				channel.sendMessage(embed.build()).queue();
				return;
			}
			Member member = GuildHandler.getMember(DiscordGuildType.PUBLIC, user);
			if (member == null) {
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Bonjour " + user.getName());
				embed.setDescription("Tu dois rejoindre le discord avant de pouvoir lier ton compte. http://discord.olympa.fr/");
				channel.sendMessage(embed.build()).queue();
				return;
			}
			OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();
			discordMember.setOlympaId(olympaPlayer.getId());
			DiscordSQL.updateMember(discordMember);
			LinkHandler.updateGroups(member, olympaPlayer);
			Member memberStaff = GuildHandler.getMember(DiscordGuildType.STAFF, user);
			if (memberStaff != null)
				LinkHandler.updateGroups(memberStaff, olympaPlayer);
			EmbedBuilder embed = new EmbedBuilder();
			embed.setDescription("Tu as relié ton compte Olympa " + player.getName() + " avec ton compte discord " + user.getAsMention() + ". Tu as reçu les bons rôles sur discord.");
			channel.sendMessage(embed.build()).queue();
		} catch (SQLException e) {
			channel.sendMessage("⚠  Une erreur est survenue.").queue();
			e.printStackTrace();
		}

	}

}
