package com.biomecompass.network;

import com.biomecompass.BiomeCompass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> Client: where the nearest biome is (or that none was found). */
public record BiomeResultPayload(String biomeId, boolean found, int x, int y, int z)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BiomeResultPayload> TYPE =
            new CustomPacketPayload.Type<>(BiomeCompass.id("biome_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeResultPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BiomeResultPayload::biomeId,
                    ByteBufCodecs.BOOL, BiomeResultPayload::found,
                    ByteBufCodecs.INT, BiomeResultPayload::x,
                    ByteBufCodecs.INT, BiomeResultPayload::y,
                    ByteBufCodecs.INT, BiomeResultPayload::z,
                    BiomeResultPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
