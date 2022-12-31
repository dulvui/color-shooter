package com.salvai.centrum.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.salvai.centrum.CentrumGameClass;
import com.salvai.centrum.actors.effects.Explosion;
import com.salvai.centrum.actors.enemys.EnemyBall;
import com.salvai.centrum.actors.player.Missile;
import com.salvai.centrum.enums.GameState;
import com.salvai.centrum.enums.GameType;
import com.salvai.centrum.input.GameInputProcessor;
import com.salvai.centrum.utils.Constants;
import com.salvai.centrum.utils.GameFlowManager;

import static com.salvai.centrum.enums.GameState.RUNNING;

public class GameScreen extends ScreenAdapter {

    public CentrumGameClass game;
    public int countdownTime;
    public GameFlowManager gameFlowManager;
    private int fadeOutTime;
    private GlyphLayout scoreLayout;
    private Texture pauseTexture;
    private boolean adVisible;



    public GameScreen(CentrumGameClass gameClass) {
        super();
        game = gameClass;

        adVisible = false;

        game.gameState = GameState.RUNNING;
        game.score = 0;

        gameFlowManager = new GameFlowManager(game);

        countdownTime = 199;
        fadeOutTime = 20;

        scoreLayout = new GlyphLayout(game.font, "3");

        pauseTexture = game.assetsManager.manager.get(Constants.PAUSE_BUTTON_IMAGE_NAME, Texture.class);


        //to catch back key
        Gdx.input.setInputProcessor(new GameInputProcessor(this));
        Gdx.input.setCatchBackKey(true);

    }

    @Override
    public void render(float delta) {
        setupScreen();

        if (countdownTime > 0 && game.gameState == RUNNING) {
            if (countdownTime < 180) {
                gameFlowManager.ball.sprite.setAlpha(1);
                scoreLayout.setText(game.font, "" + ((countdownTime / 60) + 1));
                countdownTime -= delta;
            } else {
                //fade in style
                countdownTime -= delta;
                gameFlowManager.ball.sprite.setAlpha((float) (199 - countdownTime) / 20);
            }
        } else if (game.gameState == RUNNING) {
            gameFlowManager.update(delta);
        }
        drawGame(delta);


        if (gameFlowManager.gameOver && gameFlowManager.explosions.size == 0) {
            if (game.levelSucceed && fadeOutTime > 1) {
                fadeOutTime -= delta;
                gameFlowManager.ball.sprite.setAlpha((float)fadeOutTime / 20);
            } else {
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        }
    }


    private void setupScreen() {
        Gdx.gl.glClearColor(Constants.BACKGROUND_COLOR.r, Constants.BACKGROUND_COLOR.g, Constants.BACKGROUND_COLOR.b, Constants.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }


    private void drawGame(float delta) {
        game.batch.begin();
        if (game.gameState == RUNNING)
            game.drawBackground(delta);
        else
            game.drawPause();

        //draw enemies
        for (EnemyBall enemy : gameFlowManager.enemies)
            enemy.sprite.draw(game.batch);


        if (!gameFlowManager.gameOver)
            //draw missiles
            for (Missile missile : gameFlowManager.missiles)
                missile.sprite.draw(game.batch);

        if (countdownTime == 0)
            scoreLayout.setText(game.font, "" + game.score);

        if (game.gameType == GameType.ENDLESS) {
            if (!gameFlowManager.gameOver) {
                gameFlowManager.ball.sprite.draw(game.batch);
                game.font.draw(game.batch, scoreLayout, Constants.WIDTH_CENTER - scoreLayout.width * 0.5f, Constants.HEIGHT_CENTER + scoreLayout.height * 0.5f);
            }
        } else if (gameFlowManager.gameOver) {
            if (game.levelSucceed) {
                gameFlowManager.ball.sprite.draw(game.batch);
                game.font.draw(game.batch, scoreLayout, Constants.WIDTH_CENTER - scoreLayout.width * 0.5f, Constants.HEIGHT_CENTER + scoreLayout.height * 0.5f);
            }
        } else {
            gameFlowManager.ball.sprite.draw(game.batch);
            game.font.draw(game.batch, scoreLayout, Constants.WIDTH_CENTER - scoreLayout.width * 0.5f, Constants.HEIGHT_CENTER + scoreLayout.height * 0.5f);
        }


        //PAUSED
        if (game.gameState == GameState.PAUSED) {
            game.batch.draw(pauseTexture, Constants.WIDTH_CENTER - pauseTexture.getWidth() * 0.5f, Constants.SCREEN_HEIGHT * 0.70f);
        }

        //explosions
        for (Explosion explosion : gameFlowManager.explosions) {
            if (game.gameState == RUNNING)
                explosion.particleEffect.draw(game.batch, delta);
            else
                explosion.particleEffect.draw(game.batch);
        }
        game.batch.end();
    }


    @Override
    public void pause() {
        //save score
        game.preferences.flush();
        if (game.gameState == RUNNING)
            game.gameState = GameState.PAUSED;
    }

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when teh screen size is changed
        game.viewport.update(width, height, true);
    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(new GameInputProcessor(this));
    }


}
