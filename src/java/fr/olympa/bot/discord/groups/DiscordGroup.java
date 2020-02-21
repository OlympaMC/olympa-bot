package fr.olympa.bot.discord.groups;

import java.util.List;

import com.vdurmont.emoji.EmojiParser;

import fr.olympa.bot.discord.api.DiscordUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public enum DiscordGroup {
	
	ADMIN(558322950680346624L, "Ils sont très occupés et si vraiement tu as besoin de les contacter, passe par le forum.", false),
	MODO(558322952706326548L, "Erreur lié aux sanctions automatiques **uniquement**. Si tu as été un sanctionné par un membre du staff, passe par le forum", true),
	ASSISTANT(558322953314631690L, "Question ou autre demande. Il saura t'aider pour toute autre situation.", true),
	DEV(558322951250771978L, "Signalement de bugs, quelque soit la platforme (Minecraft, Site, Forum, Discord, Teamspeak ...).", true),
	ANIMATEUR(600766311169130496L, "Tous ce qui concerne les events.", true);
	;
	
	int idMinecraft;
	long idStaff;
	long idPublic;
	String supportDesc;
	boolean supportCanTag;

	private DiscordGroup(long idStaff, String supportDesc, boolean supportCanTag) {
		this.idStaff = idStaff;
		this.supportDesc = supportDesc;
		this.supportCanTag = supportCanTag;
	}

	public String getEmoji() {
		return this.getEmoji(this.getRole());
	}

	public String getEmoji(Role role) {
		String roleName = role.getName();
		List<String> roleEmote = EmojiParser.extractEmojis(roleName);
		return roleEmote.get(0);
	}

	public Role getRole() {
		Guild guild = DiscordUtils.getStaffGuild();
		return guild.getRoleById(this.idStaff);
	}

	public String getSupportDesc() {
		return this.supportDesc;
	}

	public boolean isSupportCanTag() {
		return this.supportCanTag;
	}

	public boolean isSupportShow() {
		return this.getSupportDesc() != null;
	}
	
	public void setSupportCanTag(boolean supportCanTag) {
		this.supportCanTag = supportCanTag;
	}

	public void setSupportDesc(String supportDesc) {
		this.supportDesc = supportDesc;
	}
}
