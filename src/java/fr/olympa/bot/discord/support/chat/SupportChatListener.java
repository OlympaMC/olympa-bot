package fr.olympa.bot.discord.support.chat;

import java.awt.Color;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.common.task.NativeTask;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.message.DiscordURL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.MessageActivity.Application;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SupportChatListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		List<User> mentions = message.getMentionedUsers();
		SelfUser me = event.getJDA().getSelfUser();
		if (event.getAuthor().isBot() || !mentions.contains(me))
			return;
		sendMessage(message);
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		User user = event.getAuthor();
		if (user.isBot() || user.isSystem())
			return;
		Message message = event.getMessage();
		PrivateChannel channel = event.getChannel();
		if (!DiscordPermission.AUTHOR.hasPermissionIdUser(user))
			sendMessage(message);
		else {
			String msg = message.getContentRaw();
			String[] args = msg.split(" ");
			if (args.length == 0)
				return;
			String id = args[0];
			String idTarget;
			if (id.startsWith("@")) {
				idTarget = id.substring(1);
				event.getJDA().retrieveUserById(idTarget).queue(u -> {
					String msgFinal = msg.substring(id.length() + 1);
					u.openPrivateChannel().queue(pv -> {
						pv.sendTyping().queue(v -> {
							NativeTask.getInstance().runTaskLater(() -> pv.sendMessage(msgFinal).queue(out -> {
								channel.sendMessage("Message envoyé à " + u.getAsMention() + ": \n" + out.getContentRaw() + "").queue();
							}, error -> {
								channel.sendMessage("Impossible d'envoyer un message à " + u.getAsMention() + ": `" + error.getMessage() + "`").queue();
							}), msgFinal.length() * 100l, TimeUnit.MILLISECONDS);
						}, error -> {
							channel.sendMessage("Impossible d'écrire (sending typing packet) à " + u.getAsMention() + ": `" + error.getMessage() + "`").queue();
						});
					}, error -> {
						channel.sendMessage("Impossible d'ouvrir une discussion privé avec " + u.getAsMention() + ": `" + error.getMessage() + "`").queue();
					});
				}, error -> {
					channel.sendMessage("Impossible de trouver un User avec l'id " + idTarget + ": `" + error.getMessage() + "`").queue();
				});
			} else if (id.startsWith("#")) {
				idTarget = id.substring(1);
				TextChannel txtCh = event.getJDA().getTextChannelById(idTarget);
				if (txtCh == null) {
					channel.sendMessage("Le channel n°" + idTarget + " est introuvable."
							+ "Pour récupérer l'id d'un channel, active le mode développeur dans les paramètres, et clique gauche sur un channel > `Copier l'identifiant`").queue();
					return;
				}
				String msgFinal = msg.substring(id.length() + 1);
				txtCh.sendTyping().queue(v -> {
					NativeTask.getInstance().runTaskLater(() -> txtCh.sendMessage(msgFinal).queue(out -> {
						channel.sendMessage("Message envoyé dans " + txtCh.getAsMention() + " sur " + txtCh.getGuild().getName() + " " + new DiscordURL(out).getJumpLabel()).queue();
					}, error -> {
						channel.sendMessage("Impossible d'envoyer un message dans " + txtCh.getAsMention() + " sur " + txtCh.getGuild().getName() + ": `" + error.getMessage() + "`").queue();
					}), msgFinal.length() * 100l, TimeUnit.MILLISECONDS);
					NativeTask.getInstance().runTaskLater(() -> txtCh.sendMessage(msgFinal).queue(), msgFinal.length() * 100l, TimeUnit.MILLISECONDS);
				}, error -> {
					channel.sendMessage("Impossible d'écrire (sending typing packet) dans " + txtCh.getAsMention() + " sur " + txtCh.getGuild().getName() + ": `" + error.getMessage() + "`").queue();
				});

			} else {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("Mode Administrateur du bot");
				eb.addField("Pour envoyer à un message privé à un utilisateur discord", "Envoie moi `@<id de l'user> <ton message>`", true);
				eb.addField("Pour envoyer à un message sur un channel d'un serveur", "Envoie moi `#<id du channel> <ton message>`", true);
				eb.setColor(OlympaBots.getInstance().getDiscord().getColor());
				channel.sendMessageEmbeds(eb.build()).queue();
			}
		}
	}

	public void sendMessage(Message message) {
		List<Attachment> attachments = message.getAttachments();
		String msg = message.getContentRaw();
		EmbedBuilder eb = new EmbedBuilder();
		User user = message.getAuthor();
		String avatarUrl = user.getEffectiveAvatarUrl();
		eb.setAuthor(user.getAsTag(), avatarUrl);
		eb.setThumbnail(avatarUrl);
		eb.setDescription(user.getAsMention() + " id " + user.getIdLong());
		if (!attachments.isEmpty())
			eb.setImage(attachments.get(0).getProxyUrl());
		eb.addField("Message", msg, false);
		for (Attachment att : attachments)
			eb.addField("Fichier > " + att.getFileName(), new DiscordURL(att.getProxyUrl()).getJumpLabel(), true);
		if (message.isFromGuild()) {
			String nickName = message.getMember().getNickname();
			if (nickName != null)
				eb.appendDescription(" Surnom sur `" + message.getGuild().getName() + "` `" + nickName + "`");
			TextChannel gc = message.getTextChannel();
			eb.addField("Dans ", gc.getAsMention() + " " + new DiscordURL(message).get(), true);
			if (message.getReferencedMessage() == null)
				eb.setTitle("Mentionner dans le channel " + gc.getName());
			else {
				Message msgReferenced = message.getReferencedMessage();
				eb.setTitle("Répondu à un message du bot dans le channel " + gc.getName());
				eb.addField("Citation de réponse de " + msgReferenced.getAuthor().getAsMention(), msgReferenced.getContentRaw(), false);
			}
			eb.setFooter("Pour répondre : `#" + gc.getId() + " <ton message>`", message.getGuild().getIconUrl());
			eb.setColor(Color.ORANGE);
		} else {
			eb.setTitle("Message privé reçu");
			eb.setFooter("Pour répondre : `@" + user.getId() + " <ton message>`");
			eb.setColor(Color.BLUE);
		}
		MessageActivity activity = message.getActivity();
		if (activity != null) {
			Application appli = activity.getApplication();
			StringJoiner sj = new StringJoiner(" ");
			if (activity.getPartyId() != null)
				sj.add(activity.getPartyId());
			if (appli != null) {
				sj.add(appli.getName());
				sj.add(appli.getDescription());
			}
			eb.addField("Activiter " + activity.getType().name(), sj.toString(), false);
		}
		DiscordPermission.AUTHOR.fetchAllowIdsUser(u -> {
			u.openPrivateChannel().queue(ch -> ch.sendMessageEmbeds(eb.build()).queue());
		});
	}
}
