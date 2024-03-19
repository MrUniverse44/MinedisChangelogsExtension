package me.blueslime.minedis.extensions.changelogs;

import me.blueslime.minedis.api.extension.MinedisExtension;
import me.blueslime.minedis.extensions.changelogs.command.ChangelogCommand;
import me.blueslime.minedis.extensions.changelogs.utils.embed.EmbedSection;
import net.md_5.bungee.config.Configuration;

public final class MChangelogs extends MinedisExtension {

    @Override
    public String getIdentifier() {
        return "MChangelogs";
    }

    @Override
    public String getName() {
        return "Minedis Changelogs";
    }

    @Override
    public void onEnabled() {
        Configuration configuration = getConfiguration();

        serialize(
            "embeds.on-push",
            EmbedSection.builder()
                .author("%nick%<split>https://github.com/MrUniverse44/Minedis<split>https://minotar.net/avatar/%nick%/100.png")
                .title("[%project%] %amount% new %commit_var%<split>https://github.com/MrUniverse44/Minedis")
                .description("%commits%")
        );

        serialize(
            "embeds.on-version-release",
            EmbedSection.builder()
                .author("[%project%] New version has been released<split>https://github.com/MrUniverse44/Minedis<split>https://minotar.net/avatar/%nick%/100.png")
                .description("%commits%")
        );

        if (!configuration.contains("settings.commit-format")) {
            configuration.set("settings.commit-format", "[`%id%`](https://github.com/MrUniverse44/Minedis) %commit name% - %author%");
        }

        if (!configuration.contains("settings.push-channel-id")) {
            configuration.set("settings.push-channel-id", "NOT_SET");
        }

        if (!configuration.contains("settings.push-channel-guild")) {
            configuration.set("settings.push-channel-guild", "NOT_SET");
        }

        if (!configuration.contains("settings.version-release-channel-id")) {
            configuration.set("settings.version-release-channel-id", "NOT_SET");
        }

        if (!configuration.contains("settings.version-release-channel-guild")) {
            configuration.set("settings.version-release-channel-guild", "NOT_SET");
        }

        saveConfiguration();

        registerMinecraftCommand(
            new ChangelogCommand(this, "changelogs")
        );

        registerMinecraftCommand(
            new ChangelogCommand(this, "cl")
        );
    }

    public void serialize(String path, EmbedSection section) {
        if (!getConfiguration().contains(path)) {
            section.serialize(getConfiguration(), path).finish();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("All listeners are unloaded from Minedis Online");
    }
}
