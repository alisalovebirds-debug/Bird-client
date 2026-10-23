package com.birdclient.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BirdClient implements ClientModInitializer {
    private static boolean rightShiftWasDown;
    private static boolean espEnabled;
    private static boolean hudEnabled = true;
    private static boolean coordinatesEnabled = true;
    private static boolean triggerAssistEnabled;
    private static boolean sprintAssistEnabled;
    private static final Map<UUID, Boolean> originalGlow = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;

            boolean rightShiftDown = GLFW.glfwGetKey(
                client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT
            ) == GLFW.GLFW_PRESS;

            if (rightShiftDown && !rightShiftWasDown) {
                if (client.currentScreen instanceof BirdScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new BirdScreen());
                }
            }
            rightShiftWasDown = rightShiftDown;

            updateEsp(client);
            updateTriggerAssist(client);
            updateSprintAssist(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            int y = 7;
            if (hudEnabled) {
                drawContext.fill(7, 6, 112, 24, 0xB9161028);
                drawContext.fill(7, 6, 9, 24, 0xFFC58BFF);
                drawContext.drawText(client.textRenderer, "Bird Client", 14, 11, 0xFFE5CCFF, true);
                y = 29;
            }

            if (coordinatesEnabled) {
                String xyz = String.format("XYZ: %.1f / %.1f / %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ());
                drawContext.fill(7, y, 165, y + 14, 0xB9161028);
                drawContext.drawText(client.textRenderer, xyz, 12, y + 3, 0xFFF5EEFF, false);
            }
        });
    }

    private static void updateEsp(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            originalGlow.clear();
            return;
        }

        if (espEnabled) {
            for (Entity entity : client.world.getEntities()) {
                if (entity == client.player || !entity.isAlive()) continue;
                originalGlow.putIfAbsent(entity.getUuid(), entity.isGlowing());
                entity.setGlowing(true);
            }
        } else {
            for (Entity entity : client.world.getEntities()) {
                Boolean previous = originalGlow.remove(entity.getUuid());
                if (previous != null) entity.setGlowing(previous);
            }
            originalGlow.clear();
        }
    }

    private static void updateTriggerAssist(MinecraftClient client) {
        // Deliberately restricted to single-player worlds.
        if (!triggerAssistEnabled || client.player == null || client.world == null
            || client.interactionManager == null || !client.isInSingleplayer()) return;

        Entity target = client.targetedEntity;
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;
        if (client.player.getAttackCooldownProgress(0.5F) < 1.0F) return;

        client.interactionManager.attackEntity(client.player, living);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private static void updateSprintAssist(MinecraftClient client) {
        if (!sprintAssistEnabled || client.player == null || client.currentScreen != null) return;
        if (client.player.forwardSpeed > 0.0F && !client.player.isSneaking()) {
            client.player.setSprinting(true);
        }
    }

    private static final class BirdScreen extends Screen {
        private static final int PURPLE = 0xFFC58BFF;
        private static final int BACKGROUND = 0xF5100D1E;
        private static final int PANEL = 0xFF211832;
        private static final int HOVER = 0xFF39264F;
        private static final int WHITE = 0xFFF5EEFF;
        private static final int MUTED = 0xFFB9A8D1;
        private final int panelWidth = 365;
        private final int panelHeight = 285;
        private int panelX, panelY;

        BirdScreen() {
            super(Text.literal("Bird Client"));
        }

        @Override
        protected void init() {
            panelX = (width - panelWidth) / 2;
            panelY = (height - panelHeight) / 2;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0x99060012);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, BACKGROUND);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 3, PURPLE);
            context.fill(panelX, panelY + 3, panelX + panelWidth, panelY + 48, PANEL);

            context.drawText(textRenderer, "Bird Client", panelX + 16, panelY + 12, PURPLE, true);
            context.drawText(textRenderer, "PURPLE EDITION  •  1.21.11", panelX + 16, panelY + 29, MUTED, false);
            context.drawText(textRenderer, "RIGHT SHIFT: CLOSE", panelX + 228, panelY + 17, MUTED, false);

            context.drawText(textRenderer, "VISUALS", panelX + 16, panelY + 57, MUTED, true);
            drawModule(context, mouseX, mouseY, 0, "Entity ESP", "Outline loaded entities", espEnabled);
            drawModule(context, mouseX, mouseY, 1, "HUD", "Bird Client status label", hudEnabled);
            drawModule(context, mouseX, mouseY, 2, "Coordinates", "Player XYZ display", coordinatesEnabled);

            context.drawText(textRenderer, "UTILITY", panelX + 16, panelY + 160, MUTED, true);
            drawModule(context, mouseX, mouseY, 3, "Trigger Assist", "Single-player only", triggerAssistEnabled);
            drawModule(context, mouseX, mouseY, 4, "Sprint Assist", "Auto sprint when moving forward", sprintAssistEnabled);

            context.fill(panelX + 14, panelY + panelHeight - 31,
                panelX + panelWidth - 14, panelY + panelHeight - 30, 0xFF4A3565);
            context.drawText(textRenderer, "Click a row to toggle a module", panelX + 16,
                panelY + panelHeight - 20, MUTED, false);
            super.render(context, mouseX, mouseY, delta);
        }

        private void drawModule(DrawContext context, int mouseX, int mouseY,
                                int index, String name, String description, boolean enabled) {
            int rowY = index < 3 ? panelY + 72 + index * 27 : panelY + 175 + (index - 3) * 27;
            boolean hovered = mouseX >= panelX + 12 && mouseX <= panelX + panelWidth - 12
                && mouseY >= rowY && mouseY < rowY + 24;

            context.fill(panelX + 12, rowY, panelX + panelWidth - 12, rowY + 24,
                hovered ? HOVER : PANEL);
            context.drawText(textRenderer, name, panelX + 20, rowY + 4,
                enabled ? PURPLE : WHITE, true);
            context.drawText(textRenderer, description, panelX + 20, rowY + 14, MUTED, false);
            context.fill(panelX + panelWidth - 53, rowY + 6,
                panelX + panelWidth - 20, rowY + 18, enabled ? 0xFF7040A8 : 0xFF51475D);
            context.drawText(textRenderer, enabled ? "ON" : "OFF",
                panelX + panelWidth - 46, rowY + 8, WHITE, true);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                for (int i = 0; i < 5; i++) {
                    int rowY = i < 3 ? panelY + 72 + i * 27 : panelY + 175 + (i - 3) * 27;
                    if (mouseX >= panelX + 12 && mouseX <= panelX + panelWidth - 12
                        && mouseY >= rowY && mouseY < rowY + 24) {
                        switch (i) {
                            case 0 -> espEnabled = !espEnabled;
                            case 1 -> hudEnabled = !hudEnabled;
                            case 2 -> coordinatesEnabled = !coordinatesEnabled;
                            case 3 -> triggerAssistEnabled = !triggerAssistEnabled;
                            case 4 -> sprintAssistEnabled = !sprintAssistEnabled;
                        }
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
