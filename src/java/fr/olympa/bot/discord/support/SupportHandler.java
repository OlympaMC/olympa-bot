package fr.olympa.bot.discord.support;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

public class SupportHandler {

	public static void askWhoStaff(TextChannel textChannel, Member member) {
		EmbedBuilder mb = new EmbedBuilder().setTitle("Support Olympa");
		mb.setColor(OlympaBots.getInstance().getDiscord().getColor());
		for (DiscordGroup discordGroup : DiscordGroup.values()) {
			if (discordGroup.isSupportShow()) {
				mb.addField(discordGroup.getRole(textChannel.getGuild()).getName(), "```" + discordGroup.getSupportDesc() + "```", true);
			}
		}
		mb.setDescription("Maintenant tu dois choisir à qui s'adresse ta demande.");
		textChannel.sendMessage(mb.build()).queue(msg -> {
			for (DiscordGroup discordGroup : DiscordGroup.values()) {
				Role role = discordGroup.getRole(textChannel.getGuild());
				if (discordGroup.isSupportCanTag()) {
					msg.addReaction(DiscordGroup.getEmoji(role)).queue();
				}
			}
		});
	}

	public static void createCategor(Guild guild) {
		createCategory(guild, null);
	}

	public static void createCategory(Guild guild, Consumer<Category> created) {
		guild.createCategory("🏳️ Support").queue(r -> {
			if (created != null) {
				created.accept(r);
			}
		});
	}

	public static void createChannel(Category category, Member member) {
		DiscordUtils.deny(category, null, perm -> {
			category.createTextChannel(member.getEffectiveName().toLowerCase()).queue(textChannel -> {
				ChannelManager manager = textChannel.getManager();
				manager.setTopic(member.getId() + " " + StatusSupportChannel.OPEN.getId()).queue();
				manager.setSlowmode(10).queue();
				DiscordUtils.allow(textChannel, member, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL);

				EmbedBuilder mb = new EmbedBuilder().setTitle("Bienvenue sur le support Discord");
				mb.setColor(OlympaBots.getInstance().getDiscord().getColor());
				mb.setDescription("Les règles sont simples :");
				mb.addField("Ce que tu ne peux pas", "- Pas de signalement de cheater\n- Pas de plainte\n- Pas de candidature", true);
				mb.addField("Ce que tu peux dire", "- Signalement de bugs\n- Une question", true);
				mb.addField("Pour écrire ici tu dois", "Avoir relier ton compte Olympa et ton compte Discord via /discord link sur Minecraft", false);
				mb.addField("Pas d'abus", "N'abuse pas du support et n'oublie pas d'être polie. Cette conversation risque d'être enregistrer.", false);
				textChannel.sendMessage(mb.build()).queue();
			});
		}, Permission.VIEW_CHANNEL, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_ADD_REACTION);
	}

	public static void createChannel(Member member) {
		Guild guild = member.getGuild();
		// CompletableFuture.runAsync(() -> {
		Category category = getCategory(guild);
		if (category == null) {
			createCategory(guild, c -> createChannel(member));
		} else {
			createChannel(category, member);
		}
	}

	public static Set<Category> getCategory() {
		Set<Category> cats = OlympaBots.getInstance().getDiscord().getJda().getCategories().stream().filter(cat -> cat.getName().endsWith("Support")).collect(Collectors.toSet());
		if (cats.isEmpty()) {
			return null;
		}
		Set<Category> catsSameGuild = cats.stream().map(cat -> cats.stream().filter(cat2 -> cat2.getGuild().getIdLong() != cat.getGuild().getIdLong()).findFirst().orElse(null)).collect(Collectors.toSet());
		cats.removeAll(removeDuplicCat(catsSameGuild));
		return cats;
	}

	public static Category getCategory(Guild guild) {
		List<Category> cats = guild.getCategories().stream().filter(cat -> cat.getName().endsWith("Support")).collect(Collectors.toList());
		if (cats.isEmpty()) {
			return null;
		}
		Set<Category> catsSameGuild = cats.stream().map(cat -> cats.stream().filter(cat2 -> cat2.getGuild().getIdLong() == cat.getGuild().getIdLong()).findFirst().orElse(null)).collect(Collectors.toSet());
		cats.removeAll(removeDuplicCat(catsSameGuild));
		if (cats.size() > 1) {
			System.out.println("[ERROR] They are more than 1 Support category in guild " + guild.getName());
		}
		return cats.get(0);
	}

	public static TextChannel getChannel(Member member) {
		Category category = getCategory(member.getGuild());
		if (category != null) {
			return category.getTextChannels().stream().filter(ch -> ch.getTopic().split(" ")[0].equalsIgnoreCase(member.getId())).findFirst().orElse(null);
		}
		return null;
	}

	public static StatusSupportChannel getChannelStatus(TextChannel channel) {
		String[] topic = channel.getTopic().split(" ");
		int statusId = Integer.parseInt(topic[1]);
		return StatusSupportChannel.get(statusId);
	}

	public static Member getMemberByChannel(TextChannel channel) {
		return channel.getTopic() == null || !getCategory(channel.getGuild()).getChannels().contains(channel) ? null : channel.getGuild().getMemberById(channel.getTopic().split(" ")[0]);
	}

	public static Entry<Member, StatusSupportChannel> getMemberStatusByChannel(TextChannel channel) {
		if (channel.getTopic() == null || channel.getParent() == null || !channel.getParent().getName().endsWith("Support")) {
			return null;
		}
		String[] split = channel.getTopic().split(" ");
		return new AbstractMap.SimpleEntry<>(channel.getGuild().getMemberById(split[0]), StatusSupportChannel.get(Integer.parseInt(split[1])));
	}

	public static boolean isSupportChannel(TextChannel TextChannel, Member member) {
		return TextChannel.getName().equals(member.getEffectiveName().toLowerCase());
	}

	public static boolean panelStaff(ReactionEmote reactionEmote, TextChannel textChannel, long msgId, Member member) {
		DiscordGroup group = DiscordGroup.get(textChannel.getGuild(), reactionEmote.getEmoji());
		if (group == null) {
			return false;
		}
		Role staffRole = group.getRole(textChannel.getGuild());
		DiscordUtils.allow(textChannel, staffRole, Permission.MESSAGE_READ);
		// textChannel.sendMessage(staffRole.getAsMention()).queue();
		EmbedBuilder mb2 = new EmbedBuilder().setTitle("Panel Staff").setDescription("Ce panel est réservé au staff");
		mb2.setColor(OlympaBots.getInstance().getDiscord().getColor());
		List<String> reactions = new ArrayList<>();
		for (DiscordGroup discordGroup : DiscordGroup.values()) {
			if (!discordGroup.isStaff()) {
				continue;
			}
			Role role2 = discordGroup.getRole(textChannel.getGuild());
			if (role2 != null && role2.getIdLong() != staffRole.getIdLong()) {
				String membersRole = textChannel.getGuild().getMembersWithRoles(role2).stream().map(Member::getAsMention).collect(Collectors.joining(", "));
				if (!membersRole.isEmpty()) {
					mb2.addField(role2.getName(), membersRole, true);
					reactions.add(DiscordGroup.getEmoji(role2));
				}
			}
		}
		for (AutoResponse ar : AutoResponse.values()) {
			mb2.addField(ar.getEmoji(), "`" + ar.getName() + "`", true);
			reactions.add(ar.getEmoji());
		}
		textChannel.sendMessage(mb2.build()).queue(msg -> reactions.forEach(unicode -> msg.addReaction(unicode).queue()));
		EmbedBuilder mb = new EmbedBuilder().setTitle("Support Olympa");
		StringBuilder sb = new StringBuilder("Ta demande a été prise en compte. ");
		mb.setColor(OlympaBots.getInstance().getDiscord().getColor());
		List<Member> staff = staffRole.getGuild().getMembersWithRoles(staffRole);
		Role staffUse = staffRole;
		Permission[] perms = new Permission[] { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE };
		if (staff.isEmpty()) {
			Role assistant = DiscordGroup.ASSISTANT.getRole(textChannel.getGuild());
			DiscordUtils.allow(textChannel, assistant, perms);
			sb.append("Malheureusement nous n'avons actuellement aucun " + staffUse.getAsMention() + " actuellement. Je te redirige vers les " + assistant.getAsMention() + ".");
			staffUse = DiscordGroup.ASSISTANT.getRole(textChannel.getGuild());
		} else {
			if (staff.stream().map(Member::getOnlineStatus).filter(s -> s != OnlineStatus.OFFLINE && s != OnlineStatus.INVISIBLE).findFirst().isPresent()) {
				sb.append("Les " + staffUse.getAsMention() + " sont au courant, ils vont te répondre sous peu.");
			} else {
				sb.append("Malheureusement aucun " + staffUse.getAsMention() + " n'est actuellement disponible. Ils vont te répondre dès que possible.");
			}
		}
		DiscordUtils.allow(textChannel, staffUse, perms);
		// textChannel.sendMessage(staffUse.getAsMention()).queue();
		mb.setDescription(sb.toString());
		textChannel.sendMessage(mb.build()).queue();
		return true;
	}

	private static Set<Category> removeDuplicCat(Set<Category> catsSameGuild) {
		Iterator<Category> it = catsSameGuild.iterator();
		while (it.hasNext()) {
			Category categorieGuild = it.next();
			if (categorieGuild.getGuild().getCategories().stream().filter(cat -> cat.getName().endsWith("Support")).count() > 1) {
				categorieGuild.delete().queue();
			} else {
				catsSameGuild.remove(categorieGuild);
			}
		}
		return catsSameGuild;
	}

	public static void setChannelStatus(TextChannel channel, StatusSupportChannel status) {
		String[] topic = channel.getTopic().split(" ");
		topic[1] = String.valueOf(status.getId());
		channel.getManager().setTopic(String.join(" ", topic)).queue();
	}

	public static void updateChannel(Member member) {
		GuildChannel channel = getChannel(member);
		String name = member.getEffectiveName().toLowerCase();
		if (channel == null || channel.getName().equals(name)) {
			return;
		}
		channel.getManager().setName(name).queue();
	}
}
