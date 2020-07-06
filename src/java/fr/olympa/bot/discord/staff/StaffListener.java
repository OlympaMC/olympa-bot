package fr.olympa.bot.discord.staff;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.sql.MySQL;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.core.bungee.staffchat.StaffChatHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		TextChannel channel = event.getChannel();
		if (GuildHandler.getOlympaGuild(guild).getType() != DiscordGuildType.STAFF && channel.getIdLong() != 729534637466189955L)
			return;
		OlympaPlayer olympaPlayer = MySQL.getPlayer(member.getEffectiveName());
		if (olympaPlayer == null)
			return;
		StaffChatHandler.sendMessage(olympaPlayer, null, event.getMessage().getContentDisplay());
	}
}
