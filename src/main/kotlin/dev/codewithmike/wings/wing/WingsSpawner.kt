package dev.codewithmike.wings.wing

import dev.codewithmike.wings.CWMWings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import kotlin.math.sin

class WingsSpawner(
    private val plugin: CWMWings,
) {

    fun spawnWings(player: Player, wingsModel: String): ArmorStand {
        val leftWing = ItemStack(Material.DIAMOND).apply {
            itemMeta = itemMeta.apply {
                itemModel = NamespacedKey.minecraft(wingsModel)
            }
        }
        val rightWing = ItemStack(Material.DIAMOND).apply {
            itemMeta = itemMeta.apply {
                itemModel = NamespacedKey.minecraft(wingsModel)
            }
        }
        val armorStand = player.world.spawnEntity(
            player.location,
            EntityType.ARMOR_STAND
        ) as ArmorStand

        armorStand.isInvulnerable = true
        armorStand.setGravity(false)
        armorStand.isCollidable = false
        armorStand.setBasePlate(false)
        armorStand.isMarker = true
        armorStand.isCustomNameVisible = false
        armorStand.isInvisible = true
        armorStand.isSmall = true

        armorStand.setItem(EquipmentSlot.HAND, leftWing)
        armorStand.setItem(EquipmentSlot.OFF_HAND, rightWing)
        armorStand.setArms(true)

        var tick = 0.0

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val angle = Math.toRadians(25.0) * (sin(tick) + 1)
            armorStand.setRotation(player.yaw, 0f)
            armorStand.leftArmPose = EulerAngle(0.0, angle, 0.0)
            armorStand.rightArmPose = EulerAngle(0.0, -angle, 0.0)
            tick += 0.1
        }, 0L, 1L)

        player.addPassenger(armorStand)
        return armorStand
    }
}
