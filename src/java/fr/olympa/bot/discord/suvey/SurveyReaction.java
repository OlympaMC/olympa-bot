package fr.olympa.bot.discord.suvey;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.task.NativeTask;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class SurveyReaction extends ReactionDiscord {

	private boolean action = false;
	@Nullable
	private Integer time;

	public SurveyReaction(LinkedMap<String, String> map, boolean multiVote) {
		super(map, multiVote);
	}

	public SurveyReaction() {}

	@Override
	public void onBotStop(long messageId) {

	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String reactionEmoji) {
		if (!isClosed()) {
			editMessage(message);
			return true;
		}
		return false;
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionEmoji) {
		if (!isClosed())
			editMessage(message);
	}

	private void retriveEmojis(List<User> users, Iterator<MessageReaction> it, Consumer<List<User>> callback) {
		if (it.hasNext())
			it.next().retrieveUsers().queue(us -> {
				users.addAll(us);
				retriveEmojis(users, it, callback);
			});
		else
			callback.accept(users);
	}

	private boolean disableAction(Message message, boolean taskEdit) {
		boolean b = action;
		action = false;
		if (!taskEdit)
			NativeTask.getInstance().runTaskLater("SURVEY", () -> editMessage(message, true), 10, TimeUnit.SECONDS);
		return b != action;
	}

	private boolean enableAction() {
		boolean b = action;
		action = true;
		return b != action;
	}

	private void editMessage(Message message) {
		editMessage(message, false);
	}

	private void editMessage(Message message, boolean taskEdit) {
		if (!enableAction())
			return;
		NativeTask.getInstance().terminateTaskByName("SURVEY");
		List<MessageReaction> reactionsUsers = message.getReactions();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("📝 Sondage:");
		String question = (String) getData().get("question");
		if (question != null)
			embedBuilder.setDescription("**" + question + "**");
		retriveEmojis(new ArrayList<>(), reactionsUsers.iterator(), users -> {
			users.removeIf(user -> user.getIdLong() == message.getJDA().getSelfUser().getIdLong());
			int total = users.size();
			int totalUnique = (int) users.stream().distinct().count();
			embedBuilder.appendDescription("\n\nVotes " + total + "\n" + (!canMultiple() ? "Les votes sont uniques" : "Vous pouvez voter plusieurs fois" + "\nNombre de vote unique " + totalUnique));
			embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
			Integer time = getTime();
			if (time != null) {
				Date date = new Date(time * 1000l);
				embedBuilder.setTimestamp(date.toInstant());
				if (Utils.getCurrentTimeInSeconds() > time) {
					embedBuilder.setColor(Color.RED);
					embedBuilder.setFooter("Le sondage est terminé, merci !");
				} else {
					SimpleDateFormat format = new SimpleDateFormat("HH:mm le dd/MM/yyyy");
					embedBuilder.setFooter("Le sondage se termine à " + format.format(date) + " UTC Paris");
				}
			}
			for (Entry<String, String> entry : getEmojisData().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				MessageReaction reaction = reactionsUsers.stream().filter(r -> r.getReactionEmote().getEmoji().equals(key)).findFirst().orElse(null);
				if (reaction == null)
					continue;
				double count = reaction.getCount() - 1d;
				String pourcent;
				if (count == 0 || total == 0)
					pourcent = "0";
				else
					pourcent = new DecimalFormat("0.#").format(count / total * 100D);
				String countRound = new DecimalFormat("0.#").format(count);
				embedBuilder.addField(value, key + " " + pourcent + "% " + (count != 0 ? countRound + " vote" + Utils.withOrWithoutS((int) Math.round(count)) : ""), false);
			}
			MessageEmbed embed = embedBuilder.build();
			List<MessageEmbed> embeds = message.getEmbeds();
			if (embeds.isEmpty() || !embeds.get(0).equals(embed))
				message.editMessage(embed).queue(msg -> disableAction(message, taskEdit));
			else
				disableAction(message, taskEdit);
		});
	}

	public boolean isClosed() {
		Integer time = getTime();
		if (time == null || time == 0)
			return false;
		return Utils.getCurrentTimeInSeconds() > time;
	}

	public Integer getTime() {
		if (time != null)
			return time;
		Object objectTime = getData("time");
		if (objectTime == null)
			return null;
		if (objectTime instanceof Integer)
			return time = (int) objectTime;
		else if (objectTime instanceof String)
			return time = RegexMatcher.INT.parse((String) objectTime);
		else
			return time = RegexMatcher.INT.parse(objectTime.toString());
	}

	public void setTime(Integer time) {
		if (time != null)
			putData("time", Utils.getCurrentTimeInSeconds() + time);
		else
			removeData("time");
	}
}
