package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import systems.RenderSystem;
import utils.Point;

/**
 * Custom Camera class that updates the projection matrices of all the relevant rendering batches in the game.
 * Created by Miv on 6/11/2017.
 */
public class Camera extends OrthographicCamera {
    private static final float speed = 0.1f;
    private static final float ispeed = 1f - speed;

    private Point focus;
    private RenderSystem renderSystem;

    private boolean lockedPosition;

    public Camera(RenderSystem renderSystem) {
        this.renderSystem = renderSystem;
    }

    public void resetViewport() {
        Viewport viewport = new FillViewport(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, this);
        viewport.apply();
        update();
    }

    @Override
    public void update() {
        if(focus != null && !lockedPosition) {
            position.scl(ispeed);
            position.add(focus.x * speed, focus.y * speed, 0);
        }
        super.update();
        renderSystem.getShapeRenderer().setProjectionMatrix(combined);
        renderSystem.getBatch().setProjectionMatrix(combined);
    }

    public void teleportTo(Entity e) {
        Point pos = Mappers.hitbox.get(e).getOrigin();
        position.set(pos.x, pos.y, 0);
    }

    public void setFocus(Entity focus) {
        this.focus = Mappers.hitbox.get(focus).getOrigin();
    }

    public void setLockedPosition(boolean lockedPosition) {
        this.lockedPosition = lockedPosition;
    }
}
