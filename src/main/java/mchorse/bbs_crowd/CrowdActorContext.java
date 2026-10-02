package mchorse.bbs_crowd;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class CrowdActorContext {
    public static Map<String, LivingEntity> currentActors;
    public static ServerPlayerEntity currentRecordingPlayer;
}
