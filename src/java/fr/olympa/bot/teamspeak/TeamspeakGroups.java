package fr.olympa.bot.teamspeak;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.bot.OlympaBots;

public enum TeamspeakGroups {

	SUPERADMIN("SuperAdmin"),
	ADMIN("P-Administrateur", OlympaGroup.FONDA, OlympaGroup.ADMIN),
	RESP_TECH("P-Resp Technique", OlympaGroup.RESP_TECH),
	MODP("P-Modérateur+", OlympaGroup.MODP),
	MOD("P-Modérateur", OlympaGroup.MOD),
	ASSISTANT("P-Assistant", OlympaGroup.ASSISTANT),
	DEV("P-Développeur", OlympaGroup.DEV, OlympaGroup.RESP_TECH),
	ANIMATOR("P-Animateur", OlympaGroup.ANIMATOR),
	BUILDER("P-Buildeur", OlympaGroup.BUILDER),
	TITLE("--- TITRE ---", true),
	PERMISSION("--- PERMISSIONS ---", true),
	SETTINGS("--- PARAMETRES ---", true),
	CUSTOM(null);

	String name;
	OlympaGroup[] groups;
	boolean isSeperator = false;

	private TeamspeakGroups(String name, OlympaGroup... groups) {
		this.name = name;
		this.groups = groups;
	}

	private TeamspeakGroups(String name, boolean isSeperator) {
		this.name = name;
		this.isSeperator = isSeperator;
	}

	public TeamspeakGroups name(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public boolean isStaff() {
		return groups != null && groups.length != 0;
	}

	public boolean hasPermission(Client client) {
		return OlympaBots.getInstance().getTeamspeak().getQuery().getServerGroups().stream().anyMatch(sg -> client.isInServerGroup(sg.getId()) && sg.getName().equalsIgnoreCase(name));
	}

	public static boolean isStaff(Client client) {
		return OlympaBots.getInstance().getTeamspeak().getQuery().getServerGroups().stream()
				.anyMatch(sg -> client.isInServerGroup(sg.getId()) && Arrays.stream(TeamspeakGroups.values()).anyMatch(ts -> ts.isStaff() && sg.getName().equalsIgnoreCase(ts.getName())));
	}

	public static List<TeamspeakGroups> getSeperators() {
		return Arrays.stream(TeamspeakGroups.values()).filter(tg -> tg.isSeperator).toList();
	}

	public static List<TeamspeakGroups> get(OlympaGroup group) {
		return Arrays.stream(TeamspeakGroups.values()).filter(tg -> tg.groups != null && Arrays.stream(tg.groups).anyMatch(g -> g.getId() == group.getId())).toList();
	}

	public static List<TeamspeakGroups> get(Collection<OlympaGroup> groups) {
		return Arrays.stream(TeamspeakGroups.values()).filter(tg -> tg.groups != null && Arrays.stream(tg.groups).anyMatch(g -> groups.stream().anyMatch(gs -> g.getId() == gs.getId()))).toList();
	}

}
