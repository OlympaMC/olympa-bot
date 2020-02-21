package fr.olympa.bot.discord.old;

public class Old {
	/*try {
	jda = builder.buildAsync();
	ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> updateConnected(), 0, 1, TimeUnit.MINUTES);
	} catch(LoginException e) {
	e.printStackTrace();
	}*//*public static void updateConnected() {
		int co = ProxyServer.getInstance().getOnlineCount();
		JDA jda = OlympaDiscord.jda;
		jda.getPresence().setGame(Game.playing(co + " connecté" + Utils.withOrWithoutS(co) + " | play.olympamc.fr"));
		}
		
		public static void checkName(Guild guild, Member member) {
		List<TextChannel> channels = guild.getTextChannelsByName("bot", false);
		TextChannel channel;
		if(!channels.isEmpty()) {
		channel = channels.get(0);
		} else {
		channel = guild.getDefaultChannel();
		}
		
		GuildController guildController = guild.getController();
		
		OlympaPlayer olympaPlayer = MySQL.getPlayer(member.getEffectiveName());
		if(olympaPlayer == null) {
		guildController.removeRolesFromMember(member, member.getRoles()).queue();
		channel.sendMessage(member.getAsMention() + " ➤ " + member.getEffectiveName() + " n'est pas connu par OlympaMC, merci d'utiliser le même pseudo que en jeux").queue();
		return;
		}
		}
		
		public static void setRole(Member author, Guild guild, MessageChannel channel, String memberName) {
		List<Member> members = guild.getMembersByEffectiveName(memberName, false);
		
		if(members.isEmpty()) {
		OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le pseudo " + memberName + " est introuvable sur ce Discord.");
		return;
		}
		
		if(members.size() > 1) {
		OlympaDiscord.sendTempMessageToChannel(channel,
				author.getAsMention() + " ➤ Le pseudo " + memberName + " est associé à plusieurs comptes: " + members.stream().map(member -> member.getEffectiveName()).collect(
						Collectors.joining(", ")) + ".");
		return;
		}
		
		Member member = members.get(0);
		
		OlympaPlayer olympaPlayer = MySQL.getPlayer(member.getEffectiveName());
		if(olympaPlayer == null) {
		OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le pseudo " + member.getEffectiveName() + " est introuvable dans la base de donnés minecraft OlympaMC.");
		return;
		}
		GuildController guildController = guild.getController();
		
		List<Role> roles = jda.getRolesByName(olympaPlayer.getGroup().getName(), true);
		
		if(roles.isEmpty()) {
		OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ Le rôle " + olympaPlayer.getGroup().getName() + " n'existe pas sur ce Discord.");
		return;
		}
		
		Role role = roles.get(0);
		
		
		guildController.addSingleRoleToMember(member, role).queue();
		OlympaDiscord.sendTempMessageToChannel(channel, author.getAsMention() + " ➤ " + member.getEffectiveName() + " a désormais le rôle " + role.getName());
		}*/
}
