package fr.olympa.bot.discord.support;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

public class SupportHandler {

	public static void askWhoStaff(TextChannel textChannel, Member member) {
		EmbedBuilder mb = new EmbedBuilder().setTitle("Ta demande est prise en compte");
		mb.setColor(OlympaDiscord.getColor());
		for (DiscordGroup discordGroup : DiscordGroup.values()) {
			if (discordGroup.isSupportShow()) {
				mb.addField(discordGroup.getRole().getName(), discordGroup.getSupportDesc(), true);
			}
		}
		mb.setDescription("Maintenant tu dois choisir à qui s'adresse ta demande.");
		textChannel.sendMessage(mb.build()).queue(msg -> {
			for (DiscordGroup discordGroup : DiscordGroup.values()) {
				Role role = discordGroup.getRole();
				System.out.println("Role: " + discordGroup.getRole().getName() + " emoji " + discordGroup.getEmoji(role));
				if (discordGroup.isSupportCanTag()) {
					msg.addReaction(discordGroup.getEmoji(role)).queue();
				}
			}
		});
	}

	public static void createCategory(Guild guild) {
		guild.createCategory("🏳️ Support").queue(category -> category.getManager().setPosition(0).queue());
	}

	public static void createChannel(Category category, Member member) {
		Role defaultRole = category.getGuild().getPublicRole();
		PermissionOverrideAction permissionAction = category.createPermissionOverride(defaultRole);
		permissionAction.deny(Permission.VIEW_CHANNEL).queue(perm -> {
			category.createTextChannel(member.getEffectiveName().toLowerCase()).queue(textChannel -> {
				ChannelManager manager = textChannel.getManager();
				manager.setSlowmode(10).queue();
				manager.setTopic(member.getId() + " " + StatusSupportChannel.OPEN.getId()).queue();
				PermissionOverrideAction permissionAction2 = textChannel.createPermissionOverride(member);
				permissionAction2.setAllow(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL).queue();

				EmbedBuilder mb = new EmbedBuilder().setTitle("Bienvenue sur le support Discord");
				mb.setColor(OlympaDiscord.getColor());
				mb.setDescription("Les règles sont simples :");
				mb.addField("Ce que tu ne peux pas", "- Pas de report de cheater\n- Pas de plainte\n- Pas de candidature", true);
				mb.addField("Ce que tu peux dire", "- Signalement de bugs\n- Une question si tu as déjà chercher la réponse.", true);
				mb.addField("Pour écrire ici tu dois", "Avoir relier ton compte Olympa et ton compte Discord via /discord link sur Minecraft", false);
				mb.addField("Pas d'abus", "N'abuse pas du support et n'oublie pas d'être polie. Cette conversation risque d'être enregistrer.", false);
				textChannel.sendMessage(mb.build()).queue();
			});
		});
	}
	
	public static void createChannel(Member member) {
		Guild guild = member.getGuild();
		CompletableFuture.runAsync(() -> {
			Category category = getCategory(guild);
			if (category == null) {
				createCategory(guild);
				category = getCategory(guild);
			}
			createChannel(category, member);
		});
	}

	public static Category getCategory(Guild guild) {
		List<Category> cats = guild.getCategoriesByName("🏳 Support", true);
		if (cats.isEmpty()) {
			return null;
		}
		if (cats.size() > 1) {
			System.out.println("[ERROR] They are more than 1 Support category in guild " + guild.getName());
		}
		return cats.get(0);
	}
	
	public static TextChannel getChannel(Member member) {
		Category cat = getCategory(member.getGuild());
		GuildChannel channel = null;
		if (cat != null) {
			channel = cat.getChannels().stream().filter(ch -> {
				boolean b = ch.getType() == ChannelType.TEXT;
				if (b) {
					TextChannel tch = (TextChannel) ch;
					String[] topic = tch.getTopic().split(" ");
					b = topic[0].equalsIgnoreCase(member.getId());
				}
				return b;
			}).findFirst().orElse(null);
		}
		return (TextChannel) channel;
	}
	
	public static StatusSupportChannel getChannelStatus(TextChannel channel) {
		String[] topic = channel.getTopic().split(" ");
		int statusId = Integer.parseInt(topic[1]);
		return StatusSupportChannel.get(statusId);
	}
	
	public static boolean isSupportChannel(MessageChannel messageChannel, Member member) {
		return messageChannel.getName().equals(member.getEffectiveName().toLowerCase());
	}
	
	public static void setChannelStatus(TextChannel channel, StatusSupportChannel status) {
		String[] topic = channel.getTopic().split(" ");
		topic[1] = String.valueOf(status.getId());
		channel.getManager().setTopic(String.join(" ", topic)).queue();
	}
	
	public static void updateChannel(Member member) {
		GuildChannel channel = getChannel(member);
		if (channel == null || channel.getName().equals(member.getEffectiveName().toLowerCase())) {
			return;
		}
		channel.getManager().setName(member.getEffectiveName().toLowerCase());
	}
}
