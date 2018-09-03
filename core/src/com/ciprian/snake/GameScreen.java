package com.ciprian.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {

    SpriteBatch batch;
    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;

    public static final float MOVE_TIME = 0.3F;
    private float timer = MOVE_TIME;

    private static final int SNAKE_MOVEMENT = 32;
    private int snakeX = 0, snakeY = 0;

    private Array<BodyPart> bodyParts = new Array<BodyPart>();

    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    //Directions
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;
    private int snakeDirection = RIGHT;
    private boolean directionSet;

    //apple
    private boolean appleAvailable = false;
    private int appleX, appleY;

    //grid
    private ShapeRenderer shapeRenderer;
    private static final int GRID_CELL = 32;

    private boolean hasHit = false;

    private STATE state = STATE.PLAYING;

    private BitmapFont bitmapFont;

    @Override
    public void show() {
        super.show();
        bitmapFont = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        switch (state) {
            case PLAYING: {
                queryInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
            }
            break;
            case GAME_OVER: {

            }
            break;
        }
        clearScreen();
        drawGrid();
        draw();
    }

    private void updateSnake(float delta) {
        if (!hasHit) {
            timer -= delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
                checkForOutOfBounds();
                updateBodyPartsPosition();
                checkSnakeBodyCollision();
                directionSet = false;
            }
        }
    }

    private void clearScreen() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    //Rendering code
    private void draw() {
        batch.begin();
        batch.draw(snakeHead, snakeX, snakeY);
        for(BodyPart bodyPart : bodyParts) {
            bodyPart.draw(batch);
        }
        if(appleAvailable) {
            batch.draw(apple, appleX, appleY);
        }
        batch.end();
    }

    private void checkForOutOfBounds() {
        if(snakeX >= Gdx.graphics.getWidth()) {
            snakeX = 0;
        }
        if(snakeX < 0) {
            snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        }
        if(snakeY >= Gdx.graphics.getHeight()) {
            snakeY = 0;
        }
        if(snakeY < 0) {
            snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
        }
    }

    private void moveSnake() {

        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;

        switch (snakeDirection) {
            case RIGHT: {
                snakeX += SNAKE_MOVEMENT;
                return;
            }
            case LEFT: {
                snakeX -= SNAKE_MOVEMENT;
                return;
            }
            case UP: {
                snakeY += SNAKE_MOVEMENT;
                return;
            }
            case DOWN: {
                snakeY -= SNAKE_MOVEMENT;
                return;
            }
        }
    }

    private void queryInput() {
        boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if(lPressed) updateDirection(LEFT);
        if(rPressed) updateDirection(RIGHT);
        if(uPressed) updateDirection(UP);
        if(dPressed) updateDirection(DOWN);
    }

    private void checkAndPlaceApple() {
        if(!appleAvailable) {
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            } while (appleX == snakeX && appleY == snakeY);
        }
    }

    private void checkAppleCollision() {
        if(appleAvailable && appleX == snakeX && appleY == snakeY) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private void updateBodyPartsPosition() {
        if(bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void drawGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int x = 0; x < Gdx.graphics.getWidth(); x += GRID_CELL) {
            for(int y = 0; y < Gdx.graphics.getHeight(); y += GRID_CELL) {
                shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
                shapeRenderer.setColor(255,255,255, 0.1F);

            }
        }
        shapeRenderer.end();
    }

    private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection) {
        if(snakeDirection != oppositeDirection || bodyParts.size == 0) {
            snakeDirection = newSnakeDirection;
        }
    }

    private void updateDirection(int newSnakeDirection) {
        if(!directionSet && snakeDirection != newSnakeDirection) {
            directionSet = true;
            switch (newSnakeDirection) {
                case LEFT: {
                    updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
                }
                break;
                case RIGHT: {
                    updateIfNotOppositeDirection(newSnakeDirection, LEFT);
                }
                break;
                case UP: {
                    updateIfNotOppositeDirection(newSnakeDirection, DOWN);
                }
                break;
                case DOWN: {
                    updateIfNotOppositeDirection(newSnakeDirection, UP);
                }
                break;
            }
        }
    }

    private void checkSnakeBodyCollision() {
        for(BodyPart bodyPart : bodyParts) {
            if(bodyPart.x == snakeX && bodyPart.y == snakeY) state = STATE.GAME_OVER;
        }
    }

    private class BodyPart {

        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture) {
            this.texture = texture;
        }

        public void updateBodyPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch) {
            if(!(x == snakeX && y == snakeY)) batch.draw(texture, x, y);
        }
    }

    private enum STATE {
        PLAYING, GAME_OVER
    }



}
