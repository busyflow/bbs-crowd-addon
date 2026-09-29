package mchorse.bbs_crowd.network;

import mchorse.bbs_mod.actions.types.crowd.CrowdClientMembers;
import mchorse.bbs_mod.actions.types.crowd.CrowdExportPreload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CrowdClientNetwork {
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(CrowdServerNetwork.CLIENT_CROWD_MEMBERS, (client, handler, buf, responseSender) -> {
            int count = buf.readInt();
            int[] ids = new int[count];
            for (int i = 0; i < count; i++) {
                ids[i] = buf.readInt();
            }
            client.execute(() -> CrowdClientMembers.add(ids));
        });

        ClientPlayNetworking.registerGlobalReceiver(CrowdServerNetwork.CLIENT_CROWD_PRELOAD_READY, (client, handler, buf, responseSender) -> {
            String filmId = buf.readString();
            client.execute(() -> CrowdExportPreload.serverFinished(filmId));
        });
    }
}
