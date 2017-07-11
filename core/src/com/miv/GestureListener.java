package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import components.HitboxComponent;
import utils.Point;

/**
 * Created by Miv on 6/9/2017.
 */
public class GestureListener implements GestureDetector.GestureListener {
    private Main main;
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

    public GestureListener(Main main, InputMultiplexer inputMultiplexer) {
        this.main = main;
        movementDragTouchDownPoint = new Point(-1, 0);
        movementDragCurrentPoint = new Point(-1, 0);

        inputMultiplexer.addProcessor(new TouchListener());
    }

    public void setPlayer(Entity player) {
        playerHitbox = Mappers.hitbox.get(player);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME) {
            // Prevent movement touches in a rectangle around the attack buttons
            if(!(x > main.getHud().getDisableGesturesLowerXBound() && y > main.getHud().getDisableGesturesLowerYBound())) {
                movementDragTouchDownPoint.x = x;
                movementDragTouchDownPoint.y = y;
                movementDragCurrentPoint.x = x;
                movementDragCurrentPoint.y = y;
                movementArrowLength = 0;
                movementArrowAngle = MathUtils.PI2;
            }
        }
        return false;
    }

    public boolean touchUp(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME) {
            // Prevent movement touches in a rectangle around the attack buttons
            movementDragTouchDownPoint.x = -1;
            playerHitbox.setVelocity(0, 0);
        }
        return false;
    }

    public boolean touchDragged(float x, float y, int pointer, int button) {
        if(main.getState() == Main.GameState.MAIN_GAME && movementDragTouchDownPoint.x != -1) {
            // Prevent movement touches in a rectangle around the attack buttons
            movementDragCurrentPoint.x = x;
            movementDragCurrentPoint.y = y;

            // Get angle from touch down to current finger pointer position
            movementArrowAngle = MathUtils.atan2(y - movementDragTouchDownPoint.y, x - movementDragTouchDownPoint.x);
            movementArrowLength = Math.min((float) Math.sqrt((x - movementDragTouchDownPoint.x) * (x - movementDragTouchDownPoint.x) + (y - movementDragTouchDownPoint.y) * (y - movementDragTouchDownPoint.y)), Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE);
            float velocityX = (movementArrowLength / Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE) * playerHitbox.getMaxSpeed() * MathUtils.cos(movementArrowAngle);
            float velocityY = -(movementArrowLength / Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE) * playerHitbox.getMaxSpeed() * MathUtils.sin(movementArrowAngle);

            System.out.println(playerHitbox.getMaxSpeed());
            playerHitbox.setVelocity(velocityX, velocityY);
            playerHitbox.setLastFacedAngle(movementArrowAngle);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
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
        return false;
    }

    @Override
    public void pinchStop() {

    }

    public Point getMovementDragTouchDownPoint() {
        return movementDragTouchDownPoint;
    }

    public Point getMovementDragCurrentPoint() {
        return movementDragCurrentPoint;
    }

    public float getMovementArrowLength() {
        return movementArrowLength;
    }

    public float getMovementArrowAngle() {
        return movementArrowAngle;
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
