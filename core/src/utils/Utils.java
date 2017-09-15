package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.miv.Mappers;

import java.util.ArrayList;

import ai.AI;
import ai.SimpleFollowTarget;
import components.AIComponent;
import components.BossComponent;
import components.EnemyComponent;
import components.HitboxComponent;

/**
 * Created by Miv on 6/26/2017.
 */
public class Utils {
    public static class PopupDialogBox extends Actor {
        private Window window;
        private Skin skin;
        private Table table;
        private Label text;

        public PopupDialogBox(Skin skin, String text, float width, float height, float verticalPadding) {
            this.skin = skin;

            window = new Window("", skin);
            window.center();

            table = new Table();
            table.center();
            table.padTop(verticalPadding);
            table.padBottom(verticalPadding);
            table.setPosition((Gdx.graphics.getWidth() - width)/2f, (Gdx.graphics.getHeight() - height)/2f);
            window.add(table);

            this.text = new Label(text, skin);
            this.text.setColor(Color.WHITE);
            table.add(this.text);
            table.row();
        }

        public void setText(String text) {
            this.text.setText(text);
        }

        /**
         * @param text Text on the button
         * @param task Task to be done when the button is clicked
         */
        public void addButton(String text, final Timer.Task task) {
            TextButton textButton = new TextButton(text, skin);
            textButton.getLabel().setFontScale(0.6f);
            textButton.getLabel().setColor(Color.WHITE);
            textButton.setSize(100f, 80f);
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PopupDialogBox.this.remove();
                    if(task != null) {
                        task.run();
                    }
                }
            });
            table.add(textButton);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);

            window.draw(batch, parentAlpha);
        }
    }

    /**
     * Checks if c overlaps with any circle in circles
     */
    public static boolean overlaps(CircleHitbox c, ArrayList<CircleHitbox> circles) {
        for(CircleHitbox a : circles) {
            if(c.overlaps(a)) {
                return true;
            }
        }
        return false;
    }

    public static float getDistance(Point p1, Point p2) {
        return (float)Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    public static float getDistance(Point p1, int x2, int y2) {
        return (float)Math.sqrt((p1.x - x2)*(p1.x - x2) + (p1.y - y2)*(p1.y - y2));
    }

    public static float getDistance(Point p1, float x2, float y2) {
        return (float)Math.sqrt((p1.x - x2)*(p1.x - x2) + (p1.y - y2)*(p1.y - y2));
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    public static float normalizeAngle(float angleInRadians) {
        if(angleInRadians < 0) {
            return normalizeAngle(angleInRadians + MathUtils.PI2);
        } else if(angleInRadians > MathUtils.PI2) {
            return normalizeAngle(angleInRadians - MathUtils.PI2);
        } else {
            return angleInRadians;
        }
    }

    public static Entity cloneEnemy(PooledEngine engine, Entity original, ArrayList<CircleHitbox> circles, boolean cloneAI) {
        Entity e = engine.createEntity();

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        HitboxComponent originalHitbox = Mappers.hitbox.get(original);
        hitbox.setSubEntityStats(originalHitbox.getSubEntityStats());
        hitbox.setMaxSpeed(originalHitbox.getMaxSpeed());
        hitbox.setOrigin(originalHitbox.getOrigin().x, originalHitbox.getOrigin().y);
        hitbox.setIsShooting(true);
        hitbox.setVelocity(originalHitbox.getVelocity().x, originalHitbox.getVelocity().y);
        hitbox.setAcceleration(originalHitbox.getAcceleration().x, originalHitbox.getAcceleration().y);
        for(CircleHitbox c : circles) {
            hitbox.addCircle(c, false);
        }
        hitbox.setLastFacedAngle(originalHitbox.getLastFacedAngle());
        hitbox.setAimingAngle(originalHitbox.getAimingAngle());
        e.add(hitbox);

        if(cloneAI && Mappers.ai.has(original)) {
            e.add(engine.createComponent(AIComponent.class).setAi(Mappers.ai.get(original).getAi().getSubEntityAI().clone(e)));
        }

        e.add(engine.createComponent(EnemyComponent.class));

        if(Mappers.boss.has(e)) {
            e.add(engine.createComponent(BossComponent.class));
        }

        return e;
    }
}
