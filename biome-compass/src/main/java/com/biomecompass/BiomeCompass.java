package com.biomecompass;

import com.biomecompass.network.BiomeResultPayload;
import com.biomecompass.network.FindBiomePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BiomeCompass implements ModInitializer {
    public static final String MOD_ID = "biomecompass";

    // Same numbers vanilla /locate biome uses.
    private static final int SEARCH_RADIUS = 6400;
    private static final int HORIZONTAL_STEP = 32;
    private static final int VERTICAL_STEP = 64;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(FindBiomePayload.TYPE, FindBiomePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiomeResultPayload.TYPE, BiomeResultPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FindBiomePayload.TYPE, BiomeCompass::handleFind);
    }

    private static void handleFind(FindBiomePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        String rawId = payload.biomeId();

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        Identifier biomeId = Identifier.tryParse(rawId);
        if (biomeId == null) {
            ServerPlayNetworking.send(player, new BiomeResultPayload(rawId, false, 0, 0, 0));
            return;
        }

        // Same search that powers /locate biome. Works for biomes added by other mods too.
        var result = level.findClosestBiome3d(
                holder -> holder.is(biomeId),
                player.blockPosition(),
                SEARCH_RADIUS,
                HORIZONTAL_STEP,
                VERTICAL_STEP);

        if (result == null) {
            ServerPlayNetworking.send(player, new BiomeResultPayload(rawId, false, 0, 0, 0));
            return;
        }

        BlockPos pos = result.getFirst();
        ServerPlayNetworking.send(player,
                new BiomeResultPayload(rawId, true, pos.getX(), pos.getY(), pos.getZ()));
    }
}
