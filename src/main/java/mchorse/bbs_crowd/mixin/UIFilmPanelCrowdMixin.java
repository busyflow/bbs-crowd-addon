package mchorse.bbs_crowd.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import mchorse.bbs_crowd.access.FilmCrowdAccess;
import mchorse.bbs_crowd.access.ReplayCrowdChannels;
import mchorse.bbs_mod.actions.crowd.CrowdPaint;
import mchorse.bbs_mod.actions.crowd.CrowdWalk;
import mchorse.bbs_mod.actions.types.area.ValueAreaCells;
import mchorse.bbs_mod.actions.types.crowd.CrowdBehaviorActionClip;
import mchorse.bbs_mod.actions.types.crowd.CrowdFormation;
import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import mchorse.bbs_mod.graphics.Draw;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.crowds.Crowds;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.clips.area.AreaBrush;
import mchorse.bbs_mod.ui.film.crowds.CrowdSelection;
import mchorse.bbs_mod.ui.film.replays.UIReplaysEditor;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdWalkKeyframeFactory;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import mchorse.bbs_mod.utils.keyframes.KeyframeSegment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = UIFilmPanel.class, remap = false)
public abstract class UIFilmPanelCrowdMixin {
    @Shadow
    public UIReplaysEditor replayEditor;

    @Shadow
    public abstract int getCursor();

    @Inject(method = "renderInWorld", at = @At("RETURN"))
    private void bbs_crowd$onRenderInWorld(WorldRenderContext context, CallbackInfo ci) {
        this.bbs_crowd$renderCrowdRadius(context);
        this.bbs_crowd$renderArea(context);
        this.bbs_crowd$renderCrowdWalkPoles(context);
        this.bbs_crowd$renderCrowdWalkPath(context);
    }

    @Inject(method = "setCursor", at = @At("RETURN"))
    private void bbs_crowd$onSetCursor(int value, CallbackInfo ci) {
        this.bbs_crowd$syncCrowdPaint(value);
    }

    @Unique
    private void bbs_crowd$renderCrowdRadius(WorldRenderContext context) {
        UIFilmPanel self = (UIFilmPanel) (Object) this;
        Film film = (Film) (Object) self.getData();
        if (film == null) {
            return;
        }

        Crowd crowd = CrowdSelection.get();
        double outer;
        double inner = 0D;
        Replay replay;

        if (crowd != null) {
            CrowdFormation formation = crowd.getFormation();
            outer = CrowdUtils.formationRadius(formation, crowd.count.get(), crowd.spacing.get(), crowd.holeRadius.get());
            inner = CrowdUtils.formationHole(formation, crowd.holeRadius.get());
            replay = CrowdUtils.getReplay(film, crowd.anchor.get());
        } else if (this.replayEditor != null && self.actionEditor != null && self.actionEditor.isVisible()
            && self.actionEditor.getClip() instanceof CrowdBehaviorActionClip clip) {
            outer = clip.wanderRadius.get();
            replay = this.replayEditor.getReplay();
        } else {
            return;
        }

        if (replay == null) {
            return;
        }

        Vec3d center = CrowdUtils.replayPosition(replay, this.getCursor());
        Vec3d camera = context.camera().getPos();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        this.bbs_crowd$renderCrowdRing(context, center.subtract(camera), outer, 0.1F, 0.8F, 1F);

        if (inner > 0.05D) {
            this.bbs_crowd$renderCrowdRing(context, center.subtract(camera), inner, 1F, 0.65F, 0.1F);
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Unique
    private void bbs_crowd$renderArea(WorldRenderContext context) {
        Crowd crowd = CrowdSelection.get();
        if (crowd == null || crowd.getFormation() != CrowdFormation.PAINT) {
            return;
        }

        if (!crowd.showOutline.get() && !AreaBrush.isArmed()) {
            return;
        }

        Long2IntOpenHashMap cells = crowd.getCells();
        Vec3d camera = context.camera().getPos();
        MatrixStack stack = context.matrixStack();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        float half = 0.08F;

        for (Long2IntMap.Entry entry : cells.long2IntEntrySet()) {
            long key = entry.getLongKey();
            int x = ValueAreaCells.keyX(key);
            int z = ValueAreaCells.keyZ(key);
            float y = (float) (entry.getIntValue() + 1.02D - camera.y);
            float x1 = (float) (x - camera.x);
            float z1 = (float) (z - camera.z);
            float x2 = x1 + 1F;
            float z2 = z1 + 1F;

            if (!cells.containsKey(ValueAreaCells.key(x - 1, z))) {
                Draw.fillQuad(builder, stack, x1 - half, y, z1 - half, x1 + half, y, z1 - half, x1 + half, y, z2 + half, x1 - half, y, z2 + half, 1F, 0.7F, 0.15F, 1F);
            }
            if (!cells.containsKey(ValueAreaCells.key(x + 1, z))) {
                Draw.fillQuad(builder, stack, x2 - half, y, z1 - half, x2 + half, y, z1 - half, x2 + half, y, z2 + half, x2 - half, y, z2 + half, 1F, 0.7F, 0.15F, 1F);
            }
            if (!cells.containsKey(ValueAreaCells.key(x, z - 1))) {
                Draw.fillQuad(builder, stack, x1 - half, y, z1 - half, x2 + half, y, z1 - half, x2 + half, y, z1 + half, x1 - half, y, z1 + half, 1F, 0.7F, 0.15F, 1F);
            }
            if (!cells.containsKey(ValueAreaCells.key(x, z + 1))) {
                Draw.fillQuad(builder, stack, x1 - half, y, z2 - half, x2 + half, y, z2 - half, x2 + half, y, z2 + half, x1 - half, y, z2 + half, 1F, 0.7F, 0.15F, 1F);
            }
        }

        BlockPos hovered = AreaBrush.getHovered();
        if (hovered != null) {
            int radius = AreaBrush.getHoveredRadius();
            float y = (float) (hovered.getY() + 1.05D - camera.y);
            float cx = (float) (hovered.getX() + 0.5D - camera.x);
            float cz = (float) (hovered.getZ() + 0.5D - camera.z);
            int segments = (int) MathUtils.clamp(radius * 8, 32, 160);
            float r = AreaBrush.isErasing() ? 1F : 0.3F;
            float g = AreaBrush.isErasing() ? 0.3F : 1F;

            for (int i = 0; i < segments; i++) {
                double a1 = i / (double) segments * Math.PI * 2D;
                double a2 = (i + 1) / (double) segments * Math.PI * 2D;

                Draw.fillBoxTo(builder, stack,
                    (float) (cx + Math.cos(a1) * radius), y, (float) (cz + Math.sin(a1) * radius),
                    (float) (cx + Math.cos(a2) * radius), y, (float) (cz + Math.sin(a2) * radius),
                    0.06F, r, g, 0.35F, 0.9F);
            }
        }

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Unique
    private void bbs_crowd$renderCrowdWalkPoles(WorldRenderContext context) {
        Replay replay = this.replayEditor == null ? null : this.replayEditor.getReplay();
        if (replay == null || !(replay.form.get() instanceof CrowdForm)) {
            return;
        }

        ReplayCrowdChannels channels = ReplayCrowdChannels.of(replay.keyframes);
        if (channels == null || channels.crowdWalk.isEmpty()) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        MatrixStack stack = context.matrixStack();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        for (Keyframe<CrowdWalk> keyframe : channels.crowdWalk.getKeyframes()) {
            CrowdWalk walk = keyframe.getValue();
            if (walk == null || !walk.showPoint) {
                continue;
            }

            double x = walk.x - camera.x;
            double y = walk.y - camera.y;
            double z = walk.z - camera.z;

            Draw.fillBoxTo(builder, stack,
                (float) x, (float) y, (float) z,
                (float) x, (float) (y + 3.0F), (float) z,
                0.05F, 1F, 1F, 1F, 0.8F);
        }

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void bbs_crowd$renderCrowdWalkPath(WorldRenderContext context) {
        Replay replay = this.replayEditor == null ? null : this.replayEditor.getReplay();
        if (replay == null || !(replay.form.get() instanceof CrowdForm)) {
            return;
        }

        ReplayCrowdChannels channels = ReplayCrowdChannels.of(replay.keyframes);
        if (channels == null || channels.crowdWalk.isEmpty()) {
            return;
        }

        List<Keyframe<CrowdWalk>> keyframes = channels.crowdWalk.getKeyframes();
        if (keyframes.isEmpty()) {
            return;
        }

        boolean showAnyPath = false;
        for (Keyframe<CrowdWalk> kf : keyframes) {
            if (kf.getValue() != null && kf.getValue().showPath) {
                showAnyPath = true;
                break;
            }
        }

        boolean isEditingWalk = this.replayEditor.keyframeEditor != null && this.replayEditor.keyframeEditor.editor instanceof UICrowdWalkKeyframeFactory;
        if (!showAnyPath && !isEditingWalk) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        MatrixStack stack = context.matrixStack();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        float firstTick = keyframes.get(0).getTick();
        float lastTick = keyframes.get(keyframes.size() - 1).getTick();
        float currentReplayTick = replay.getTick(this.getCursor());

        if (lastTick > firstTick) {
            float step = 0.5F;
            Vec3d prev = null;

            for (float t = firstTick; t <= lastTick + 0.001F; t += step) {
                CrowdWalk point = channels.crowdWalk.interpolate(t);
                if (point == null) {
                    continue;
                }

                Vec3d cur = new Vec3d(point.x - camera.x, point.y - camera.y, point.z - camera.z);
                if (prev != null) {
                    float r = t <= currentReplayTick ? 0.2F : 0.95F;
                    float g = t <= currentReplayTick ? 0.85F : 0.85F;
                    float b = t <= currentReplayTick ? 1.0F : 0.25F;
                    float a = 0.85F;

                    Draw.fillBoxTo(builder, stack,
                        (float) prev.x, (float) prev.y + 0.05F, (float) prev.z,
                        (float) cur.x, (float) cur.y + 0.05F, (float) cur.z,
                        0.06F, r, g, b, a);
                }

                prev = cur;
            }
        }

        for (Keyframe<CrowdWalk> kf : keyframes) {
            CrowdWalk walk = kf.getValue();
            if (walk == null) {
                continue;
            }

            double wx = walk.x - camera.x;
            double wy = walk.y - camera.y;
            double wz = walk.z - camera.z;

            Draw.fillBoxTo(builder, stack,
                (float) wx, (float) wy, (float) wz,
                (float) wx, (float) wy + 0.2F, (float) wz,
                0.12F, 1.0F, 0.65F, 0.15F, 0.9F);
        }

        if (currentReplayTick >= firstTick && currentReplayTick <= lastTick) {
            CrowdWalk headPoint = channels.crowdWalk.interpolate(currentReplayTick);
            if (headPoint != null) {
                double hx = headPoint.x - camera.x;
                double hy = headPoint.y - camera.y;
                double hz = headPoint.z - camera.z;

                Draw.fillBoxTo(builder, stack,
                    (float) hx, (float) hy, (float) hz,
                    (float) hx, (float) hy + 0.35F, (float) hz,
                    0.2F, 0.15F, 1.0F, 0.4F, 1.0F);
            }
        }

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void bbs_crowd$renderCrowdRing(WorldRenderContext context, Vec3d center, double radius, float r, float g, float b) {
        if (radius <= 0.05D) {
            return;
        }

        int segments = (int) MathUtils.clamp(radius * 4D, 64D, 512D);
        float thickness = (float) Math.max(0.08D, radius * 0.004D);
        MatrixStack stack = context.matrixStack();
        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            double a1 = i / (double) segments * Math.PI * 2D;
            double a2 = (i + 1) / (double) segments * Math.PI * 2D;

            Draw.fillBoxTo(builder, stack,
                (float) (center.x + Math.cos(a1) * radius), (float) center.y, (float) (center.z + Math.sin(a1) * radius),
                (float) (center.x + Math.cos(a2) * radius), (float) center.y, (float) (center.z + Math.sin(a2) * radius),
                thickness, r, g, b, 0.85F);
        }

        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    @Unique
    private void bbs_crowd$syncCrowdPaint(int ticks) {
        if (this.replayEditor == null || AreaBrush.isPainting()) {
            return;
        }

        Replay replay = this.replayEditor.getReplay();
        if (replay == null || !(replay.form.get() instanceof CrowdForm cf)) {
            return;
        }

        ReplayCrowdChannels channels = ReplayCrowdChannels.of(replay.keyframes);
        if (channels == null || channels.crowdPaint.isEmpty()) {
            return;
        }

        UIFilmPanel self = (UIFilmPanel) (Object) this;
        Film film = (Film) (Object) self.getData();
        if (film == null) {
            return;
        }

        Crowds crowds = FilmCrowdAccess.getCrowds(film);
        Crowd crowd = crowds == null ? null : crowds.byTag(cf.crowd.get());
        if (crowd == null || crowd.getFormation() != CrowdFormation.PAINT) {
            return;
        }

        KeyframeSegment<CrowdPaint> segment = channels.crowdPaint.find(replay.getTick(ticks));
        if (segment != null) {
            Keyframe<CrowdPaint> active = ticks >= segment.b.getTick() ? segment.b : segment.a;
            if (active != null && active.getValue() != null) {
                crowd.setCells(active.getValue().getCells());
            }
        }
    }
}
