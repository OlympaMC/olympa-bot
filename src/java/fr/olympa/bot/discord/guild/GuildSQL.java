package fr.olympa.bot.discord.guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.StatementType;
import fr.olympa.api.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSQL {

	static String tableGuild = "discord.guilds";

	{
		OlympaStatement.formatTableName(tableGuild);
	}

	private static OlympaStatement insertGuildStatement = new OlympaStatement(StatementType.INSERT, tableGuild, new String[] { "guild_id", "guild_name" });

	public static OlympaGuild addGuild(Guild guild) throws SQLException {
		PreparedStatement statement = insertGuildStatement.getStatement();
		OlympaGuild olympaGuild = null;
		int i = 1;
		statement.setLong(i++, guild.getIdLong());
		statement.setString(i++, guild.getName());
		statement.executeUpdate();
		ResultSet resultSet = selectGuildIdStatement.executeQuery(statement);
		resultSet.next();
		olympaGuild = OlympaGuild.createObject(resultSet);
		resultSet.close();
		statement.close();
		return olympaGuild;
	}

	private static OlympaStatement selectGuildIdStatement = new OlympaStatement(StatementType.SELECT, tableGuild, "id", null);

	public static OlympaGuild selectGuildById(long id) throws SQLException {
		PreparedStatement statement = selectGuildIdStatement.getStatement();
		OlympaGuild olympaGuild = null;
		int i = 1;
		statement.setLong(i++, id);
		ResultSet resultSet = selectGuildIdStatement.executeQuery(statement);
		if (resultSet.next())
			olympaGuild = OlympaGuild.createObject(resultSet);
		resultSet.close();
		return olympaGuild;
	}

	private static OlympaStatement selectGuildsIdStatement = new OlympaStatement(StatementType.SELECT, tableGuild, (String[]) null, (String[]) null);

	public static List<OlympaGuild> selectGuilds() throws SQLException {
		PreparedStatement statement = selectGuildsIdStatement.getStatement();
		List<OlympaGuild> olympaGuilds = new ArrayList<>();
		ResultSet resultSet = selectGuildIdStatement.executeQuery(statement);
		while (resultSet.next())
			olympaGuilds.add(OlympaGuild.createObject(resultSet));
		resultSet.close();
		return olympaGuilds;
	}

	private static OlympaStatement updateGuildStatement = new OlympaStatement(StatementType.UPDATE, tableGuild, "id", new String[] {
			"guild_name", "log_voice", "log_msg", "log_username", "log_attachment", "log_roles", "log_entries", "log_insult", "status_message_enabled",
			"log_channel_id", "staff_channel_id", "bugs_channel_id", "minecraft_channel_id", "exclude_channels_ids",
			"guild_type" });

	public static void updateGuild(OlympaGuild olympaGuild) throws SQLException {
		PreparedStatement statement = updateGuildStatement.getStatement();
		int i = 1;
		statement.setString(i++, olympaGuild.getName());
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogVoice()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogMsg()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogUsername()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogAttachment()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogRoles()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogEntries()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isLogInsult()));
		statement.setLong(i++, Utils.booleanToBinary(olympaGuild.isStatusMessageEnabled()));
		if (olympaGuild.getLogChannelId() != 0)
			statement.setLong(i++, olympaGuild.getLogChannelId());
		else
			statement.setObject(i++, null);
		if (olympaGuild.getStaffChannelId() != 0)
			statement.setLong(i++, olympaGuild.getStaffChannelId());
		else
			statement.setObject(i++, null);
		if (olympaGuild.getBugsChannelId() != 0)
			statement.setLong(i++, olympaGuild.getBugsChannelId());
		else
			statement.setObject(i++, null);
		if (olympaGuild.getMinecraftChannelId() != 0)
			statement.setLong(i++, olympaGuild.getMinecraftChannelId());
		else
			statement.setObject(i++, null);
		if (!olympaGuild.getExcludeChannelsIds().isEmpty())
			statement.setString(i++, new Gson().toJson(olympaGuild.getExcludeChannelsIds()));
		else
			statement.setObject(i++, null);
		statement.setInt(i++, olympaGuild.getType().ordinal());
		statement.setLong(i++, olympaGuild.getId());
		selectGuildIdStatement.execute(statement);
		statement.close();
	}
}
