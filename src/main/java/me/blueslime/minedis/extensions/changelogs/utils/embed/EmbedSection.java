package me.blueslime.minedis.extensions.changelogs.utils.embed;

import me.blueslime.minedis.extensions.changelogs.utils.color.ColorUtils;
import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.config.Configuration;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbedSection {
    private static final TextReplacer EMPTY = TextReplacer.builder();
    private final List<EmbedField> fieldList = new ArrayList<>();
    private String description = "";
    private String thumbnail = null;
    private String footer = null;
    private String author = null;
    private String title = null;
    private String image = null;
    private String url = null;
    private String color = "YELLOW";

    public EmbedSection(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        if (configuration.contains("description")) {
            description = configuration.getString("description", " ");
        }
        if (configuration.contains("footer")) {
            footer = configuration.getString("footer", " ");
        }
        if (configuration.contains("title")) {
            title = configuration.getString("title", " ");
        }
        if (configuration.contains("color")) {
            color = configuration.getString("color", "YELLOW");
        }
        if (configuration.contains("thumbnail")) {
            thumbnail = configuration.getString("thumbnail", "");
        }
        if (configuration.contains("image")) {
            image = configuration.getString("image", "");
        }
        if (configuration.contains("author")) {
            author = configuration.getString("author", "");
        }
        if (configuration.contains("url")) {
            url = configuration.getString("url", "");
        }

        if (configuration.contains("fields")) {
            for (String key : configuration.getSection("fields").getKeys()) {
                fieldList.add(
                        new EmbedField(
                                key,
                                configuration.getBoolean("fields." + key + ".inline", true),
                                configuration.getString("fields." + key + ".name", " "),
                                configuration.getString("fields." + key + ".value", " ")
                        )
                );
            }
        }
    }


    public static EmbedSection builder() {
        return new EmbedSection(null);
    }

    public EmbedSection description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public EmbedSection footer(String footer) {
        this.footer = footer;
        return this;
    }

    public EmbedSection title(String title) {
        this.title = title;
        return this;
    }

    public EmbedSection color(String color) {
        this.color = color;
        return this;
    }

    public EmbedSection thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public EmbedSection image(String image) {
        this.image = image;
        return this;
    }

    public EmbedSection author(String author) {
        this.author = author;
        return this;
    }

    public EmbedSection url(String url) {
        this.url = url;
        return this;
    }

    public EmbedSection fields(EmbedField... fields) {
        fieldList.addAll(Arrays.asList(fields));
        return this;
    }

    public EmbedSection serialize(Configuration configuration, String path) {

        path = path.endsWith(".") ? path : path + ".";

        if (configuration == null) {
            return this;
        }
        configuration.set(path + "description", description);
        configuration.set(path + "title", title == null ? "" : title);
        configuration.set(path + "color", color == null ? "YELLOW" : color);
        if (footer != null) {
            configuration.set(path + "footer", footer);
        }
        if (thumbnail != null) {
            configuration.set(path + "thumbnail", thumbnail);
        }
        if (image != null) {
            configuration.set(path + "image", image);
        }
        if (url != null) {
            configuration.set(path + "url", url);
        }
        if (author != null) {
            configuration.set(path + "author", author);
        }
        if (footer != null) {
            configuration.set(path + "footer", footer);
        }

        for (EmbedField field : fieldList) {
            configuration.set(path + "fields." + field.getId() + ".inline", field.isInline());
            configuration.set(path + "fields." + field.getId() + ".name", field.getName());
            configuration.set(path + "fields." + field.getId() + ".value", field.getValue());
        }
        return this;
    }

    public void finish() {

    }

    public MessageEmbed build() {
        return build(EMPTY);
    }

    public Color getColor() {
        return ColorUtils.getColor(color);
    }

    public MessageEmbed build(TextReplacer replacer) {

        EmbedBuilder builder = new EmbedBuilder().setColor(getColor());

        replacer = replacer.replace("\\n","\n");

        if (description.isEmpty()) {
            builder.setDescription(" ");
        } else {
            builder.setDescription(
                    replacer.apply(description)
            );
        }

        if (thumbnail != null) {
            builder.setThumbnail(
                    replacer.apply(
                            thumbnail
                    )
            );
        }

        if (footer != null) {
            String[] split = footer.split("<split>");

            if (split.length == 1) {
                builder.setFooter(
                        replacer.apply(footer)
                );
            } else {
                builder.setFooter(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            }
        }

        if (!fieldList.isEmpty()) {
            for (EmbedField field : fieldList) {
                builder.addField(
                        replacer.apply(field.getName()),
                        replacer.apply(field.getValue()),
                        field.isInline()
                );
            }
        }

        if (author != null) {
            String[] split = author.split("<split>");

            if (split.length == 1) {
                builder.setAuthor(
                        replacer.apply(author)
                );
            } else if (split.length == 2) {
                builder.setAuthor(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            } else {
                builder.setAuthor(
                        replacer.apply(split[0]),
                        replacer.apply(split[1]),
                        replacer.apply(split[2])
                );
            }

        }

        if (title != null) {
            String[] split = title.split("<split>");
            if (split.length == 1) {
                builder.setTitle(
                        replacer.apply(title)
                );
            } else {
                builder.setTitle(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            }
        }

        if (image != null) {
            builder.setImage(
                    replacer.apply(image)
            );
        }

        if (url != null) {
            builder.setUrl(
                    replacer.apply(url)
            );
        }

        return builder.build();
    }

    public EmbedSection timestamp() {
        return this;
    }

    public static class EmbedField {

        private final boolean inline;
        private final String value;
        private final String name;
        private final String id;

        public EmbedField(String id, boolean inline, String name, String value) {
            this.inline = inline;
            this.value = value;
            this.name = name;
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public boolean isInline() {
            return inline;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }
}


