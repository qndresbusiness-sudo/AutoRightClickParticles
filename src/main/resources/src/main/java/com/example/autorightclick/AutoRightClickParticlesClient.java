package com.example.autorightclick;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoRightClickParticlesClient implements ClientModInitializer {

    private static boolean enabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.autorightclick.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        "category.autorightclick"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            net.minecraft.text.Text.literal(
                                    "Auto Right Click: " + (enabled ? "ON" : "OFF")
                            ), true
                    );
                }
            }

            if (!enabled || client.player == null || client.world == null) return;

            checkParticlesAndClick(client);
        });
    }

    private void checkParticlesAndClick(MinecraftClient client) {
        ParticleManager manager = client.particleManager;

        for (Particle particle : manager.particles.values().stream()
                .flatMap(list -> list.stream()).toList()) {

            if (particle.getPos().distanceTo(client.player.getEyePos()) > 4.0) continue;

            float r = particle.getRed();
            float g = particle.getGreen();
            float b = particle.getBlue();

            if (r > 0.8f && g < 0.4f && b < 0.4f) {
                if (client.crosshairTarget != null &&
                        client.crosshairTarget.getType() != HitResult.Type.MISS) {
                    client.doItemUse();
                    break;
                }
            }
        }
    }
}
