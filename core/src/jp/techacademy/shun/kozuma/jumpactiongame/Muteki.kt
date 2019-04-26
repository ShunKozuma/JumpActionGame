package jp.techacademy.shun.kozuma.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Muteki(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): Sprite(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val MUTEKI_WIDTH = 1.3f
        val MUTEKI_HEIGHT = 1.3f

        // 状態
        val MUTEKI_EXIST = 0
        val MUTEKI_NONE = 1
    }

    var mState: Int = 0

    init {
        setSize(MUTEKI_WIDTH, MUTEKI_HEIGHT)
        mState = MUTEKI_EXIST
    }

    fun get() {
        mState = MUTEKI_NONE
        setAlpha(0f)
    }
}