package io.github.liyze09.arms.network

import io.netty.buffer.ByteBuf
import net.minecraftarm.common.Identifier
import java.util.concurrent.ConcurrentHashMap

object PluginChannel {
    private val handlers: MutableMap<Identifier, PluginChannelHandler> =
        ConcurrentHashMap()
    fun broadcast(identifier: Identifier, data: ByteBuf) {
        handlers[identifier]?.handle(data)
        data.release()
    }

    fun send(identifier: Identifier, data: ByteBuf, connection: Connection) {
        connection.sendPacket(
            io.github.liyze09.arms.network.packet.clientbound.PluginMessageBody(identifier, data),
            io.github.liyze09.arms.network.packet.clientbound.PluginMessage()
        )
        data.release()
    }

    fun register(identifier: Identifier, handler: PluginChannelHandler) {
        handlers[identifier] = handler
    }
}

@FunctionalInterface
fun interface PluginChannelHandler {
    fun handle(data: ByteBuf)
}