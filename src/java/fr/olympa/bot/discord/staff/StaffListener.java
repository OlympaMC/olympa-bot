package fr.olympa.bot.discord.staff;

import java.sql.SQLException;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.core.bungee.staffchat.StaffChatHandler;
import fr.olympa.core.common.provider.AccountProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StaffListener extends ListenerAdapter {
	{
		/*
		 * Role staffRole = group.getRole(textChannel.getGuild());
		 * DiscordUtils.allow(textChannel, staffRole, Permission.MESSAGE_READ); //
		 * textChannel.sendMessage(staffRole.getAsMention()).queue(); EmbedBuilder mb2 =
		 * new EmbedBuilder().setTitle("Panel Staff").
		 * setDescription("Ce panel est réservé au staff");
		 * mb2.setColor(OlympaDiscord.getColor()); List<String> reactions = new
		 * ArrayList<>(); for (DiscordGroup discordGroup : DiscordGroup.values()) { if
		 * (!discordGroup.isStaff()) { continue; } Role role2 =
		 * discordGroup.getRole(textChannel.getGuild()); if (role2 != null &&
		 * role2.getIdLong() != staffRole.getIdLong()) { String membersRole =
		 * textChannel.getGuild().getMembersWithRoles(role2).stream().map(Member::
		 * getAsMention).collect(Collectors.joining(", ")); if (!membersRole.isEmpty())
		 * { mb2.addField(role2.getName(), membersRole, true);
		 * reactions.add(discordGroup.getEmoji(role2)); } } }
		 */
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if (member == null || member.isFake() || member.getUser().isBot() || !GuildHandler.isStaffChannel(channel))
			return;
		message.clearReactions().queue(r -> message.addReaction(guild.getEmotesByName("VNO", false).get(0)).queue());

	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if (member == null || member.isFake() || member.getUser().isBot() || !GuildHandler.isStaffChannel(channel))
			return;
		DiscordMember dm;
		try {
			dm = CacheDiscordSQL.getDiscordMember(member.getIdLong());
			OlympaPlayer olympaPlayer;
			if (dm.getOlympaId() == 0)
				olympaPlayer = AccountProvider.getter().getSQL().getPlayer(member.getEffectiveName());
			else
				olympaPlayer = AccountProvider.getter().getSQL().getPlayer(dm.getOlympaId());
			if (olympaPlayer == null) {
				message.addReaction(guild.getEmotesByName("V_", false).get(0)).queue();
				return;
			}
			StringBuilder out = new StringBuilder(message.getContentDisplay());
			message.getAttachments().forEach(att -> out.append(" " + att.getProxyUrl()));
			String staffOnline = String.valueOf(StaffChatHandler.sendMessage(olympaPlayer, null, out.toString()));
			message.addReaction(guild.getEmotesByName("VYES", false).get(0)).queue();
			for (int i = 0; staffOnline.length() > i; i++)
				message.addReaction(staffOnline.charAt(i) + "\u20E3").queue();
		} catch (SQLException e) {
			e.printStackTrace();
			message.addReaction(guild.getEmotesByName("VNO", false).get(0)).queue();
			return;
		}

	}
}
