package fr.olympa.bot.bungee;

import java.awt.Color;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.staffchat.StaffChatEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffListenerBungee implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = null;
		try {
			olympaPlayer = new AccountProvider(player.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		DiscordMember discordMember = null;
		String playerName = player.getName();
		if (olympaPlayer != null)
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (discordMember != null) {
			User user = discordMember.getUser();
			playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(player.getName(), null, "https://minotar.net/helm/" + player.getName());
		eb.setDescription(playerName + " s'est déconnecté du serveur.");
		eb.setColor(Color.RED);
		channelStaffDiscord.sendMessage(eb.build()).queue();
	}

	@EventHandler
	public void onPostLogin(ServerSwitchEvent event) {
		if (event.getFrom() != null)
			return;
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = null;
		try {
			olympaPlayer = new AccountProvider(player.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		DiscordMember discordMember = null;
		String playerName = player.getName();
		if (olympaPlayer != null)
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (discordMember != null) {
			User user = discordMember.getUser();
			playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(player.getName(), null, "https://minotar.net/helm/" + player.getName());
		eb.setDescription(playerName + " s'est connecté au serveur " + player.getServer().getInfo().getName() + ".");
		eb.setColor(Color.GREEN);
		channelStaffDiscord.sendMessage(eb.build()).queue();
	}

	@EventHandler
	public void onStaffChat(StaffChatEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String message = event.getMessage();

		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		Guild guild = olympaGuild.getGuild();
		TextChannel channelStaffDiscord = olympaGuild.getStaffChannel();
		if (olympaPlayer != null) {
			Member member = null;
			try {
				DiscordMember dm = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
				if (dm != null)
					member = guild.getMemberById(dm.getDiscordId());
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			if (member == null)
				member = guild.getMembersByEffectiveName(olympaPlayer.getName(), true).get(0);
			if (member != null)
				WebHookHandler.send(message, channelStaffDiscord, member);
		} else
			WebHookHandler.send(message, channelStaffDiscord, event.getSender().getName(), "https://c7.uihere.com/files/250/925/132/computer-terminal-linux-console-computer-icons-command-line-interface-linux.jpg");
	}

	ArrayListMultimap<String, String> queue = ArrayListMultimap.create();

	public void sendErrorsInQueue() {
		for (Entry<String, String> e : queue.entries())
			sendError(e.getKey(), e.getValue());
		queue.clear();
	}

	public static Cache<Entry<String, String>, Message> cache = CacheBuilder.newBuilder().maximumSize(50).build();

	public void sendBungeeError(String stackTrace) {
		sendError("bungee", stackTrace);
	}

	public void sendError(String serverName, String stackTrace) {
		OlympaDiscord olympaDiscord = OlympaBots.getInstance().getDiscord();
		if (olympaDiscord == null || olympaDiscord.getJda() == null) {
			if (!queue.containsKey(serverName) || !queue.get(serverName).contains(stackTrace))
				queue.put(serverName, stackTrace);
			return;
		}
		SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<>(serverName, stackTrace);
		Message message = cache.getIfPresent(entry);
		if (message != null) {
			String content = message.getContentRaw();
			int xIndex = content.lastIndexOf('x');
			int times = Integer.parseInt(content.substring(xIndex + 1));
			times++;
			message.editMessage(content.substring(0, xIndex + 1) + times).queue(null, x -> {
				cache.invalidate(entry);
				sendError(serverName, stackTrace);
			});
			/*Collection<Emoji> emojis = EmojiManager.getAll();
			message.addReaction(emojis.stream().skip(new Random().nextInt(emojis.size())).findFirst().orElse(null).getUnicode()).queue(null, ErrorResponseException.ignore(ErrorResponse.TOO_MANY_REACTIONS, ErrorResponse.REACTION_BLOCKED));*/
			return;
		}
		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
		if (channelStaffDiscord == null) {
			LinkSpigotBungee.Provider.link.sendMessage("&cImpossible de print une erreur (de &4serverName = %s&c) sur discord, le bot discord est pas connecté.", serverName);
			return;
		}
		List<String> strings = new ArrayList<>(2);
		int maxSize = 2000 - 50;
		if (stackTrace.length() < maxSize)
			strings.add(stackTrace);
		else {
			StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
			StringBuilder output = new StringBuilder(stackTrace.length());
			int lineLen = 0;
			while (tok.hasMoreTokens()) {
				String word = tok.nextToken() + "\n";

				if (lineLen + word.length() > maxSize) {
					strings.add(output.toString());
					output = new StringBuilder(stackTrace.length());
					lineLen = 0;
				}
				output.append(word);
				lineLen += word.length();
			}
			strings.add(output.toString());
		}
	}

	public void sendErrorFlushInfo() {
		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
		if (channelStaffDiscord != null)
			channelStaffDiscord.sendMessage("__Le bot a redémarré ~ vidage du cache__").queue();
	}
}
