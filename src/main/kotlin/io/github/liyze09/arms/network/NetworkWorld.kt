package io.github.liyze09.arms.network

import io.github.liyze09.arms.GlobalConfiguration
import io.github.liyze09.arms.network.packet.clientbound.*
import net.minecraftarm.common.Identifier
import net.minecraftarm.common.TeleportBody
import net.minecraftarm.entity.Player
import net.minecraftarm.world.Chunk
import net.minecraftarm.world.Dimension
import net.minecraftarm.world.World

object NetworkWorld {
    fun newPlayer(connection: Connection): Player {
        // TODO: Get player data from save
        val ret = Player(connection)
        connection.boundedPlayerEntity = ret
        val currentDimension = World.getDimension(Identifier("minecraft", "overworld"))
        val position = World.getWorldSpawnPoint().toEntityPosition()
        ret.loadToWorld(currentDimension, position)
        connection.sendPacket(ret, PlayLogin)
            .sendPacket(
                GlobalConfiguration.instance.difficulty,
                ChangeDifficulty
            )
            .sendPacket(ret.heldItem, SetHeldItem)
            .sendPacket(
                Pair(ret.entityId, 24/*TODO op level*/),
                EntityEvent
            )
            .sendPacket(
                TeleportBody(position),
                SynchronizePlayerPosition
            ).sendPacket(
                Pair(GameEvent.GameEventBody.START_WAITING_FOR_LEVEL_CHUNKS, 0F),
                GameEvent
            ).sendPacket(
                Pair(position.x.toInt() and 15, position.z.toInt() and 15),
                SetCenterChunk
            )
        loadChunksAround(
            position.x.toInt(),
            position.z.toInt(),
            GlobalConfiguration.instance.simulationDistance,
            currentDimension
        )
            .forEach {
                connection.sendPacket(it, ChunkDataAndUpdateLight)
            }
        // TODO Recipe Book / Recipes
        // TODO 24/08/09
        return ret
    }

    fun loadChunksAround(playerX: Int, playerZ: Int, radius: Int, dimension: Dimension): List<Chunk> {
        val startX0 = playerX / 16 - radius
        val endX0 = playerX / 16 + radius
        val startZ0 = playerZ / 16 - radius
        val endZ0 = playerZ / 16 + radius
        for (x in startX0..endX0) {
            for (z in startZ0..endZ0) {
                dimension.getChunk(x, z)
            }
        }

        val startX = playerX / 16 - radius - 2
        val endX = playerX / 16 + radius + 2
        val startZ = playerZ / 16 - radius - 2
        val endZ = playerZ / 16 + radius + 2
        val ret = mutableListOf<Chunk>()

        for (x in startX..endX) {
            for (z in startZ..endZ) {
                ret.add(dimension.preloadChunk(x, z))
            }
        }
        return ret
    }

    fun setPlayerPosition(connection: Connection, x: Double, y: Double, z: Double) {
        val pos = connection.boundedPlayerEntity?.position ?: return
        pos.x = x
        pos.y = y
        pos.z = z
    }

    fun setPlayerRotation(connection: Connection, yaw: Float, pitch: Float) {
        val pos = connection.boundedPlayerEntity?.position ?: return
        pos.yaw = yaw
        pos.pitch = pitch
    }

    fun setPlayerOnGround(connection: Connection, onGround: Boolean) {
        val pos = connection.boundedPlayerEntity?.position ?: return
        pos.onGround = onGround
    }
}