package mchorse.bbs_crowd.network;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class CrowdServerNetwork {
    public static final Identifier CLIENT_CROWD_MEMBERS = new Identifier("bbs_crowd", "c1");
    public static final Identifier CLIENT_CROWD_PRELOAD_READY = new Identifier("bbs_crowd", "c2");

    public static void sendCrowdMembers(ServerWorld world, IntList ids) {
        if (world == null || ids == null) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                buf.writeInt(ids.getInt(i));
            }
            ServerPlayNetworking.send(player, CLIENT_CROWD_MEMBERS, buf);
        }
    }

    public static void sendCrowdPreloadReady(ServerPlayerEntity player, String filmId) {
        if (player == null || filmId == null) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(filmId);
        ServerPlayNetworking.send(player, CLIENT_CROWD_PRELOAD_READY, buf);
    }
}
