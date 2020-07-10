package fr.olympa.bot.discord.sanctions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.core.bungee.ban.SanctionUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

public class SanctionHandler {

	@Deprecated
	public static void mute(Member target, String reason) throws SQLException {
		SelfUser me = OlympaBots.getInstance().getDiscord().getJda().getSelfUser();
		addSanction(target, me, reason, DiscordSanctionType.MUTE, null);
	}

	public static void addSanction(Member target, User author, String reason, DiscordSanctionType type, Long expire) throws SQLException {
		Guild guild = target.getGuild();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		DiscordMember authorMember = CacheDiscordSQL.getDiscordMember(author.getIdLong());
		DiscordMember targetMember = CacheDiscordSQL.getDiscordMember(target.getIdLong());
		DiscordSanction discordSanction = new DiscordSanction(targetMember.getId(), authorMember.getId(), type, reason, expire);
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle(type.getName());
		em.setDescription(target.getAsMention() + " a été mute par " + author.getAsMention() + " pour **" + reason + "**");
		if (expire != null)
			em.appendDescription(" pendant **" + Utils.timestampToDuration(expire) + "**");
		em.appendDescription(".");
		olympaGuild.getLogChannel().sendMessage(em.build()).queue();

		if (discordSanction.getType() == DiscordSanctionType.MUTE) {
			if (expire != null)
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						guild.removeRoleFromMember(target, DiscordGroup.MUTED.getRole(guild)).queue();
					}
				}, new Date(expire * 1000L));
			guild.addRoleToMember(target, DiscordGroup.MUTED.getRole(guild)).queue();
		} else
			guild.kick(target, reason);
	}

	public static void addSanctionFromMsg(Member target, Message message, DiscordSanctionType type) throws SQLException {
		String[] args = message.getContentRaw().split(" ");
		List<String> listArgs = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
		Long expire = null;
		User author = message.getAuthor();
		String reason;
		Matcher matcherDuration = SanctionUtils.matchDuration(String.join(" ", listArgs));
		if (matcherDuration.find()) {
			listArgs.remove(matcherDuration.group());
			String time = matcherDuration.group(1);
			String unit = matcherDuration.group(2);
			if (!listArgs.remove(time + unit)) {
				listArgs.remove(time);
				listArgs.remove(unit);
			}
			expire = SanctionUtils.toTimeStamp(Integer.parseInt(time), unit);
		}
		reason = String.join(" ", listArgs);
		addSanction(target, author, reason, type, expire);
	}
}
