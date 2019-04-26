package jp.techacademy.shun.kozuma.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Enemy(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): GameObject(texture, srcX, srcY, srcWidth, srcHeight) {
/*    companion object {
        // 横幅、高さ
        val ENEMY_WIDTH = 0.8f
        val ENEMY_HEIGHT = 0.8f

        // 状態
        val ENEMY_EXIST = 0
        val ENEMY_NONE = 1
    }

    var mState: Int = 0

    init {
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT)
        mState = ENEMY_EXIST
    }

    fun get() {
        mState = ENEMY_NONE
        setAlpha(0f)
    }
    */

    companion object {
        // 横幅、高さ
        val ENEMY_WIDTH = 0.8f
        val ENEMY_HEIGHT = 0.8f

        val ENEMY_TYPE_MOVING = 0
        val ENEMY_NONE = 1

        // 速度
        val ENEMY_VELOCITY = 2.0f
    }

    var mState: Int = 0

    init {
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT)
        velocity.x = Enemy.ENEMY_VELOCITY
    }

    // 座標を更新する
    fun update(deltaTime: Float) {
        x += velocity.x * deltaTime

        if (x < ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = ENEMY_WIDTH / 2
        }
        if (x > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2
        }
    }

    fun get() {
        mState = ENEMY_NONE
        setAlpha(0f)
    }

    fun notget() {
        mState = ENEMY_TYPE_MOVING
        //setAlpha(0f)
    }

}