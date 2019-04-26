package jp.techacademy.shun.kozuma.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Apple (texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): Sprite(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val APPLE_WIDTH = 0.8f
        val APPLE_HEIGHT = 0.8f

        // 状態
        val APPLE_EXIST = 0
        val APPLE_NONE = 1
    }

    var mState: Int = 0

    init {
        setSize(APPLE_WIDTH, APPLE_HEIGHT)
        mState = APPLE_EXIST
    }

    fun get() {
        mState = APPLE_NONE
        setAlpha(0f)
    }

}