package com.biomecompass.client;

import com.biomecompass.network.FindBiomePayload;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/** Searchable, paged list of every biome in the registry (vanilla + modded). */
public class BiomeSelectScreen extends Screen {
    private static final int MAX_ROWS = 10;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;

    private final List<String> allBiomes;
    private List<String> filtered;
    private final Button[] rows = new Button[MAX_ROWS];
    private Button prevButton;
    private Button nextButton;
    private StringWidget pageLabel;
    private EditBox search;
    private String query = "";
    private int page = 0;
    private int perPage = MAX_ROWS;

    public BiomeSelectScreen(List<String> biomes) {
        super(Component.translatable("screen.biomecompass.title"));
        this.allBiomes = biomes;
        this.filtered = biomes;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        perPage = Math.max(3, Math.min(MAX_ROWS, (this.height - LIST_TOP - 34) / ROW_HEIGHT));

        addRenderableWidget(new StringWidget(cx - 130, 8, 260, 9, this.title, this.font).alignCenter());

        search = new EditBox(this.font, cx - 130, 22, 260, 20,
                Component.translatable("screen.biomecompass.search"));
        search.setHint(Component.translatable("screen.biomecompass.search"));
        search.setValue(query);
        search.setResponder(text -> {
            query = text;
            page = 0;
            applyFilter();
        });
        addRenderableWidget(search);
        setInitialFocus(search);

        for (int i = 0; i < MAX_ROWS; i++) {
            final int index = i;
            rows[i] = addRenderableWidget(
                    Button.builder(Component.empty(), button -> choose(index))
                            .bounds(cx - 130, LIST_TOP + i * ROW_HEIGHT, 260, 20)
                            .build());
        }

        int bottom = LIST_TOP + perPage * ROW_HEIGHT + 4;
        prevButton = addRenderableWidget(
                Button.builder(Component.literal("<"), button -> changePage(-1))
                        .bounds(cx - 130, bottom, 40, 20).build());
        nextButton = addRenderableWidget(
                Button.builder(Component.literal(">"), button -> changePage(1))
                        .bounds(cx + 90, bottom, 40, 20).build());
        pageLabel = addRenderableWidget(
                new StringWidget(cx - 85, bottom + 6, 170, 9, Component.empty(), this.font).alignCenter());

        applyFilter();
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filtered = allBiomes;
        } else {
            filtered = allBiomes.stream()
                    .filter(id -> id.toLowerCase(Locale.ROOT).contains(q)
                            || displayName(id).toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        refresh();
    }

    private void refresh() {
        int pages = Math.max(1, (filtered.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < MAX_ROWS; i++) {
            int idx = page * perPage + i;
            boolean show = i < perPage && idx < filtered.size();
            rows[i].visible = show;
            rows[i].active = show;
            if (show) {
                rows[i].setMessage(Component.literal(displayName(filtered.get(idx))));
            }
        }
        prevButton.active = page > 0;
        nextButton.active = page < pages - 1;
        pageLabel.setMessage(Component.literal((page + 1) + " / " + pages));
    }

    private void changePage(int delta) {
        page += delta;
        refresh();
    }

    private void choose(int rowIndex) {
        int idx = page * perPage + rowIndex;
        if (idx < 0 || idx >= filtered.size()) {
            return;
        }
        String id = filtered.get(idx);
        onClose();
        ClientPlayNetworking.send(new FindBiomePayload(id));
    }

    static String displayName(String id) {
        int colon = id.indexOf(':');
        String ns = colon < 0 ? "minecraft" : id.substring(0, colon);
        String path = colon < 0 ? id : id.substring(colon + 1);
        String key = "biome." + ns + "." + path.replace('/', '.');
        return I18n.exists(key) ? I18n.get(key) + " (" + id + ")" : id;
    }
}
