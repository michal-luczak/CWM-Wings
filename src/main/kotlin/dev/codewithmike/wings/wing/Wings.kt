package dev.codewithmike.wings.wing

import dev.codewithmike.wings.CWMWings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.EulerAngle
import kotlin.math.sin

class Wings(
    private val wingsItemModel: String,
    val wingsDefinitionId: String,
    private val owner: Player
) {

    private val armorStand: ArmorStand = spawnWings()
    private lateinit var animationTask: BukkitTask

    private fun spawnWings(): ArmorStand {
        val leftWing = ItemStack(Material.DIAMOND).apply {
            itemMeta = itemMeta.apply {
                itemModel = NamespacedKey.minecraft(wingsItemModel)
            }
        }
        val rightWing = ItemStack(Material.DIAMOND).apply {
            itemMeta = itemMeta.apply {
                itemModel = NamespacedKey.minecraft(wingsItemModel)
            }
        }
        val armorStand = owner.world.spawnEntity(
            owner.location,
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

        animationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(CWMWings.instance, Runnable {
            val angle = Math.toRadians(25.0) * (sin(tick) + 1)
            armorStand.setRotation(owner.yaw, 0f)
            armorStand.leftArmPose = EulerAngle(0.0, angle, 0.0)
            armorStand.rightArmPose = EulerAngle(0.0, -angle, 0.0)
            tick += 0.1
        }, 0L, 1L)

        owner.addPassenger(armorStand)
        owner.playSound(owner, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)
        return armorStand
    }

    fun despawn() {
        animationTask.cancel()
        owner.playSound(owner, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)
        if (armorStand.isValid) {
            armorStand.remove()
        }
    }
}