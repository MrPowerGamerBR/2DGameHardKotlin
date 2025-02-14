package net.perfectdreams.ddgamehard.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class Instance(
    val _objName: String,
    val _x: Int,
    val _y: Int,
    val _name: String,
    val _locked: String,
    val _code: String,
    val _scaleX: Float,
    val _scaleY: Float,
    val _colour: String,
    val _rotation: String
)

@Serializable
data class InstancesWrapper(
    val instance: List<Instance>
)

@Serializable
data class Root(
    val instances: InstancesWrapper
)
