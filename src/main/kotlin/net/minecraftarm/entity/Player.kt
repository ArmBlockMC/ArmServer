package net.minecraftarm.entity

import io.github.liyze09.arms.network.Connection
import net.minecraftarm.common.GameMode
import net.minecraftarm.common.UUID

class Player(val connection: Connection) : LivingEntity() {
    val previousGamemode: GameMode? = null
    var gamemode = GameMode.SURVIVAL
    var portalCooldownTick = 0
    var heldItem = 0
    override fun whenDead() {

    }

    override val uuid: UUID
        get() = connection.getUUID()
}
