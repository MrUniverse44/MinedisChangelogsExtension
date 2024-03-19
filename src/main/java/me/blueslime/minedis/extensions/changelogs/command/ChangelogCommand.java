package me.blueslime.minedis.extensions.changelogs.command;

import me.blueslime.minedis.api.command.MinecraftCommand;
import me.blueslime.minedis.api.command.sender.Sender;
import me.blueslime.minedis.extensions.changelogs.MChangelogs;
import me.blueslime.minedis.extensions.changelogs.utils.commit.CommitCode;
import me.blueslime.minedis.extensions.changelogs.utils.embed.EmbedSection;
import me.blueslime.minedis.extensions.changelogs.utils.version.VersionConverter;
import me.blueslime.minedis.modules.discord.Controller;
import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangelogCommand extends MinecraftCommand {
    private final MChangelogs plugin;
    private final String name;
    public ChangelogCommand(MChangelogs plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public void execute(Sender sender, String[] args) {
        String senderName = sender.isPlayer() ? sender.toPlayer().getName() : "Console";
        String senderId = sender.isPlayer() ? sender.toPlayer().getUniqueId().toString() : "NONE";
        String senderUniqueId = senderId.replace("-", "");

        if (!sender.hasPermission("minedis.changelogs.use")) {
            return;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            String command = "&a/" + name + " ";

            sender.send(
                command + "&8- &fCommand list for the plugin",
                command + "help &8- &fCommand list for the plugin",
                command + "commit (project) (name) &8- &fAdd a change to your commit list",
                command + "push (project) &8- &fPush changes to the commit list",
                command + "release (project) (big-minor-fix) &8- &fRelease a new version for your project",
                command + "register (name) &8- &fRegister a new project",
                command + "projects &8- &fView all project list",
                command + "commits (name) &8- &fView commit list of a project",
                command + "reload &8- &fReloads the configuration"
            );
            return;
        }

        String argument = args[0].toLowerCase(Locale.ENGLISH);

        if (argument.equals("reload")) {
            plugin.reloadConfiguration();
            sender.send("&aPlugin-Addon has been reloaded.");
            return;
        }

        if (argument.equals("push")) {
            if (args.length == 1) {
                sender.send("&aWrong usage!&f See the command list with &2/" + name + " help");
                return;
            }
            String id = args[1].toLowerCase(Locale.ENGLISH);

            if (!plugin.getConfiguration().contains("projects." + id + ".name")) {
                sender.send("&cThis project don't exists.");
                return;
            }

            Configuration configuration = plugin.getConfiguration();

            Configuration section = configuration.getSection("projects." + id + ".commits");
            String projectName = configuration.getString("projects." + id + ".name", id);

            StringBuilder commits = new StringBuilder();

            String FORMAT = configuration.getString("settings.commit-format", "[`%id%`](https://github.com/MrUniverse44/Minedis) %commit name% - %author%");

            String split = "\n";

            int amount = 0;

            String guildID = configuration.getString("settings.push-channel-guild", "NOT_SET");
            String channelID = configuration.getString("settings.push-channel-id", "NOT_SET");

            if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET")) {
                sender.send("&cGuild for push is not set!");
                return;
            }

            if (channelID.isEmpty() || channelID.equalsIgnoreCase("NOT_SET")) {
                sender.send("&cChannel for push is not set!");
                return;
            }

            Guild guild = plugin.getPlugin().getModule(Controller.class).getBot().getClient().getGuildById(
                guildID
            );

            if (guild == null) {
                sender.send("&cGuild for push is not found!");
                return;
            }

            TextChannel textChannel = guild.getTextChannelById(
                channelID
            );

            StandardGuildMessageChannel channel;

            if (textChannel == null) {
                NewsChannel newsChannel = guild.getNewsChannelById(
                        channelID
                );
                if (newsChannel == null) {
                    sender.send("&cChannel for push is not found!");
                    return;
                }
                channel = newsChannel;
            } else {
                channel = textChannel;
            }

            TextReplacer replacer = TextReplacer.builder();

            for (String key : section.getKeys()) {
                String path = "projects." + id + ".commits." + key + ".";

                String tag = configuration.getString(path + "tag", "NOT_TAG");
                String name = configuration.getString(path + "name", key);
                boolean push = configuration.getBoolean(path + "public", false);

                if (!tag.equalsIgnoreCase("NOT_TAG") && !tag.equalsIgnoreCase("NOT_SET")) {
                    continue;
                }
                if (push) {
                    continue;
                }

                configuration.set(path + "public", true);

                commits.append(
                    FORMAT.replace(
                        "%id%",
                        key
                    ).replace(
                        "%commit name%",
                        name
                    ).replace(
                        "%name%",
                        name
                    ).replace(
                        "%commit id%",
                        key
                    ).replace(
                        "%commit author%",
                        senderName
                    ).replace(
                        "%commit author id%",
                        senderId
                    ).replace(
                        "%commit author unique id%",
                        senderUniqueId
                    ).replace(
                        "%author%",
                        senderName
                    ).replace(
                        "%author id%",
                        senderId
                    ).replace(
                        "%author unique id%",
                        senderUniqueId
                    ).replace(
                        "%project%",
                        projectName
                    ).replace(
                        "%amount%",
                        String.valueOf(amount)
                    )
                ).append(
                    split
                );

                amount++;
            }

            if (amount < 1) {
                sender.send("&cNo commits pending to push!");
                return;
            }

            channel.sendMessageEmbeds(
                new EmbedSection(
                    configuration.getSection("embeds.on-push")
                ).build(
                    replacer.replace(
                        "%commit_var%",
                        amount == 1 ? "commit" : "commits"
                    ).replace(
                        "%nick%",
                        senderName
                    ).replace(
                        "%author name%",
                        senderName
                    ).replace(
                        "%author%",
                        senderName
                    ).replace(
                        "%author id%",
                        senderId
                    ).replace(
                        "%author unique id%",
                        senderUniqueId
                    ).replace(
                        "%commits%",
                        commits.toString()
                    ).replace(
                        "%project%",
                        projectName
                    ).replace(
                        "%amount%",
                        String.valueOf(amount)
                    )
                )
            ).queue();

            sender.send("&aCommits published to your discord.");
            return;
        }

        if (argument.equals("release")) {
            if (args.length == 1 || args.length == 2) {
                sender.send("&aWrong usage!&f See the command list with &2/" + name + " help");
                return;
            }

            String id = args[1].toLowerCase(Locale.ENGLISH);
            String converter = args[2].toLowerCase(Locale.ENGLISH);

            if (!plugin.getConfiguration().contains("projects." + id + ".name")) {
                sender.send("&cThis project don't exists.");
                return;
            }

            if (!VersionConverter.Conversion.isConversion(converter)) {
                sender.send("&cWrong usage! &fPlease use &6BIG&f, &6MINOR&f or &6FIX&f.");
                return;
            }

            Configuration configuration = plugin.getConfiguration();

            Configuration section = configuration.getSection("projects." + id + ".commits");
            String projectName = configuration.getString("projects." + id + ".name", id);

            StringBuilder commits = new StringBuilder();

            String FORMAT = configuration.getString("settings.commit-format", "[`%id%`](https://github.com/MrUniverse44/Minedis) %commit name% - %author%");

            String split = "\n";

            int amount = 0;

            String guildID = configuration.getString("settings.version-release-channel-guild", "NOT_SET");
            String channelID = configuration.getString("settings.version-release-channel-id", "NOT_SET");

            if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET")) {
                sender.send("&cGuild for release is not set!");
                return;
            }

            if (channelID.isEmpty() || channelID.equalsIgnoreCase("NOT_SET")) {
                sender.send("&cChannel for release is not set!");
                return;
            }

            Guild guild = plugin.getPlugin().getModule(Controller.class).getBot().getClient().getGuildById(
                    guildID
            );

            if (guild == null) {
                sender.send("&cGuild for release is not found!");
                return;
            }

            TextChannel textChannel = guild.getTextChannelById(
                    channelID
            );

            StandardGuildMessageChannel channel;

            if (textChannel == null) {
                NewsChannel newsChannel = guild.getNewsChannelById(
                        channelID
                );
                if (newsChannel == null) {
                    sender.send("&cChannel for release is not found!");
                    return;
                }
                channel = newsChannel;
            } else {
                channel = textChannel;
            }

            String currentVersion = configuration.getString("projects." + id + ".version", "0.0.1");

            String newVersion = VersionConverter.convert(VersionConverter.Conversion.fromString(converter), currentVersion);

            TextReplacer replacer = TextReplacer.builder();

            for (String key : section.getKeys()) {
                String path = "projects." + id + ".commits." + key + ".";

                String tag = configuration.getString(path + "tag", "NOT_TAG");
                String name = configuration.getString(path + "name", key);

                if (!tag.equalsIgnoreCase("NOT_TAG") && !tag.equalsIgnoreCase("NOT_SET")) {
                    continue;
                }

                configuration.set(path + "tag", newVersion);

                commits.append(
                    FORMAT.replace(
                        "%id%",
                        key
                    ).replace(
                        "%commit name%",
                        name
                    ).replace(
                        "%name%",
                        name
                    ).replace(
                        "%commit id%",
                        key
                    ).replace(
                        "%commit author%",
                        senderName
                    ).replace(
                        "%commit author id%",
                        senderId
                    ).replace(
                        "%commit author unique id%",
                        senderUniqueId
                    ).replace(
                        "%author%",
                        senderName
                    ).replace(
                        "%author id%",
                        senderId
                    ).replace(
                        "%author unique id%",
                        senderUniqueId
                    ).replace(
                        "%project%",
                        projectName
                    ).replace(
                        "%amount%",
                        String.valueOf(amount)
                    )
                ).append(
                    split
                );

                amount++;
            }

            if (amount < 1) {
                sender.send("&cNo commits pending to make a new release!");
                return;
            }

            configuration.set("projects." + id + ".version", newVersion);

            plugin.saveConfiguration();

            channel.sendMessageEmbeds(
                new EmbedSection(
                        configuration.getSection("embeds.on-version-release")
                ).build(
                    replacer.replace(
                        "%commit_var%",
                        amount == 1 ? "commit" : "commits"
                    ).replace(
                        "%nick%",
                        senderName
                    ).replace(
                        "%author name%",
                        senderName
                    ).replace(
                        "%author%",
                        senderName
                    ).replace(
                        "%author id%",
                        senderId
                    ).replace(
                        "%author unique id%",
                        senderUniqueId
                    ).replace(
                        "%commits%",
                        commits.toString()
                    ).replace(
                        "%project%",
                        projectName
                    ).replace(
                        "%amount%",
                        String.valueOf(amount)
                    )
                )
            ).queue();

            sender.send("&aNew release uploaded to your discord: " + newVersion);
            return;
        }

        if (argument.equals("commit")) {
            if (args.length == 1 || args.length == 2) {
                sender.send("&aWrong usage!&f See the command list with &2/" + name + " help");
                return;
            }
            String id = args[1].toLowerCase(Locale.ENGLISH);

            if (!plugin.getConfiguration().contains("projects." + id + ".name")) {
                sender.send("&cThis project don't exists.");
                return;
            }
            StringBuilder builder = new StringBuilder();

            for (int i = 2; i < args.length; i++) {
                builder.append(args[i]).append(" ");
            }

            String code = CommitCode.generate();
            String name = builder.toString();

            String path = "projects." + id + ".commits." + code + ".";

            plugin.getConfiguration().set(path + "tag", "NOT_SET");
            plugin.getConfiguration().set(path + "name", name);
            plugin.getConfiguration().set(path + "public", false);
            plugin.saveConfiguration();

            sender.send("&aCommit id:", "&f" + code, "&aCommit content:", "&f" + name, "&aCommit registered!");
            return;
        }

        if (argument.equals("commits")) {
            if (args.length == 1) {
                sender.send("&aWrong usage!&f See the command list with &2/" + name + " help");
                return;
            }
            String id = args[1].toLowerCase(Locale.ENGLISH);

            if (!plugin.getConfiguration().contains("projects." + id + ".commits")) {
                sender.send("&cThis project don't have commits yet!");
                return;
            }

            Configuration configuration = plugin.getConfiguration();

            Configuration section = configuration.getSection("projects." + id + ".commits");

            StringBuilder commits = new StringBuilder();

            Map<String, Integer> tagMap = new HashMap<>();

            String published = "&b&lPUBLIC &3Commit ";
            String local = "&e&lLOCAL &6Commit ";
            String split = "\n";

            for (String key : section.getKeys()) {
                String path = "projects." + id + ".commits." + key + ".";

                String tag = configuration.getString(path + "tag", "NOT_TAG");
                String name = configuration.getString(path + "name", key);
                boolean push = configuration.getBoolean(path + "public", false);

                if (!tag.equalsIgnoreCase("NOT_TAG") && !tag.equalsIgnoreCase("NOT_SET")) {
                    int count = tagMap.getOrDefault(tag, 0);

                    count++;

                    tagMap.put(tag, count);
                    continue;
                }
                commits.append(push ? published : local)
                       .append(key)
                       .append("&f ")
                       .append(name)
                       .append(split);
            }

            StringBuilder tags = new StringBuilder();

            String specifiedTag = "&a&lTag ";

            for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                tags.append(specifiedTag)
                        .append(entry.getKey())
                        .append(split).append("&6+")
                        .append(entry.getValue())
                        .append(" &fcommits for this tag")
                        .append(split);
            }

            tags.append(
                "&6&lUnreleased commits:"
            ).append(
                split
            ).append(
                commits
            );

            sender.send(
                tags.toString()
            );
            return;
        }

        if (argument.equals("projects")) {
            Configuration section = plugin.getConfiguration().getSection("projects");

            if (section == null) {
                sender.send("&cNo projects registered yet.");
                return;
            }

            int size = 0;

            for (String key : section.getKeys()) {
                String name = plugin.getConfiguration().getString("projects." + key + ".name", key);
                String version = plugin.getConfiguration().getString("projects." + key + ".version", "0.0.1");

                sender.send(
                    "&7- &aProject: &6" + name + "&a Version: &6" + version
                );
                size++;
            }

            if (size == 0) {
                sender.send("&cNo projects registered yet.");
            } else {
                sender.send("&a" + size + " project" + (size == 1 ? "" : "s") + " found.");
            }
            return;
        }

        if (argument.equals("register")) {
            if (args.length == 1) {
                sender.send("&aWrong usage!&f See the command list with &2/" + name + " help");
                return;
            }
            String name = args[1];
            String id = args[1].toLowerCase(Locale.ENGLISH);

            if (plugin.getConfiguration().contains("projects." + id + ".name")) {
                sender.send("&cThis project already exists.");
                return;
            }

            plugin.getConfiguration().set("projects." + id + ".name", name);
            plugin.getConfiguration().set("projects." + id + ".version", "0.0.1");
            plugin.saveConfiguration();
            sender.send("&aProject has been registered.");
            return;
        }
    }
}
