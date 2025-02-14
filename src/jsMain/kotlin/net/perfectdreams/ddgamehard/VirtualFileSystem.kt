package net.perfectdreams.ddgamehard

import web.html.Image

class VirtualFileSystem(
    val images: Map<String, Image>,
    val files: Map<String, ByteArray>
)