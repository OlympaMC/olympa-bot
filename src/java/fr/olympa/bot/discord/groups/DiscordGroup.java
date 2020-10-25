package fr.olympa.bot.discord.groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vdurmont.emoji.EmojiParser;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public enum DiscordGroup {

	FONDA(OlympaGroup.FONDA, 610410821868060673L, 606161934126940193L, null, false),
	ADMIN(OlympaGroup.ADMIN, 558322950680346624L, 544594116932272139L, "Ils sont très occupés, utilise plutôt le forum pour discuter avec eux.", false),
	MODP(OlympaGroup.MODP, 558322952009941012L, 558179276973932546L, null, false),
	RESP_TECH(OlympaGroup.RESP_TECH, 571755659557601290L, 600770206192762908L, null, false),
	MOD(OlympaGroup.MOD, 558322952706326548L, 545830186738909195L, "Erreur liée aux sanctions automatiques uniquement. Si tu as été sanctionné par un membre du staff, passe par le forum.", true),
	ASSISTANT(OlympaGroup.ASSISTANT, 558322953314631690L, 558168138848403456L, "Question ou autre demande. Il saura t'aider pour toute autre situation.", true),
	RESP_STAFF(OlympaGroup.RESP_STAFF, 600766523354644491L, 600770102006120452L, null, false),
	RESP_COM(OlympaGroup.RESP_COM, 731215969128808508L, 731225340655173632L, null, false),
	RESP_BUILDER(OlympaGroup.RESP_BUILDER, 570320569443155983L, 560935286289203202L, null, false),
	RESP_ANIMATION(OlympaGroup.RESP_ANIMATION, 606158641355292723L, 606162089903521827L, null, false),
	GAMEMASTER(OlympaGroup.GAMEMASTER, 730161035088101416L, 730164373418541057L, "Question spécifique au fonctionnement d'un de nos jeux Minecraft.", true),
	DEVP(OlympaGroup.DEVP, 558322951250771978L, 0, null, false),
	DEV(OlympaGroup.DEV, 558322951250771978L, 558441264140386304L, "Signalement de bugs, quelque soit la platforme (Minecraft, Site, Forum, Discord, Teamspeak ...).", true),
	BUILDER(OlympaGroup.BUILDER, 558322957798080514L, 558441911271161867L, null, false),
	ANIMATEUR(OlympaGroup.ANIMATOR, 600766311169130496L, 620711429942804512L, "Tous ce qui concerne les events.", true),
	GRAPHISTE(OlympaGroup.GRAPHISTE, 558322958905638944L, 558442057174089740L, null, false),
	FRIEND(OlympaGroup.FRIEND, 603012821630058498L, 558168724708786187L, null, false),
	YOUTUBER(OlympaGroup.YOUTUBER, 558322955386617858L, 558570424288542740L, null, false),
	MINI_YOUTUBER(OlympaGroup.MINI_YOUTUBER, 558322956070289430L, 558440310108323874L, null, false),
	PLAYER(OlympaGroup.PLAYER, 0, 558334380393627670L, null, false),
	SIGNED(679992766117183494L, 0, null, false),
	ABSENT(624938102313582593L, 0, null, false),
	WARNING1(593166910082777141L, 0, null, false),
	WARNING2(593167088617652329L, 0, null, false),
	NITRO(682522717563518996L, 587672022382018569L, null, false),
	OLDER(0, 684335098312917026L, null, false),
	BETA_ACCES(0, 764195523396763658L, null, false),
	MUTED(0, 566627971276865576L, null, false);

	public static Set<DiscordGroup> get(Collection<OlympaGroup> groups) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> groups.stream().anyMatch(g -> dg.getOlympaGroup() != null && g.getId() == dg.getOlympaGroup().getId())).collect(Collectors.toSet());
	}

	public static DiscordGroup get(Guild guild, String emoji) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> dg.hasRoleId(guild) && dg.getRole(guild).getName().startsWith(emoji)).findFirst().orElse(null);
	}

	public static Set<Role> getSecondsRoles(Guild guild) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> dg.getOlympaGroup() == null && dg.hasRoleId(guild)).map(dg -> dg.getRole(guild)).collect(Collectors.toSet());
	}

	public static DiscordGroup get(long id) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> dg.idStaff == id || dg.idPublic == id).findFirst().orElse(null);
	}

	public static DiscordGroup get(OlympaGroup group) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> dg.getOlympaGroup() != null && dg.getOlympaGroup().getId() == group.getId()).findFirst().orElse(null);
	}

	public static DiscordGroup get(Role role) {
		return Arrays.stream(DiscordGroup.values()).filter(dg -> {
			Role r = dg.getRole(role.getGuild());
			return r != null && r.getIdLong() == role.getIdLong();
		}).findFirst().orElse(DiscordGroup.PLAYER);
	}

	public static String getEmoji(Role role) {
		String roleName = role.getName();
		List<String> roleEmote = EmojiParser.extractEmojis(roleName);
		return roleEmote.get(0);
	}

	public static boolean isStaff(Collection<Role> roles) {
		return roles.stream().anyMatch(DiscordGroup::isStaff);
	}

	public static boolean isStaff(Member member) {
		return member.getRoles().stream().anyMatch(role -> get(role).isStaff());
	}

	public static boolean isStaff(Role role) {
		return get(role).isStaff();
	}

	OlympaGroup olympaGroup;
	long idStaff;
	long idPublic;
	String supportDesc;
	boolean supportCanTag;

	private DiscordGroup(long idStaff, long idPublic, String supportDesc, boolean supportCanTag) {
		this.idStaff = idStaff;
		this.idPublic = idPublic;
		this.supportDesc = supportDesc;
		this.supportCanTag = supportCanTag;
	}

	private DiscordGroup(OlympaGroup olympaGroup, long idStaff, long idPublic, String supportDesc, boolean supportCanTag) {
		this.olympaGroup = olympaGroup;
		this.idStaff = idStaff;
		this.idPublic = idPublic;
		this.supportDesc = supportDesc;
		this.supportCanTag = supportCanTag;
	}

	public String getEmoji(Guild guild) {
		return getEmoji(getRole(guild));
	}

	public OlympaGroup getOlympaGroup() {
		return olympaGroup;
	}

	public boolean hasRoleId(Guild guild) {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() == DiscordGuildType.STAFF)
			return idStaff != 0;
		else if (olympaGuild.getType() == DiscordGuildType.PUBLIC)
			return idPublic != 0;
		else
			return false;
	}

	public Role getRole(Guild guild) {
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (idStaff != 0 && olympaGuild.getType() == DiscordGuildType.STAFF)
			return guild.getRoleById(idStaff);
		else if (idPublic != 0 && olympaGuild.getType() == DiscordGuildType.PUBLIC)
			return guild.getRoleById(idPublic);
		else
			return null;
	}

	public String getSupportDesc() {
		return supportDesc;
	}

	public boolean isStaff() {
		return idStaff != 0L && olympaGroup != null && olympaGroup.getPower() >= OlympaGroup.GRAPHISTE.getPower();
	}

	public boolean isSupportCanTag() {
		return supportCanTag;
	}

	public boolean isSupportShow() {
		return getSupportDesc() != null;
	}

	public void setSupportCanTag(boolean supportCanTag) {
		this.supportCanTag = supportCanTag;
	}

	public void setSupportDesc(String supportDesc) {
		this.supportDesc = supportDesc;
	}

}
