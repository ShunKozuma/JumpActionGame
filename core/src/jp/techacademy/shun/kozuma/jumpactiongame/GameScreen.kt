package jp.techacademy.shun.kozuma.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import java.util.*
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.*
import kotlin.collections.ArrayList


class GameScreen(private val mGame: JumpActionGame): ScreenAdapter() {
    companion object {
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f
        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 20    // 20画面分登れば終了
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f

        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2
        val GAME_STATE_MUTEKI = 10


        // 重力
        val GRAVITY = -12

        val sound = Gdx.audio.newSound(Gdx.files.internal("bomb.mp3"))

    }

    private val mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewPort: FitViewport

    private var mRandom: Random
    private var mSteps: ArrayList<Step>
    private var mStars: ArrayList<Star>
    private lateinit var mUfo: Ufo
    private lateinit var mPlayer: Player

    private var mEnemys: ArrayList<Enemy>
    private var mApples: ArrayList<Apple>
    private var mMutekis: ArrayList<Muteki>

    private var mGameState: Int
    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3
    private var mFont: BitmapFont
    private var mScore: Int
    private var mHighScore: Int
    private var mPrefs: Preferences
    private var hp: Int = 3

    private lateinit var enemy: Enemy

    private lateinit var muteki: Muteki

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 10

    lateinit var barrierTexture: Texture
    lateinit var playerTexture: Texture



    init {
        //背景の準備
        val bgTexture = Texture("back.png")
        //TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture,0,0,540,810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f,0f)

        //カメラ、ViewPortを生成、設定する
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT,mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // プロパティの初期化
        mRandom = Random()
        mSteps = ArrayList<Step>()
        mStars = ArrayList<Star>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()

        mEnemys = ArrayList<Enemy>()
        mApples = ArrayList<Apple>()
        mMutekis = ArrayList<Muteki>()

        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
        mFont.data.setScale(0.8f)
        mScore = 0
        mHighScore = 0

        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.shun.kozuma.jumpactiongame")
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0)

        createStage()
    }

    override fun render(delta: Float) {
        // それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f,0f,0f,1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.y > mCamera.position.y) {
            mCamera.position.y = mPlayer.y
        }


        //カメラの座標をアップデート（計算）し、スプライドの表示に反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin()

        //背景
        //原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // Step
        for (i in 0 until mSteps.size) {
            mSteps[i].draw(mGame.batch)
        }

        // Star
        for (i in 0 until mStars.size) {
            mStars[i].draw(mGame.batch)
        }

        // UFO
        mUfo.draw(mGame.batch)

        //Player
        mPlayer.draw(mGame.batch)

        // Enemy
        for (i in 0 until mEnemys.size) {
            mEnemys[i].draw(mGame.batch)
        }

        // Apple
        for (i in 0 until 7) {
            mApples[i].draw(mGame.batch)
        }

        // Muteki
        for (i in 0 until 5) {
            mMutekis[i].draw(mGame.batch)
        }


        mGame.batch.end()

        // スコア表示
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined
        mGame.batch.begin()
        mFont.draw(mGame.batch, "HighScore: $mHighScore", 16f, GUI_HEIGHT - 15)
        mFont.draw(mGame.batch, "Score: $mScore", 16f, GUI_HEIGHT - 35)

        mFont.draw(mGame.batch, "HP: $hp", GUI_WIDTH - 70, GUI_HEIGHT - 25)
        mFont.draw(mGame.batch, "Super\nTime: $mTimerSec", GUI_WIDTH - 110, GUI_HEIGHT - 400)

        //GameOver表示
        if(mGameState == GAME_STATE_GAMEOVER){
            mFont.draw(mGame.batch,"   Game Over\n\nTouch Screen", GUI_WIDTH / 3 - 22f, GUI_HEIGHT / 2 + 35)
        }

        mGame.batch.end()
    }

    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height)
        mGuiViewPort.update(width, height)
    }

    // ステージを作成する
    private fun createStage() {

        // テクスチャの準備
        val stepTexture = Texture("step.png")
        val starTexture = Texture("star.png")
        playerTexture = Texture("pose.png")
        val ufoTexture = Texture("ufo.png")
        val enemyTexture = Texture("bakudan.png")
        val appleTexture = Texture("fruit_ringo.png")
        val mutekiTexture = Texture("rokubousei.png")
        barrierTexture = Texture("hero.png")

        // StepとStarをゴールの高さまで配置していく
        var y = 0f
        var ye = 3f

        val maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY)
        while (y < WORLD_HEIGHT - 5) {
            val type = if (mRandom.nextFloat() > 0.8f) Step.STEP_TYPE_MOVING else Step.STEP_TYPE_STATIC
            val x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH)

            val step = Step(type, stepTexture, 0, 0, 144, 36)
            step.setPosition(x, y)
            mSteps.add(step)

            //val types = if(mRandom.nextFloat() > 0.8f) Enemy.ENEMY_TYPE_MOVING else Enemy.ENEMY_TYPE_STATIC
            val xe = mRandom.nextFloat() * (WORLD_WIDTH - Enemy.ENEMY_WIDTH)

            val enemy = Enemy(enemyTexture, 0, 0, 120, 150)
            enemy.setPosition(xe, ye * 2)
            mEnemys.add(enemy)

            if (mRandom.nextFloat() > 0.6f) {
                val star = Star(starTexture, 0, 0, 72, 72)
                star.setPosition(step.x + mRandom.nextFloat(), step.y + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mStars.add(star)
            }

            if (mRandom.nextFloat() > 0.6f) {
                val apple = Apple(appleTexture, 0, 0, 155, 150)
                apple.setPosition(step.x + mRandom.nextFloat() * 3, step.y * 4 + Apple.APPLE_HEIGHT + mRandom.nextFloat() )
                mApples.add(apple)

                muteki = Muteki(mutekiTexture, 0, 0, 155, 150)
                muteki.setPosition(step.x + mRandom.nextFloat() * 3, step.y * 5 + Apple.APPLE_HEIGHT + mRandom.nextFloat() )
                mMutekis.add(muteki)
            }


            ye += (maxJumpHeight - 0.5f)
            ye -= mRandom.nextFloat() * (maxJumpHeight / 3)


            y += (maxJumpHeight - 0.5f)
            y -= mRandom.nextFloat() * (maxJumpHeight / 3)
        }

        // Playerを配置
        mPlayer = Player(playerTexture, 0, 0, 300, 380)
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.width / 2, Step.STEP_HEIGHT)


        // ゴールのUFOを配置
        mUfo = Ufo(ufoTexture, 0, 0, 120, 74)
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y)


    }

        // それぞれのオブジェクトの状態をアップデートする
    private fun update(delta: Float) {
        when (mGameState) {
            GAME_STATE_READY ->
                updateReady()
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver()
            GAME_STATE_MUTEKI ->
                updatePlaying(delta)
        }
    }

    //10秒間判定
    private fun checkMuteki() {
        mTimer = Timer()

        mGameState = GAME_STATE_MUTEKI
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mTimerSec -= 1
                //Gdx.app.log("JampActionGame", mGameState.toString())

                mPlayer.texture = barrierTexture
                if(mTimerSec == 0){
                    mTimer!!.cancel()
                    mGameState = GAME_STATE_PLAYING
                    mTimerSec = 10
                    mPlayer.texture = playerTexture
                }
                if(mGameState == GAME_STATE_GAMEOVER){
                    mTimer!!.cancel()
                }
            }
        }, 0, 1000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
    }

    private fun updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING
        }
    }

    private fun updatePlaying(delta: Float) {
        var accel = 0f
        if (Gdx.input.isTouched) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
            val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)

            //mViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            //val left = Rectangle(0f, 0f, CAMERA_WIDTH / 2, CAMERA_HEIGHT)
            //val right = Rectangle(CAMERA_WIDTH / 2, 0f, CAMERA_WIDTH / 2, CAMERA_HEIGHT)
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f
            }
        }

        // Step
        for (i in 0 until mSteps.size) {
            mSteps[i].update(delta)
        }

        // Player
        if (mPlayer.y <= 0.5f) {
            mPlayer.hitStep()
        }

        mPlayer.update(delta, accel)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // Enemy
        for (i in 0 until mEnemys.size) {
            mEnemys[i].update(delta)
        }

        // 当たり判定を行う
        checkCollision()

        // ゲームオーバーか判断する
        checkGameOver()
    }

    private fun checkCollision() {
        // UFO(ゴールとの当たり判定)
        if (mPlayer.boundingRectangle.overlaps(mUfo.boundingRectangle)) {
            mGameState = GAME_STATE_GAMEOVER
            return
        }

        // Starとの当たり判定
        for (i in 0 until mStars.size) {
            val star = mStars[i]

            if (star.mState == Star.STAR_NONE) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(star.boundingRectangle)) {
                star.get()
                mScore++
                if (mScore > mHighScore) {
                    mHighScore = mScore
                    //ハイスコアをPreferenceに保存する
                    mPrefs.putInteger("HIGHSCORE", mHighScore)
                    mPrefs.flush()
                }
                break
            }
        }

        // Enemyとの当たり判定
        for (i in 0 until mEnemys.size) {
            enemy = mEnemys[i]

            if (enemy.mState == Enemy.ENEMY_NONE || mGameState == GAME_STATE_MUTEKI) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(enemy.boundingRectangle)) {
                sound.play(1.0f)
                enemy.get()
                hp--
                    if(hp == 0){
                        //Gdx.app.log("JampActionGame", "GAMEOVER")
                        mGameState = GAME_STATE_GAMEOVER
                    }
                break
            }

        }

        // Appleとの当たり判定
        for (i in 0 until 7) {

            val apple = mApples[i]

            if (apple.mState == Apple.APPLE_NONE) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(apple.boundingRectangle)) {
                //sound.play(1.0f)
                apple.get()
                hp++
                break
            }

        }

        // Mutekiとの当たり判定
        for (i in 0 until 5) {

            val muteki = mMutekis[i]

            if (muteki.mState == Muteki.MUTEKI_NONE) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(muteki.boundingRectangle)) {
                //sound.play(1.0f)
                muteki.get()
                checkMuteki()
                break
            }

        }


        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) {
            return
        }

        for (i in 0 until mSteps.size) {
            val step = mSteps[i]

            if (step.mState == Step.STEP_STATE_VANISH) {
                continue
            }

            if (mPlayer.y > step.y) {
                if (mPlayer.boundingRectangle.overlaps(step.boundingRectangle)) {
                    mPlayer.hitStep()
                    if (mRandom.nextFloat() > 0.5f) {
                        step.vanish()
                    }
                    break
                }
            }
        }

    }

    private fun updateGameOver() {
        if (Gdx.input.justTouched()) {
            mGame.screen = ResultScreen(mGame, mScore)
        }
    }

    private fun checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.y) {
            hp = 0
            Gdx.app.log("JampActionGame", "GAMEOVER")
            mGameState = GAME_STATE_GAMEOVER
        }
    }

}