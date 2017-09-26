package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;
import com.miv.Main;
import com.miv.Options;

/**
 * Created by Miv on 5/23/2017.
 */
public class PlayerComponent implements Component, Pool.Poolable {
    // Maximum distance from player origin of any placed circle when customizing player
    private float customizationRadius = Options.INITIAL_PLAYER_CUSTOMIZATION_RADIUS;
    private float pixelPoints;

    private float score;

    @Override
    public void reset() {
        pixelPoints = 0;
        score = 0;
        customizationRadius = Options.INITIAL_PLAYER_CUSTOMIZATION_RADIUS;
    }

    public float getPixelPoints() {
        return pixelPoints;
    }

    public void addPixelPoints(Main main, float pixelPoints, boolean contributesToScore) {
        this.pixelPoints += pixelPoints;
        main.updateScreenActors();

        // Create text from the player that displays gain in pp
        if(Options.SHOW_PP_GAIN_FLOATING_TEXT && pixelPoints != 0) {
            if(pixelPoints > 0) {
                if (pixelPoints < 10) {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), "+" + String.format("%.2f", pixelPoints) + "pp", Color.BLACK);
                } else if (pixelPoints < 100) {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), "+" + String.format("%.1f", pixelPoints) + "pp", Color.BLACK);
                } else {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), "+" + String.valueOf(Math.round(pixelPoints)) + "pp", Color.BLACK);
                }
            } else {
                if (pixelPoints > -10) {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), String.format("%.2f", pixelPoints) + "pp", Color.BLACK);
                } else if (pixelPoints > -100) {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), String.format("%.1f", pixelPoints) + "pp", Color.BLACK);
                } else {
                    main.getRenderSystem().addFloatingText(main.getPlayer(), String.valueOf(Math.round(pixelPoints)) + "pp", Color.BLACK);
                }
            }
        }

        if(contributesToScore && pixelPoints > 0) {
            score += pixelPoints;
        }
    }

    public void setPixelPoints(Main main, float pixelPoints, boolean contributesToScore) {
        addPixelPoints(main, pixelPoints - this.pixelPoints, contributesToScore);
    }

    public float getCustomizationRadius() {
        return customizationRadius;
    }

    public void setCustomizationRadius(float customizationRadius) {
        this.customizationRadius = customizationRadius;
    }

    public float getScore() {
        return score;
    }
}
