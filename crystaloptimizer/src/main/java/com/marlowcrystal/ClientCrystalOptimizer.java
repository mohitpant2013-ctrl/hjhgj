package com.marlowcrystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

/**
 * Client-side optimistic crystal removal for older Forge clients.
 *
 * The server still receives the normal attack packet and remains authoritative;
 * this only removes the local crystal entity so it does not block the next
 * placement while the server's destroy packet is in flight.
 */
@Mod.EventBusSubscriber(
        modid = MarlowCrystalOptimizer.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientCrystalOptimizer {
    private ClientCrystalOptimizer() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        Entity target = event.getTarget();

        if (event.isCanceled()
                || !player.level.isClientSide
                || player.isSpectator()
                || !(target instanceof EnderCrystalEntity)) {
            return;
        }

        EnderCrystalEntity crystal = (EnderCrystalEntity) target;
        World world = player.level;
        WorldBorder border = world.getWorldBorder();
        if (!border.isWithinBounds(crystal.getBoundingBox()) || !canDamageCrystal(player)) {
            return;
        }

        // Do not cancel or replace the attack. The original packet is still sent
        // and the server decides whether the crystal is actually destroyed.
        crystal.remove();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            minecraft.gameRenderer.pick(1.0F);
        }
    }

    private static boolean canDamageCrystal(PlayerEntity player) {
        double damage = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        Collection<AttributeModifier> modifiers = player.getMainHandItem()
                .getAttributeModifiers(EquipmentSlotType.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE);
        for (AttributeModifier modifier : modifiers) {
            damage += modifier.getAmount();
        }

        EffectInstance strength = player.getEffect(Effects.DAMAGE_BOOST);
        if (strength != null) {
            damage += 3.0D * (strength.getAmplifier() + 1);
        }

        EffectInstance weakness = player.getEffect(Effects.WEAKNESS);
        if (weakness != null) {
            damage -= 4.0D * (weakness.getAmplifier() + 1);
        }

        return Math.max(0.0D, damage) > 0.0D;
    }
}
