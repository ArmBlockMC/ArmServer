package net.minecraftarm.world

import java.util.concurrent.locks.ReentrantReadWriteLock

class Chunk(
    height: Int,
    var minY: Int
) {
    init {
        if (height % 16 != 0) {
            throw IllegalArgumentException("Height must be a multiple of 16")
        }
    }

    val maxY = minY + height - 1
    private val lock = ReentrantReadWriteLock()
    private val childChunks = Array(height / 16) { ChildChunk() }
    internal fun getBlockStateIDByChunkPosition(x: Int, y: Int, z: Int): Int {
        lock.readLock().lock()
        try {
            if (x < 0 || x > 15 || z < 0 || z > 15 || y < minY || y > maxY) {
                throw IllegalArgumentException("Position out of bounds")
            }
            return childChunks[y / 16].getBlockState(x, y % 16, z)
        } finally {
            lock.readLock().unlock()
        }
    }

    internal fun setBlockStateIDByChunkPosition(x: Int, y: Int, z: Int, id: Int) {
        lock.writeLock().lock()
        try {
            if (x < 0 || x > 15 || z < 0 || z > 15 || y < minY || y > maxY) {
                throw IllegalArgumentException("Position out of bounds")
            }
            childChunks[y / 16].setBlockState(x, y % 16, z, id)
        } finally {
            lock.writeLock().unlock()
        }
    }

    internal class ChildChunk {
        private val blocks = IntArray(4096)
        fun getBlockState(x: Int, y: Int, z: Int) = blocks[toYZX(x, y, z)]
        fun setBlockState(x: Int, y: Int, z: Int, state: Int) {
            blocks[toYZX(x, y, z)] = state
        }

        companion object {
            @JvmStatic
            fun toYZX(x: Int, y: Int, z: Int) = y shl 8 or (z shl 4) or x
        }
    }
}

