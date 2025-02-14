package net.perfectdreams.ddgamehard.logic

object Sprites {
    val registeredSprites = mutableListOf<Sprite>()

    val MissingTexture = createSprite("missing_texture.png")
    val BlockYellow = createSprite("block_yellow.png")
    val BlockRed = createSprite("block_red.png")
    val BlockPurple = createSprite("block_purple.png")
    val BlockGreen = createSprite("block_green.png")
    val BlockBlue = createSprite("block_blue.png")
    val MinecraftGrass = createSprite("minecraft_grass.png")
    val MinecraftDirt = createSprite("minecraft_dirt.png")
    val MinecraftBrick = createSprite("minecraft_brick.png")
    val Windows98Start = createSprite("windows98_start.png")
    val Windows98Taskbar = createSprite("windows98_taskbar.png")
    val SonicteamPowerBlog = createSprite("sonicteampower_blog.png")
    val SonicteamPowerBlogVertical = createSprite("sonicteampower_blog_vertical.png")

    val Finish = createSprite("finish.png")
    val Fire = createSprite("fire.png")

    val Coin1 = createSprite("coin1.png")
    val Coin2 = createSprite("coin2.png")
    val Coin3 = createSprite("coin3.png")
    val Coin4 = createSprite("coin4.png")

    val MayWalking1 = createSprite("may_walking1.png")
    val MayWalking2 = createSprite("may_walking2.png")
    val MayWalking3 = createSprite("may_walking3.png")

    val Goomba1 = createSprite("goomba1.png")
    val Goomba2 = createSprite("goomba2.png")
    val Goomba3 = createSprite("goomba3.png")
    val GoombaSquished = createSprite("goomba_squished.png")

    val YellowSpring = createSprite("spring.png")

    val Goomba = listOf(
        Goomba1,
        Goomba2,
        Goomba3
    )

    val MayWalking = listOf(
        MayWalking1,
        MayWalking2,
        MayWalking3
    )

    val Coins = listOf(
        Coin1,
        Coin2,
        Coin3,
        Coin4
    )

    fun createSprite(spriteName: String): Sprite {
        val spriteRef = Sprite(spriteName)
        this.registeredSprites += spriteRef
        return spriteRef
    }
}