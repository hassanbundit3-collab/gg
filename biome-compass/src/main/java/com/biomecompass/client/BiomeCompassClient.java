package com.biomecompass.client;

import com.biomecompass.BiomeCompass;
import com.biomecompass.network.BiomeResultPayload;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public class BiomeCompassClient implements ClientModInitializer {
    private static final String[] ARROWS = {
            "\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"
    };

    private static KeyMapping openKey;

    private static BlockPos target;
    private static ClientLevel targetLevel;
    private static String targetName = "";
    private static int tickCounter;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(BiomeCompass.id("main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.biomecompass.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_B, category));

        ClientPlayNetworking.registerGlobalReceiver(BiomeResultPayload.TYPE, (payload, context) -> {
            Minecraft mc = context.client();
            if (!payload.found()) {
                target = null;
                mc.gui.hud.setOverlayMessage(Component.translatable("msg.biomecompass.not_found"), false);
                return;
            }
            target = new BlockPos(payload.x(), payload.y(), payload.z());
            targetLevel = mc.level;
            targetName = BiomeSelectScreen.displayName(payload.biomeId());
            tickCounter = 0;
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.translatable("msg.biomecompass.located",
                        targetName, payload.x(), payload.y(), payload.z()));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(BiomeCompassClient::onTick);
    }

    private static void onTick(Minecraft mc) {
        while (openKey.consumeClick()) {
            if (mc.level != null && mc.gui.screen() == null) {
                mc.gui.setScreen(new BiomeSelectScreen(collectBiomes(mc.level)));
            }
        }
        updateDirectionHint(mc);
    }

    private static List<String> collectBiomes(ClientLevel level) {
        var registry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        List<String> ids = new ArrayList<>();
        for (var id : registry.keySet()) {
            ids.add(id.toString());
        }
        ids.sort(String::compareTo);
        return ids;
    }

    private static void updateDirectionHint(Minecraft mc) {
        if (target == null || mc.player == null || mc.level == null) {
            return;
        }
        if (mc.level != targetLevel) {
            target = null;
            return;
        }
        if (++tickCounter % 5 != 0) {
            return;
        }

        double dx = target.getX() + 0.5 - mc.player.getX();
        double dz = target.getZ() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 16) {
            target = null;
            mc.gui.hud.setOverlayMessage(Component.translatable("msg.biomecompass.arrived"), false);
            return;
        }

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = (targetYaw - mc.player.getYRot()) % 360f;
        if (rel > 180f) rel -= 360f;
        if (rel < -180f) rel += 360f;
        int index = Math.floorMod(Math.round(rel / 45f), 8);

        mc.gui.hud.setOverlayMessage(Component.translatable("msg.biomecompass.tracking",
                targetName, ARROWS[index], (int) dist), false);
    }
}
