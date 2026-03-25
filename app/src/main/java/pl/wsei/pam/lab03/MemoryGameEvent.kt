package pl.wsei.pam.lab03

import Tile

data class MemoryGameEvent(
    val tiles: List<Tile>,
    val state: GameStates) {
}
