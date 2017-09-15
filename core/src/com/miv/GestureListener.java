package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import components.HitboxComponent;
import components.PlayerComponent;
import screens.MapScreen;
import utils.Point;

import static screens.HUD.SMALL_BUTTON_PADDING;
import static screens.HUD.SMALL_BUTTON_SIZE;

/**
 * Created by Miv on 6/9/2017.
 */
public class GestureListener implements GestureDetector.GestureListener {
    private Main main;
    private PlayerComponent playerPlayerComponent;
    private HitboxComponent playerHitbox;

    // The point where the finger was touched down to move the player
    // x-pos is set to -1 in place of movementDragTouchDownPoint being set to null
    private Point movementDragTouchDownPoint;
    // The current position of the finger after touching down to move the player
    private Point movementDragCurrentPoint;
    // Length of the movement arrow in pixels
    private float movementArrowLength;
    // Angle from touch down to current point
    private float movementArrowAngle;

    // The point where the finger was touched down to set shooting angle of the player
    // x-pos is set to -1 in place of shootingDragTouchDownPoint being set to null
    private Point shootingDragTouchDownPoint;
    // The current position of the finger after touching down to rotate the player
    private Point shootingDragCurrentPoint;
    // Length of the movement arrow in pixels
    private float shootingArrowLength;
    // Angle from touch down to current point
    private float shootingArrowAngle;

    private float screenWidth;
    private float screenHeight;
    private float screenSplitX;

    private MapScreen mapScreen;

    public GestureListener(Main main, InputMultiplexer inputMultiplexer) {
        this.main = main;
        movementDragTouchDownPoint = new Point(-1, 0);
        movementDragCurrentPoint = new Point(-1, 0);
        shootingDragCurrentPoint = new Point(-1, 0);
        shootingDragTouchDownPoint = new Point(-1, 0);

        inputMultiplexer.addProcessor(new TouchListener());
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();
        screenSplitX = screenWidth/2f;
    }

    public void setPlayer(Entity player) {
        playerPlayerComponent = Mappers.player.get(player);
        playerHitbox = Mappers.hitbox.get(player);
    }

    public void setMapScreen(MapScreen mapScreen) {
        this.mapScreen = mapScreen;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME) {
            // Prevent movement touches on right side of screen
            if(x <= screenSplitX) {
                movementDragTouchDownPoint.x = x;
                movementDragTouchDownPoint.y = y;
                movementDragCurrentPoint.x = x;
                movementDragCurrentPoint.y = y;
                movementArrowLength = 0;
                movementArrowAngle = MathUtils.PI2;
            }
            // Prevent rotation touches on left side of screen and near buttons
            if(x > screenSplitX
                    && y > SMALL_BUTTON_SIZE + SMALL_BUTTON_PADDING) {
                shootingDragTouchDownPoint.x = x;
                shootingDragTouchDownPoint.y = y;
                shootingDragCurrentPoint.x = x;
                shootingDragCurrentPoint.y = y;
                shootingArrowLength = 0;
                shootingArrowAngle = MathUtils.PI2;

                playerHitbox.setIsShooting(true);
            }
        } else if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().touchDown(x, screenHeight - y);
        }
        return false;
    }

    public boolean touchUp(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME) {
            if(movementDragTouchDownPoint.x != -1 && x < screenSplitX) {
                movementTouchUp();
            }
            if(shootingDragTouchDownPoint.x != -1 && x >= screenSplitX) {
                shootingTouchUp();
            }
        } else if(main.getState() == Main.GameState.MAP) {
            mapScreen.touchUp(x, y);
        } else if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().touchUp(x, screenHeight - y);
        }
        return false;
    }

    private void movementTouchUp() {
        movementDragTouchDownPoint.x = -1;
        if (!playerHitbox.isTravelling()) {
            playerHitbox.setVelocity(0, 0);
        }
    }

    public void shootingTouchUp() {
        shootingDragTouchDownPoint.x = -1;
        playerHitbox.setIsShooting(false);
    }

    public boolean touchDragged(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME) {
            if(movementDragTouchDownPoint.x != -1 && x < screenSplitX) {
                movementDragCurrentPoint.x = x;
                movementDragCurrentPoint.y = y;

                movementArrowAngle = MathUtils.atan2(y - movementDragTouchDownPoint.y, x - movementDragTouchDownPoint.x);
                movementArrowLength = Math.min((float) Math.sqrt((x - movementDragTouchDownPoint.x) * (x - movementDragTouchDownPoint.x) + (y - movementDragTouchDownPoint.y) * (y - movementDragTouchDownPoint.y)), Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE);

                if(!playerHitbox.isTravelling()) {
                    // Get angle from touch down to current finger pointer position
                    float velocityX = (movementArrowLength / Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE) * playerHitbox.getMaxSpeed() * MathUtils.cos(movementArrowAngle);
                    float velocityY = (movementArrowLength / Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE) * playerHitbox.getMaxSpeed() * MathUtils.sin(movementArrowAngle);

                    playerHitbox.setVelocity(velocityX, -velocityY);
                }
            }
            if(shootingDragTouchDownPoint.x != -1 && x >= screenSplitX) {
                shootingDragCurrentPoint.x = x;
                shootingDragCurrentPoint.x = y;

                shootingArrowAngle = MathUtils.atan2(y - shootingDragTouchDownPoint.y, x - shootingDragTouchDownPoint.x);
                shootingArrowLength = Math.min((float) Math.sqrt((x - shootingDragTouchDownPoint.x) * (x - shootingDragTouchDownPoint.x) + (y - shootingDragTouchDownPoint.y) * (y - shootingDragTouchDownPoint.y)), Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE);

                playerHitbox.setTargetAngle(-shootingArrowAngle);
                playerHitbox.setAimingAngle(-shootingArrowAngle);
            }
        } else if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().touchDragged(x, screenHeight - y);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().tap(x, screenHeight - y);
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if(main.getState() == Main.GameState.MAP) {
            mapScreen.pan(x, y, deltaX, deltaY);
        } else if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().pan(x, y, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if(main.getState() == Main.GameState.MAP) {
            mapScreen.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        } else if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }
        return false;
    }

    @Override
    public void pinchStop() {
        if(main.getState() == Main.GameState.CUSTOMIZE) {
            main.getPlayerBuilder().pinchStop();
        }
    }

    public Point getMovementDragTouchDownPoint() {
        return movementDragTouchDownPoint;
    }

    public Point getShootingDragTouchDownPoint() {
        return shootingDragTouchDownPoint;
    }

    public float getMovementArrowLength() {
        return movementArrowLength;
    }

    public float getMovementArrowAngle() {
        return movementArrowAngle;
    }

    public float getShootingArrowLength() {
        return shootingArrowLength;
    }

    public float getShootingArrowAngle() {
        return shootingArrowAngle;
    }

    // Class used solely for listening for touchUp/touchDragged because libgdx is dumb
    public class TouchListener implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return GestureListener.this.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return GestureListener.this.touchDragged(screenX, screenY, pointer, Input.Buttons.LEFT);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            return false;
        }
    }
}
