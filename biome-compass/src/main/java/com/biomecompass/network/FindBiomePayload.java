package com.biomecompass.network;

import com.biomecompass.BiomeCompass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> Server: ask for the nearest biome with this id. */
public record FindBiomePayload(String biomeId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FindBiomePayload> TYPE =
            new CustomPacketPayload.Type<>(BiomeCompass.id("find_biome"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FindBiomePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, FindBiomePayload::biomeId,
                    FindBiomePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
