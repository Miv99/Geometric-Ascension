package screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.miv.*;
import com.miv.Options;

import java.util.ArrayList;

import components.HitboxComponent;
import factories.AttackPatternFactory;
import jdk.nashorn.internal.runtime.Specialization;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;
import utils.Utils;

/**
 * Created by Miv on 7/31/2017.
 */
public class PlayerBuilder implements Screen {
    private class SpecializationChooser extends Group {
        // In seconds
        private static final float ANIMATION_TIME = 0.4f;
        private static final float TEXT_BUTTON_HEIGHT = 100f;
        private static final float TEXT_BUTTON_VERTICAL_PADDING = 40f;
        private static final float DESCRIPTION_WINDOW_WIDTH = 600f;
        private static final float DESCRIPTION_WINDOW_HEIGHT = 400f;

        private GlyphLayout layout = new GlyphLayout();

        private Skin skin;
        private TextButton borderedLabel;
        private Array<TextButton> choices = new Array<TextButton>();
        private Window descriptionWindow;
        private boolean descriptionWindowIsShowing;
        private TextButton chooseButton;
        private Label descriptionLabel;
        private Button cancelChoosing;

        public SpecializationChooser(Skin skin) {
            this.skin = skin;

            // New game button that covers entire screen; also works as color overlay
            cancelChoosing = new Button(skin, "white-overlay");
            cancelChoosing.setColor(1f, 1f, 1f, 0f);
            cancelChoosing.setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
            cancelChoosing.setPosition(0, 0);
            cancelChoosing.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    playHideAnimation();
                }
            });
            cancelChoosing.setTouchable(Touchable.disabled);
            addActor(cancelChoosing);

            borderedLabel = new TextButton("Choose a specialization", skin, "window");
            borderedLabel.getLabel().setFontScale(BODY_LABEL_FONT_SCALE);
            borderedLabel.getLabel().setColor(Color.BLACK);
            borderedLabel.setSize(editableAreaRightXBound - 50f, TEXT_BUTTON_HEIGHT);
            borderedLabel.setPosition(-borderedLabel.getWidth(), pp.getY() - TOP_PADDING - borderedLabel.getHeight());
            borderedLabel.setTouchable(Touchable.disabled);
            addActor(borderedLabel);

            descriptionWindow = new Window("", skin, "text-button-window");
            descriptionWindow.setKeepWithinStage(false);
            descriptionWindow.top().left().pad(0, 0, 0, 0);
            descriptionWindow.setSize(DESCRIPTION_WINDOW_WIDTH, DESCRIPTION_WINDOW_HEIGHT);
            descriptionWindow.setPosition(-descriptionWindow.getWidth(), borderedLabel.getY() - descriptionWindow.getHeight() - TEXT_BUTTON_VERTICAL_PADDING);
            addActor(descriptionWindow);

            chooseButton = new TextButton("Choose", skin);

            descriptionLabel = new Label("", skin);
            descriptionLabel.setAlignment(Align.top);
            descriptionLabel.setAlignment(Align.left);
            descriptionLabel.setFontScale(BODY_LABEL_FONT_SCALE);
            descriptionLabel.setSize(descriptionWindow.getWidth(), descriptionWindow.getHeight());
            descriptionLabel.setPosition(LEFT_PADDING, TOP_PADDING);
            descriptionLabel.setColor(Color.BLACK);
            descriptionLabel.setWrap(true);
            descriptionWindow.add(descriptionLabel).top().left().width(descriptionWindow.getWidth() - LEFT_PADDING*2f).height(descriptionWindow.getHeight() - chooseButton.getPrefHeight() - TOP_PADDING).padLeft(LEFT_PADDING).row();

            chooseButton.getLabel().setColor(Color.WHITE);
            chooseButton.getLabel().setFontScale(0.6f);
            chooseButton.setPosition((descriptionWindow.getWidth() - chooseButton.getPrefWidth()) / 2f, TOP_PADDING);
            chooseButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    modifyCircleSpecialization(selectedCircle, (CircleHitbox.Specialization) chooseButton.getUserObject());
                    playHideAnimation();
                    updateActors();
                }
            });
            chooseButton.pack();
            descriptionWindow.add(chooseButton).width(chooseButton.getPrefWidth()).height(chooseButton.getPrefHeight()).center();
        }

        public void addChoices(CircleHitbox.Specialization parent) {
            choices.clear();
            for(CircleHitbox.Specialization sp : parent.getChildren()) {
                addChoice(sp);
            }
        }

        public void addChoice(final CircleHitbox.Specialization choice) {
            final TextButton button = new TextButton(choice.getStringRepresentation(), skin, "window");
            button.getLabel().setColor(Color.BLACK);
            button.getLabel().setFontScale(BODY_LABEL_FONT_SCALE);
            layout.setText(button.getStyle().font, button.getText());
            button.setSize((layout.width * BODY_LABEL_FONT_SCALE) + LEFT_PADDING * 2f, TEXT_BUTTON_HEIGHT);
            button.setPosition(-button.getWidth(), borderedLabel.getY() - (choices.size + 1) * (TEXT_BUTTON_HEIGHT + TEXT_BUTTON_VERTICAL_PADDING));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (descriptionWindowIsShowing) {
                        hideDescriptionWindow();
                    } else {
                        showDescription(button, choice);
                    }
                }
            });
            addActor(button);

            choices.add(button);
        }

        private void showDescription(TextButton clicked, CircleHitbox.Specialization specialization) {
            chooseButton.setUserObject(specialization);

            // All buttons except the one clicked disappear into left side of screen
            for(TextButton button : choices) {
                if(!button.equals(clicked)) {
                    button.addAction(Actions.moveTo(-button.getWidth(), button.getY(), ANIMATION_TIME));
                }
            }
            // Clicked button moves right and stays attached to description window
            clicked.addAction(Actions.moveBy(DESCRIPTION_WINDOW_WIDTH, 0, ANIMATION_TIME));
            // Description window moves right
            descriptionWindow.addAction(Actions.moveTo(0, descriptionWindow.getY(), ANIMATION_TIME));
            descriptionWindowIsShowing = true;

            descriptionLabel.setText(specialization.getDescription());
        }

        public void playShowAnimation() {
            chooseButton.setTouchable(Touchable.enabled);
            for(Actor a : choices) {
                a.setTouchable(Touchable.enabled);
            }

            cancelChoosing.setTouchable(Touchable.enabled);
            cancelChoosing.addAction(Actions.alpha(0.7f, ANIMATION_TIME));
            disableNonPopupInputs();

            // TODO White overlay on screen transitions from 0% to 50% alpha

            borderedLabel.setX(-borderedLabel.getWidth());
            borderedLabel.addAction(Actions.moveTo(0, borderedLabel.getY(), ANIMATION_TIME));

            // Show choices
           for(Actor a : choices) {
               a.addAction(Actions.moveTo(0, a.getY(), ANIMATION_TIME));
           }
        }

        public void playHideAnimation() {
            chooseButton.setTouchable(Touchable.disabled);
            for(Actor a : choices) {
                a.setTouchable(Touchable.disabled);
            }

            cancelChoosing.setTouchable(Touchable.disabled);
            cancelChoosing.addAction(Actions.fadeOut(ANIMATION_TIME));
            // TODO White overlay transitions to 0% alpha

            borderedLabel.setX(0);
            borderedLabel.addAction(Actions.moveTo(-borderedLabel.getWidth(), borderedLabel.getY(), ANIMATION_TIME));

            // Hide choices
            for(Actor a : choices) {
                a.addAction(Actions.moveTo(-a.getWidth(), a.getY(), ANIMATION_TIME));
            }

            if(descriptionWindowIsShowing) {
                descriptionWindowIsShowing = false;
                descriptionWindow.addAction(Actions.moveTo(-descriptionWindow.getWidth(), descriptionWindow.getY(), ANIMATION_TIME));
            }

            enableNonPopupInputs();
        }

        public void hideDescriptionWindow() {
            descriptionWindowIsShowing = false;

            // Description window moves into left side of screen
            descriptionWindow.addAction(Actions.moveTo(-descriptionWindow.getWidth(), descriptionWindow.getY(), ANIMATION_TIME));

            // All choices move back to original positions
            for(Actor a : choices) {
                a.addAction(Actions.moveTo(0, a.getY(), ANIMATION_TIME));
            }
        }
    }
    private class AttackPatternInfo extends Window {
        // In seconds
        private static final float ANIMATION_TIME = 0.4f;

        private Label body;
        private Label title;
        private TextButton upgrade;

        private boolean isShowing;

        public AttackPatternInfo(String title, Skin skin, float width, float height) {
            super("", skin);
            setSize(width, height);

            setKeepWithinStage(false);
            top();
            left();
            pad(0, 0, 0, 0);

            // -5 to hide bottom border of the window
            setPosition(-width, -5);

            this.title = new Label(title, skin);
            this.title.setFontScale(TITLE_LABEL_FONT_SCALE);
            this.title.setAlignment(Align.top);
            this.title.setAlignment(Align.left);
            this.title.setColor(Color.BLACK);
            this.title.setPosition(LEFT_PADDING, getHeight() - 20f - TOP_PADDING);
            add(this.title).top().left().padLeft(LEFT_PADDING).width(width).height(80f).row();

            upgrade = new TextButton("Upgrade", skin);

            body = new Label("", skin);
            body.setAlignment(Align.top);
            body.setAlignment(Align.left);
            body.setFontScale(BODY_LABEL_FONT_SCALE);
            body.setSize(width, height);
            body.setPosition(LEFT_PADDING, TOP_PADDING);
            body.setColor(Color.BLACK);
            add(body).top().left().width(width).height(height - 80f - upgrade.getPrefHeight() - 5 - TOP_PADDING).padLeft(LEFT_PADDING).row();

            // Create upgrade button
            upgrade.getLabel().setColor(Color.WHITE);
            upgrade.getLabel().setFontScale(0.6f);
            upgrade.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    upgradeCircleAttackPattern(selectedCircle);
                }
            });
            upgrade.pack();
            add(upgrade).width(upgrade.getPrefWidth()).height(upgrade.getPrefHeight()).center();

            // Remove the capture listener added in Window that sends the window to front when clicked on
            getCaptureListeners().clear();
        }

        @Override
        public void setPosition(float x, float y, int alignment) {
            super.setPosition(x, y, alignment);
            if(attackPatternDisplayToggle != null) {
                attackPatternDisplayToggle.setPosition(x + getWidth() - 14f, y + (getHeight() - attackPatternDisplayToggle.getHeight()) / 2f);
            }
        }

        public void setTitleText(String text) {
            this.title.setText(text);
        }

        public void setBodyText(String text) {
            body.setText(text);
        }

        public void playShowAnimation() {
            isShowing = true;

            addAction(Actions.moveTo(0, 0, ANIMATION_TIME));
        }

        public void playHideAnimation() {
            isShowing = false;

            addAction(Actions.moveTo(-getWidth(), 0, ANIMATION_TIME));
        }
    }

    private static final float TITLE_LABEL_FONT_SCALE = 2.5f;
    private static final float BODY_LABEL_FONT_SCALE = 2f;

    private static final float PLAYER_RENDER_ANGLE = MathUtils.PI/2f;
    private static final Color RIGHT_HALF_OF_SCREEN_COLOR = new Color(225/255f, 225/255f, 225/255f, 0.5f);
    private static final float STATIC_SHAPE_RENDERER_LINE_WIDTH = 4f;
    private static final Color STATIC_SHAPE_RENDERER_GRAY = new Color(150/255f, 150/255f, 150/255f, 1f);
    private static final Color STATIC_SHAPE_RENDERER_LIGHT_GRAY = new Color(180/255f, 180/255f, 180/255f, 1f);
    private static final Color STATIC_SHAPE_RENDERER_GRID_LINE = new Color(RenderSystem.NORMAL_MAP_AREA_GRID_LINE_COLOR);
    // How much circles can overlap each other by (in world units) before being considered in invalid positions
    // Also applies to circles out of bounds
    private static final float CIRCLE_OVERLAP_LENIENCY = 5f;
    // Distance in screen units before a circle being moved snaps to x/y axes
    private static final float CIRCLE_SNAP_DISTANCE = 15f;
    // Difference in radians between current
    private static final float ATTACK_PATTERN_ANGLE_SNAP_MARGIN = MathUtils.degreesToRadians * 3f;

    private static final Color DAMAGE_TAKEN_AURA_COLOR = new Color(Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b, 0.35f);
    private static final Color LIFESTEAL_AURA_COLOR = new Color(Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, 0.35f);
    private static final Color MAX_HEALTH_AURA_COLOR = new Color(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.35f);

    private static final float ATTACK_PATTERN_ANGLE_ARROW_LENGTH = 200f;

    private Main main;
    private Skin skin;
    private Stage stage;
    private InputMultiplexer inputMultiplexer;
    private Entity player;
    // Shape renderer unaffected by camera
    private ShapeRenderer staticShapeRenderer;

    private boolean disableTouch;
    private boolean isPanningCamera;
    private CircleHitbox selectedCircle;
    private float selectedCircleRadiusInScreenUnits;
    private float selectedCircleAuraRadiusInScreenUnits;

    private ShapeRenderer playerShapeRenderer;
    private Batch playerRenderBatch;
    // Camera used to draw player render
    private OrthographicCamera playerRenderCamera;
    // Circles copied from player entity at time of screen showing
    private ArrayList<CircleHitbox> playerRender;
    private Point playerRenderOrigin;
    // Contains any new circles or circles with unsaved changes
    // Changed circles are removed from the player render and placed in here
    private ArrayList<CircleHitbox> unsavedCircles;
    private ArrayList<CircleHitbox> allCircles;
    private boolean unsavedChangesExist;

    // Actors that show up when a circle is selected
    private Label playerStatsTitleLabel;
    private Label playerStatsLabel;
    private Label circleStatsLabel;
    private ImageButton circleDeleteButton;
    private TextButton circleUpgradeButton;
    private AttackPatternInfo attackPatternInfo;
    private ImageButton attackPatternDisplayToggle;
    private SpecializationChooser specializationChooser;

    // Pp from deleting circles
    private float circleDeletionPp;

    private Label pp;
    private ImageButton saveChangesButton;
    private ImageButton discardChangesButton;

    private float screenWidth;
    private float screenHeight;
    private float editableAreaRightXBound;

    private Drawable newBubbleDrawable;
    private float newBubbleButtonX;
    private float newBubbleButtonY;
    private float newBubbleButtonLength;

    // Position of finger if is dragging a new circle into editable area
    private boolean isDraggingNewCircle;
    private Point newCircleDragCurrentPoint;
    // Position of finger if is dragging an existing circle around
    private boolean isMovingCircle;
    private Point movingCircleDragCurrentPoint;
    // Position of finger if it is dragging the angle of an attack pattern around
    private boolean isDraggingAttackPatternArrowAngle;
    private Point draggingAttackPatternArrowPos;
    private float draggingAttackPatternArrowAngle; // In radians
    // In screen coordinates
    private Point movingCircleDragOffset;

    private final float LEFT_PADDING = 25f;
    private final float TOP_PADDING = 25f;

    // Random stuff for pinch zooming
    private Vector2 oldInitialFirstPointer=null, oldInitialSecondPointer=null;
    private float oldScale;
    private boolean isPinching;

    public PlayerBuilder(final Main main, InputMultiplexer inputMultiplexer, AssetManager assetManager, final Entity player) {
        this.main = main;
        this.inputMultiplexer = inputMultiplexer;
        this.player = player;
        stage = new Stage();

        playerRenderOrigin = new Point(0, 0);
        newCircleDragCurrentPoint = new Point(-1, -1);
        movingCircleDragCurrentPoint = new Point(-1, -1);
        draggingAttackPatternArrowPos = new Point(-1, -1);
        movingCircleDragOffset = new Point(0, 0);
        playerRender = new ArrayList<CircleHitbox>();
        unsavedCircles = new ArrayList<CircleHitbox>();
        allCircles = new ArrayList<CircleHitbox>();
        playerShapeRenderer = new ShapeRenderer();
        playerShapeRenderer.setAutoShapeType(true);
        staticShapeRenderer = new ShapeRenderer();
        staticShapeRenderer.setAutoShapeType(true);
        playerRenderBatch = new SpriteBatch();
        playerRenderCamera = new OrthographicCamera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());

        // Back button
        Texture backButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_UP_PATH).path());
        Texture backButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle backButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(backButtonUp)), new TextureRegionDrawable(new TextureRegion(backButtonDown)), null, null, null, null);
        ImageButton backButton = new ImageButton(backButtonStyle);
        backButton.setSize(70f, 70f);
        backButton.setPosition(LEFT_PADDING, TOP_PADDING);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitToHud();
            }
        });
        stage.addActor(backButton);

        pp = new Label("", skin, "big");

        editableAreaRightXBound = screenWidth - LEFT_PADDING*2f - 450;

        pp.setFontScale(1f);
        pp.setText(Math.round(Mappers.player.get(player).getPixelPoints()) + "pp");
        pp.pack();
        pp.setPosition(LEFT_PADDING, screenHeight - TOP_PADDING - 55f);
        pp.setColor(Color.BLACK);
        stage.addActor(pp);

        // Save changes button
        Texture saveChangesButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CHECKMARK_BUTTON_UP_PATH).path());
        Texture saveChangesButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CHECKMARK_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle saveChangesButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(saveChangesButtonUp)), new TextureRegionDrawable(new TextureRegion(saveChangesButtonDown)), null, null, null, null);
        saveChangesButton = new ImageButton(saveChangesButtonStyle);
        saveChangesButton.setSize(70f, 70f);
        saveChangesButton.setPosition(editableAreaRightXBound - LEFT_PADDING - saveChangesButton.getWidth(), TOP_PADDING);
        saveChangesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveChanges();
            }
        });
        stage.addActor(saveChangesButton);

        // Discard changes button
        Texture discardChangesButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CANCEL_BUTTON_UP_PATH).path());
        Texture discardChangesButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CANCEL_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle discardChangesButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(discardChangesButtonUp)), new TextureRegionDrawable(new TextureRegion(discardChangesButtonDown)), null, null, null, null);
        discardChangesButton = new ImageButton(discardChangesButtonStyle);
        discardChangesButton.setSize(70f, 70f);
        discardChangesButton.setPosition(saveChangesButton.getX() - LEFT_PADDING - discardChangesButton.getWidth(), TOP_PADDING);
        discardChangesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                discardChanges();
            }
        });
        stage.addActor(discardChangesButton);

        newBubbleDrawable = main.getRenderSystem().bubbleDrawables[RenderSystem.HitboxTextureType.PLAYER_NEW_CIRCLE.getId()];
        newBubbleButtonLength = 150f;
        newBubbleButtonX = editableAreaRightXBound - newBubbleButtonLength;
        newBubbleButtonY = screenHeight - newBubbleButtonLength;

        playerStatsLabel = new Label("", skin);
        playerStatsLabel.setAlignment(Align.top);
        playerStatsLabel.setAlignment(Align.left);
        playerStatsLabel.setFontScale(BODY_LABEL_FONT_SCALE);
        playerStatsLabel.setSize(screenWidth - editableAreaRightXBound, screenHeight);
        playerStatsLabel.setPosition(editableAreaRightXBound + LEFT_PADDING, TOP_PADDING);
        playerStatsLabel.setColor(Color.BLACK);
        stage.addActor(playerStatsLabel);

        circleStatsLabel = new Label("", skin);
        circleStatsLabel.setAlignment(Align.top);
        circleStatsLabel.setAlignment(Align.left);
        circleStatsLabel.setFontScale(BODY_LABEL_FONT_SCALE);
        circleStatsLabel.setSize(screenWidth - editableAreaRightXBound, screenHeight);
        circleStatsLabel.setPosition(editableAreaRightXBound + LEFT_PADDING, TOP_PADDING);
        circleStatsLabel.setColor(Color.BLACK);
        circleStatsLabel.setVisible(false);
        stage.addActor(circleStatsLabel);

        Texture circleDeleteButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.DELETE_BUTTON_UP_PATH).path());
        Texture circleDeleteButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.DELETE_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle circleDeleteButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(circleDeleteButtonUp)), new TextureRegionDrawable(new TextureRegion(circleDeleteButtonDown)), null, null, null, null);
        circleDeleteButton = new ImageButton(circleDeleteButtonStyle);
        circleDeleteButton.setSize(70f, 70f);
        circleDeleteButton.setPosition(screenWidth - LEFT_PADDING - circleDeleteButton.getWidth(), screenHeight - TOP_PADDING - circleDeleteButton.getHeight());
        circleDeleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Cannot delete last circle
                if(allCircles.size() > 1) {
                    deleteCircle(selectedCircle);
                    deselectCircle();
                }
            }
        });
        circleDeleteButton.setVisible(false);
        stage.addActor(circleDeleteButton);

        playerStatsTitleLabel = new Label("PLAYER STATS", skin);
        playerStatsTitleLabel.setAlignment(Align.top);
        playerStatsTitleLabel.setAlignment(Align.left);
        playerStatsTitleLabel.setFontScale(TITLE_LABEL_FONT_SCALE);
        playerStatsTitleLabel.setPosition(editableAreaRightXBound + LEFT_PADDING, circleDeleteButton.getY() + circleDeleteButton.getHeight()/2f);
        playerStatsTitleLabel.setColor(Color.BLACK);
        stage.addActor(playerStatsTitleLabel);

        circleUpgradeButton = new TextButton("Upgrade", skin);
        circleUpgradeButton.getLabel().setColor(Color.WHITE);
        circleUpgradeButton.getLabel().setFontScale(0.6f);
        circleUpgradeButton.setPosition((Gdx.graphics.getWidth() - editableAreaRightXBound - circleUpgradeButton.getPrefWidth()) / 2f + editableAreaRightXBound, TOP_PADDING);
        circleUpgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCircle.isSpecializationAvailable()) {
                    specializationChooser.addChoices(selectedCircle.getSpecialization());
                    showSpecializationChooser();
                } else {
                    upgradeCircle(selectedCircle);
                }
            }
        });
        circleUpgradeButton.pack();
        circleUpgradeButton.setVisible(false);
        stage.addActor(circleUpgradeButton);

        attackPatternInfo = new AttackPatternInfo("WEAPON", skin, LEFT_PADDING*2f + 450f, screenHeight * 5/6f);
        stage.addActor(attackPatternInfo);

        Texture displayToggleUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_UP_PATH).path());
        Texture displayToggleDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_DOWN_PATH).path());
        attackPatternDisplayToggle = new ImageButton(new TextureRegionDrawable(new TextureRegion(displayToggleUp)), new TextureRegionDrawable(new TextureRegion(displayToggleDown)));
        attackPatternDisplayToggle.setSize(displayToggleUp.getWidth(), displayToggleUp.getHeight());
        attackPatternDisplayToggle.align(Align.left);
        attackPatternDisplayToggle.setPosition(attackPatternInfo.getX() + attackPatternInfo.getWidth() - 14f, attackPatternInfo.getY() + (attackPatternInfo.getHeight() - attackPatternInfo.getHeight()) / 2f);
        attackPatternDisplayToggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (attackPatternInfo.isShowing) {
                    attackPatternInfo.playHideAnimation();
                } else {
                    attackPatternInfo.playShowAnimation();
                }
                event.stop();
            }
        });
        stage.addActor(attackPatternDisplayToggle);

        specializationChooser = new SpecializationChooser(skin);
        stage.addActor(specializationChooser);

        //TODO: add button to attackPatternInfo that pulls out/pulls in the window

        // TODO: arrow keys to move circle precisely in top left corner (not part of table)

        updateActors();
        // Fixes random bug with positioning
        attackPatternDisplayToggle.setPosition(attackPatternInfo.getX() + attackPatternInfo.getWidth() - 14f, attackPatternInfo.getY() + (attackPatternInfo.getHeight() - attackPatternDisplayToggle.getHeight()) / 2f);
    }

    public void touchDown(float x, float y) {
        if(disableTouch || isPinching) {
            resetInputs();
            return;
        }
        // Disable touch near buttons
        if(y < TOP_PADDING + 70f && (x < LEFT_PADDING + 70f || (x > editableAreaRightXBound - LEFT_PADDING*2f - 140f && x < editableAreaRightXBound))) {
            return;
        }

        // Touch down started in new circle box
        if(x >= newBubbleButtonX && x <= newBubbleButtonX + newBubbleButtonLength && y >= newBubbleButtonY && y <= newBubbleButtonY + newBubbleButtonLength) {
            isDraggingNewCircle = true;
            newCircleDragCurrentPoint.x = x;
            newCircleDragCurrentPoint.y = y;
            selectedCircleRadiusInScreenUnits = playerRenderCamera.project(new Vector3(Options.DEFAULT_NEW_CIRCLE_RADIUS, 0, 0)).x - playerRenderCamera.project(new Vector3(0, 0, 0)).x;
            selectedCircleAuraRadiusInScreenUnits = playerRenderCamera.project(new Vector3(Options.DEFAULT_NEW_CIRCLE_RADIUS + Options.CIRCLE_AURA_RANGE, 0, 0)).x - playerRenderCamera.project(new Vector3(0, 0, 0)).x;
        }
        // Touch down on the selected circle
        else if(selectedCircle != null && getAllCirclesAtPoint(playerRenderCamera.unproject(new Vector3(x, -(y - screenHeight), 0))).contains(selectedCircle) && x < editableAreaRightXBound) {
            isMovingCircle = true;
            movingCircleDragCurrentPoint.x = x;
            movingCircleDragCurrentPoint.y = y;
            Vector3 selectedCircleScreenPos = playerRenderCamera.project(new Vector3(selectedCircle.x + playerRenderOrigin.x, selectedCircle.y + playerRenderOrigin.y, 0));
            movingCircleDragOffset.x = selectedCircleScreenPos.x - x;
            movingCircleDragOffset.y = selectedCircleScreenPos.y - y;
            selectedCircleRadiusInScreenUnits = playerRenderCamera.project(new Vector3(selectedCircle.radius, 0, 0)).x - playerRenderCamera.project(new Vector3(0, 0, 0)).x;
            selectedCircleAuraRadiusInScreenUnits = playerRenderCamera.project(new Vector3(selectedCircle.radius + Options.CIRCLE_AURA_RANGE, 0, 0)).x - playerRenderCamera.project(new Vector3(0, 0, 0)).x;
        }
        // Touch down on the tip of attack pattern arrow angle
        else if (selectedCircle != null && selectedCircle.getAttackPattern() != null && isWithinRangeOfAttackPatternArrowTip(playerRenderCamera.unproject(new Vector3(x, -(y - screenHeight), 0)))) {
            isDraggingAttackPatternArrowAngle = true;
            draggingAttackPatternArrowPos.x = x;
            draggingAttackPatternArrowPos.y = y;
            Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(x, -(y - screenHeight), 0));
            draggingAttackPatternArrowAngle = MathUtils.atan2(worldCoordinates.y - selectedCircle.y, worldCoordinates.x - selectedCircle.x) - PLAYER_RENDER_ANGLE;
        }
        else if (x < editableAreaRightXBound) {
            isPanningCamera = true;
        }
    }

    private boolean isWithinRangeOfAttackPatternArrowTip(Vector3 worldCoordinates) {
        float angle = selectedCircle.getAttackPattern().getAngleOffset() + PLAYER_RENDER_ANGLE;
        return Utils.getDistance(selectedCircle.x + playerRenderOrigin.x + MathUtils.cos(angle)*(selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), selectedCircle.y + playerRenderOrigin.y + MathUtils.sin(angle)*(selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), worldCoordinates.x, worldCoordinates.y) < 50;
    }

    public void touchUp(float x, float y) {
        if(disableTouch || isPinching) {
            resetInputs();
            return;
        }

        if(isDraggingNewCircle) {
            // Place down circle
            placeNewCircle(newCircleDragCurrentPoint.x, -(newCircleDragCurrentPoint.y - screenHeight));

            isDraggingNewCircle = false;
            newCircleDragCurrentPoint.x = -1;
        } else if(isMovingCircle) {
            // Place down circle
            moveCircle(selectedCircle, movingCircleDragCurrentPoint.x + movingCircleDragOffset.x, -(movingCircleDragCurrentPoint.y + movingCircleDragOffset.y - screenHeight));

            /**
             * Delay setting variable to false to avoid slight adjustments being counted as taps since {@link #tap(float, float)} deselects
             * circle iff isMovingCircle is true
             */
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    movingCircleDragCurrentPoint.x = -1;
                    isMovingCircle = false;
                }
            }, 0.04f);
        } else if(isDraggingAttackPatternArrowAngle) {
            modifyCircleAttackPatternAngle(selectedCircle, draggingAttackPatternArrowAngle);

            /**
             * Delay setting variable to false to avoid slight adjustments being counted as taps since {@link #tap(float, float)} deselects
             * circle iff isDraggingAttackPatternArrowAngle is true
             */
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    isDraggingAttackPatternArrowAngle = false;
                }
            }, 0.04f);
        } else if(isPanningCamera) {
            isPanningCamera = false;
        }
    }

    public void touchDragged(float x, float y) {
        if(disableTouch || isPinching) {
            resetInputs();
            return;
        }

        if(isDraggingNewCircle) {
            newCircleDragCurrentPoint.x = x;
            newCircleDragCurrentPoint.y = y;

            Vector3 originScreenCoordinates = playerRenderCamera.project(new Vector3(playerRenderOrigin.x, playerRenderOrigin.y, 0));
            // Snap to y-axis
            if(Math.abs(x - originScreenCoordinates.x) < CIRCLE_SNAP_DISTANCE) {
                newCircleDragCurrentPoint.x = originScreenCoordinates.x;
            }
            // Snap to x-axis
            if(Math.abs(y - originScreenCoordinates.y) < CIRCLE_SNAP_DISTANCE) {
                newCircleDragCurrentPoint.y = originScreenCoordinates.y;
            }
        } else if(isMovingCircle) {
            movingCircleDragCurrentPoint.x = x;
            movingCircleDragCurrentPoint.y = y;

            Vector3 originScreenCoordinates = playerRenderCamera.project(new Vector3(playerRenderOrigin.x, playerRenderOrigin.y, 0));
            // Snap to y-axis
            if(Math.abs(x + movingCircleDragOffset.x - originScreenCoordinates.x) < CIRCLE_SNAP_DISTANCE) {
                movingCircleDragCurrentPoint.x = originScreenCoordinates.x - movingCircleDragOffset.x;
            }
            // Snap to x-axis
            if(Math.abs(y + movingCircleDragOffset.y - originScreenCoordinates.y) < CIRCLE_SNAP_DISTANCE) {
                movingCircleDragCurrentPoint.y = originScreenCoordinates.y - movingCircleDragOffset.y;
            }
        } else if(isDraggingAttackPatternArrowAngle) {
            draggingAttackPatternArrowPos.x = x;
            draggingAttackPatternArrowPos.y = y;
            Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(x, -(y - screenHeight), 0));
            draggingAttackPatternArrowAngle = MathUtils.atan2(worldCoordinates.y - selectedCircle.y, worldCoordinates.x - selectedCircle.x) - PLAYER_RENDER_ANGLE;

            // Snap to nearest increment of 45 degrees
            float normalized = Utils.normalizeAngle(draggingAttackPatternArrowAngle);
            for(int i = 0; i < 8; i++) {
                if(Math.abs(normalized - (i * (MathUtils.PI/4f))) < ATTACK_PATTERN_ANGLE_SNAP_MARGIN) {
                    draggingAttackPatternArrowAngle = i * (MathUtils.PI/4f);
                    break;
                }
            }
        }
    }

    private void resetInputs() {
        if(isDraggingNewCircle) {
            isDraggingNewCircle = false;
            newCircleDragCurrentPoint.x = -1;
        }
        if(isMovingCircle) {
            isMovingCircle = false;
            movingCircleDragCurrentPoint.x = -1;
        }
        if(isPanningCamera) {
            isPanningCamera = false;
        }
    }

    public void tap(float x, float y) {
        if(disableTouch) {
            return;
        }
        // Disable touch near buttons
        if(y < TOP_PADDING + 70f && (x < LEFT_PADDING + 70f || (x > editableAreaRightXBound - LEFT_PADDING*2f - 140f && x < editableAreaRightXBound))) {
            return;
        }

        if(x < editableAreaRightXBound && !isDraggingAttackPatternArrowAngle && !isMovingCircle) {
            if (selectedCircle != null) {
                deselectCircle();
            } else {
                y = -(y - screenHeight);

                Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(x, y, 0));

                ArrayList<CircleHitbox> circlesAtPoint = getAllCirclesAtPoint(worldCoordinates);
                if (circlesAtPoint.size() == 0) {
                    deselectCircle();
                } else {
                    // Select smallest circle at that point
                    CircleHitbox smallestCircle = circlesAtPoint.get(0);
                    float smallestRadius = smallestCircle.radius;
                    for (CircleHitbox c : circlesAtPoint) {
                        if (c.radius < smallestRadius) {
                            smallestCircle = c;
                            smallestRadius = c.radius;
                        }
                    }
                    selectCircle(smallestCircle);
                }
            }
        }
    }

    /**
     * Camera zooming code copied off some guy
     */
    public boolean pinch(Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        isPinching = true;

        if(!(initialFirstPointer.equals(oldInitialFirstPointer)&&initialSecondPointer.equals(oldInitialSecondPointer))){
            oldInitialFirstPointer = initialFirstPointer.cpy();
            oldInitialSecondPointer = initialSecondPointer.cpy();
            oldScale = playerRenderCamera.zoom;
        }
        Vector3 center = new Vector3(
                (firstPointer.x+initialSecondPointer.x)/2,
                (firstPointer.y+initialSecondPointer.y)/2,
                0
        );
        zoomCamera(center, oldScale * initialFirstPointer.dst(initialSecondPointer) / firstPointer.dst(secondPointer));
        return true;
    }

    public void pinchStop() {
        isPinching = false;
    }

    private void zoomCamera(Vector3 origin, float scale) {
        playerRenderCamera.update();
        Vector3 oldUnprojection = playerRenderCamera.unproject(origin.cpy()).cpy();
        playerRenderCamera.zoom = scale;
        playerRenderCamera.zoom = Math.min(3f, Math.max(playerRenderCamera.zoom, 0.1f));
        playerRenderCamera.update();
        Vector3 newUnprojection = playerRenderCamera.unproject(origin.cpy()).cpy();
        playerRenderCamera.position.add(oldUnprojection.cpy().add(newUnprojection.cpy().scl(-1f)));
    }

    public void pan(float x, float y, float deltaX, float deltaY) {
        if(isPanningCamera) {
            // Prevent camera from going too far from player
            Vector3 editableAreaCenterWorldCoordinates = playerRenderCamera.unproject(new Vector3(editableAreaRightXBound/2f, screenHeight/2f, 0));
            if(!(editableAreaCenterWorldCoordinates.x - deltaX * playerRenderCamera.zoom < -Mappers.player.get(player).getCustomizationRadius() + 50f && deltaX > 0)
                    && !(editableAreaCenterWorldCoordinates.x - deltaX * playerRenderCamera.zoom > Mappers.player.get(player).getCustomizationRadius()  - 50f && deltaX < 0)) {
                playerRenderCamera.position.add(-deltaX * playerRenderCamera.zoom, 0, 0);
            }
            if(!(editableAreaCenterWorldCoordinates.y + deltaY * playerRenderCamera.zoom < -Mappers.player.get(player).getCustomizationRadius() + 50f && deltaY < 0)
                    && !(editableAreaCenterWorldCoordinates.y + deltaY * playerRenderCamera.zoom > Mappers.player.get(player).getCustomizationRadius() - 50f && deltaY > 0)) {
                playerRenderCamera.position.add(0, deltaY * playerRenderCamera.zoom, 0);
            }
        }
    }

    public ArrayList<CircleHitbox> getAllCirclesAtPoint(Vector3 worldCoordinates) {
        ArrayList<CircleHitbox> circlesAtPoint = new ArrayList<CircleHitbox>();
        for (CircleHitbox c : allCircles) {
            if (Utils.getDistance(worldCoordinates.x, worldCoordinates.y, c.x, c.y) <= c.radius) {
                circlesAtPoint.add(c);
            }
        }
        return circlesAtPoint;
    }

    public void selectCircle(CircleHitbox c) {
        selectedCircle = c;

        playerStatsTitleLabel.setText("CIRCLE STATS");
        playerStatsLabel.setVisible(false);
        circleStatsLabel.setVisible(true);
        circleDeleteButton.setVisible(true);
        circleUpgradeButton.setVisible(true);

        updateActors();
    }

    public void deselectCircle() {
        selectedCircle = null;

        playerStatsTitleLabel.setText("PLAYER STATS");
        playerStatsLabel.setVisible(true);
        circleStatsLabel.setVisible(false);
        circleDeleteButton.setVisible(false);
        circleUpgradeButton.setVisible(false);

        updateActors();
    }

    private float calculateUnsavedPp() {
        float pp = Mappers.player.get(player).getPixelPoints();

        pp += circleDeletionPp;

        for(CircleHitbox c : unsavedCircles) {
            // Cost of creating the circle (if it is new)
            pp -= c.getUnsavedCreationCost();

            // Cost of upgrades to circle and attack pattern
            pp -= c.getUnsavedUpgradeCost();
        }

        return pp;
    }

    public void placeNewCircle(float x, float y) {
        if(x > editableAreaRightXBound) {
            return;
        }

        Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(x, y, 0));
        if(Utils.getDistance(worldCoordinates.x, worldCoordinates.y, 0, 0) > Mappers.player.get(player).getCustomizationRadius() * 2f) {
            return;
        }

        CircleHitbox c = new CircleHitbox();
        c.setHitboxTextureType(RenderSystem.HitboxTextureType.PLAYER);
        c.radius = com.miv.Options.DEFAULT_NEW_CIRCLE_RADIUS;
        c.x = worldCoordinates.x;
        c.y = worldCoordinates.y;
        c.setAttackPattern(AttackPatternFactory.getAttackPattern("PLAYER_DEFAULT_1"));
        // Set original position to be at if circle was rotated to 0 degrees
        c.setOriginalPosX(c.x * MathUtils.cos(-PLAYER_RENDER_ANGLE) - c.y * MathUtils.sin(-PLAYER_RENDER_ANGLE));
        c.setOriginalPosY(c.x * MathUtils.sin(-PLAYER_RENDER_ANGLE) + c.y * MathUtils.cos(-PLAYER_RENDER_ANGLE));
        c.setMaxHealth(Options.DEFAULT_NEW_CIRCLE_MAX_HEALTH);
        c.setHealth(c.getMaxHealth());
        // Calculate cost of creating new circle
        c.setUnsavedCreationCost((float) Math.pow((playerRender.size() + unsavedCircles.size()), Options.CIRCLE_CREATION_EXPONENT) + map.Map.INITIAL_MAP_AREA_PIXEL_POINTS / 2f);
        unsavedCircles.add(c);
        allCircles.add(c);

        selectCircle(c);

        onCircleModification(c);
    }

    public void deleteCircle(CircleHitbox c) {
        playerRender.remove(c);
        unsavedCircles.remove(c);
        allCircles.remove(c);

        circleDeletionPp  += c.getTotalUpgradesPp() * Options.CIRCLE_DELETION_PP_RETURN_MULTIPLIER;

        onCircleModification2();
    }

    public void upgradeCircle(CircleHitbox c) {
        c.upgrade();

        onCircleModification(c);
    }

    private void showSpecializationChooser() {
        if(attackPatternInfo.isShowing) {
            attackPatternInfo.playHideAnimation();
        }
        specializationChooser.playShowAnimation();
    }

    public void upgradeCircleAttackPattern(CircleHitbox c) {
        c.upgradeAttackPattern();

        onCircleModification(c);
    }

    public void modifyCircleSpecialization(CircleHitbox c, CircleHitbox.Specialization newSpecialization) {
        c.changeSpecialization(newSpecialization);

        onCircleModification(c);
    }

    public void modifyCircleAttackPatternAngle(CircleHitbox c, float angle) {
        c.getAttackPattern().setAngleOffset(angle);

        onCircleModification(c);
    }

    public void modifyCircleMaxHealth(CircleHitbox c, float newMaxHealth) {
        if(newMaxHealth > c.getMaxHealth()) {
            float difference = c.getMaxHealth() - c.getHealth();
            c.setMaxHealth(newMaxHealth);
            c.setHealth(newMaxHealth - difference);
        } else {
            c.setMaxHealth(newMaxHealth);
        }
        onCircleModification(c);
    }

    public void modifyCircleRadius(CircleHitbox c, float newRadius) {
        c.radius = newRadius;
        onCircleModification(c);
    }

    public void modifyCircleAttackPattern(CircleHitbox c, AttackPattern ap) {
        c.setAttackPattern(ap);
        onCircleModification(c);
    }

    private void onCircleModification(CircleHitbox c) {
        if(!unsavedCircles.contains(c)) {
            unsavedCircles.add(c);
        }
        playerRender.remove(c);

        onCircleModification2();
    }

    private void onCircleModification2() {
        checkAllCirclesPositionsValidity();

        Utils.setAuraBuffsForAllCircles(allCircles);

        //TODO: save state

        unsavedChangesExist = true;
        updateActors();
    }

    /**
     * Checks if all circles are within editable bounds and do not overlap with each other.
     * If one of these conditions are not met, the circle is set to have an invalid position.
     * Otherwise, it is set to have a valid position.
     */
    private void checkAllCirclesPositionsValidity() {
        for(CircleHitbox c : allCircles) {
            checkCirclePositionValidity(c);
        }
    }

    /**
     * To be used only by {@link PlayerBuilder#checkAllCirclesPositionsValidity()}
     */
    private void checkCirclePositionValidity(CircleHitbox c) {
        boolean invalidPosition = false;

        // Check if overlapping with other circles
        for(CircleHitbox c2 : allCircles) {
            if(!c2.equals(c)) {
                if(Utils.getDistance(c.x, c.y, c2.x, c2.y) < c.radius + c2.radius - CIRCLE_OVERLAP_LENIENCY) {
                    c2.setInvalidPosition(true);
                    invalidPosition = true;
                }
            }
        }

        // Check bounds
        float angle = MathUtils.atan2(c.y, c.x);
        if(Utils.getDistance(c.x + c.radius*MathUtils.cos(angle), c.y + c.radius*MathUtils.sin(angle), 0, 0) > Mappers.player.get(player).getCustomizationRadius()) {
            invalidPosition = true;
        }

        c.setInvalidPosition(invalidPosition);
    }

    /**
     * @param x In screen units
     * @param y In screen units
     */
    public void moveCircle(CircleHitbox circle, float x, float y) {
        if(x > editableAreaRightXBound) {
            isMovingCircle = false;
            movingCircleDragCurrentPoint.x = -1;
            return;
        }

        Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(x, y, 0));
        if(Utils.getDistance(worldCoordinates.x, worldCoordinates.y, 0, 0) > Mappers.player.get(player).getCustomizationRadius() * 2f) {
            return;
        }

        circle.x = worldCoordinates.x;
        circle.y = worldCoordinates.y;
        // Set original position to be at if circle was rotated to 0 degrees
        circle.setOriginalPosX(circle.x * MathUtils.cos(-PLAYER_RENDER_ANGLE) - circle.y * MathUtils.sin(-PLAYER_RENDER_ANGLE));
        circle.setOriginalPosY(circle.x * MathUtils.sin(-PLAYER_RENDER_ANGLE) + circle.y * MathUtils.cos(-PLAYER_RENDER_ANGLE));

        onCircleModification(circle);
    }

    public void discardChanges() {
        if(!unsavedChangesExist) {
            return;
        }

        for(CircleHitbox c : unsavedCircles) {
            allCircles.remove(c);
        }
        unsavedCircles.clear();
        loadPlayerRenderFromPlayerEntity();
        deselectCircle();

        unsavedChangesExist = false;
        updateActors();
    }

    public void saveChanges() {
        if(!unsavedChangesExist) {
            return;
        }

        // Check if enough pp is available
        float unsavedPp = calculateUnsavedPp();
        if(unsavedPp < 0) {
            createPopup("Not enough pp!");
            return;
        } else if(getOutOfBoundsCirclesCount() == 1) {
            createPopup("A circle is out of bounds!");
        } else if(getOutOfBoundsCirclesCount() > 1) {
            createPopup("Some circles are out of bounds!");
        } else if(!allCirclesHaveValidPositions()) {
            createPopup("Some circles are overlapping!");
            return;
        }

        // Apply changes to pp
        Mappers.player.get(player).setPixelPoints(main, unsavedPp);

        // Move all unsaved circles into playerRender
        for (CircleHitbox c : unsavedCircles) {
            playerRender.add(c);
            c.setUnsavedCreationCost(0);
            c.setUnsavedUpgradeCost(0);

            if (c.getMaxHealth() < c.getHealth()) {
                c.setHealth(c.getMaxHealth());
            }
        }
        unsavedCircles.clear();
        deselectCircle();

        loadPlayerRenderIntoPlayerEntity();
        loadPlayerRenderFromPlayerEntity();

        Save.save(main);

        unsavedChangesExist = false;
        updateActors();
    }

    private boolean allCirclesHaveValidPositions() {
        for(CircleHitbox c : allCircles) {
            if(c.isInvalidPosition()) {
                return false;
            }
        }
        return true;
    }

    private int getOutOfBoundsCirclesCount() {
        int count = 0;
        for(CircleHitbox c : allCircles) {
            float angle = MathUtils.atan2(c.y, c.x);
            if(Utils.getDistance(c.x + c.radius*MathUtils.cos(angle), c.y + c.radius*MathUtils.sin(angle), 0, 0) > Mappers.player.get(player).getCustomizationRadius() + CIRCLE_OVERLAP_LENIENCY) {
                count++;
            }
        }
        return count;
    }

    public void loadPlayerRenderIntoPlayerEntity() {
        // Rotate all circles to face 0 (since all circles face up in player builder)
        for(int i = 0; i < playerRender.size(); i++) {
            CircleHitbox c = playerRender.get(i);
            c.setPosition(c.getOriginalPosX() * MathUtils.cos(0) - c.getOriginalPosY() * MathUtils.sin(0),
                    c.getOriginalPosX() * MathUtils.sin(0) + c.getOriginalPosY() * MathUtils.cos(0));
        }
        HitboxComponent hitbox = Mappers.hitbox.get(player);
        hitbox.clearCircles();
        for(CircleHitbox c : playerRender) {
            hitbox.addCircle(c, true);
        }

        hitbox.setLastFacedAngle(hitbox.getLastFacedAngle());
    }

    public void exitToHud() {
        if(unsavedChangesExist) {
            createConfirmationPopup("Exit without saving?", new Timer.Task() {
                @Override
                public void run() {
                    // Act 0.5s to speed past the dialog fade out
                    stage.act(0.5f);

                    Mappers.hitbox.get(player).recenterOriginalCirclePositions();
                    main.loadHUD();
                }
            });
        } else {
            Mappers.hitbox.get(player).recenterOriginalCirclePositions();
            main.loadHUD();
        }
    }

    /**
     * Creates popup with an ok button only
     */
    private void createPopup(String text) {
        disableNonPopupInputs();

        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                enableNonPopupInputs();
            }
        };
        dialog.setMovable(false);
        dialog.setSize(screenWidth * 1.5f / 3f, screenHeight / 3f);
        dialog.setPosition((screenWidth - dialog.getWidth()) / 2f, (screenHeight - dialog.getHeight()) / 2f);
        Label textLabel = new Label(text, skin);
        textLabel.setFontScale(BODY_LABEL_FONT_SCALE);
        textLabel.setColor(Color.BLACK);
        dialog.text(textLabel);
        TextButton button = new TextButton("Ok", skin, "small-with-big-font");
        button.getLabel().setFontScale(0.6f);
        dialog.button(button);
        stage.addActor(dialog);
    }

    /**
     * Creates popup with a cancel button and a confirm button
     * @param text
     * @param confirmationTask Task to be run if confirm button is clicked. Make sure to call {@link PlayerBuilder#enableNonPopupInputs()} in it.
     */
    private void createConfirmationPopup(String text, Timer.Task confirmationTask) {
        disableNonPopupInputs();

        final Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                Timer.Task task = (Timer.Task)object;
                enableNonPopupInputs();
                if(task != null) {
                    task.run();
                    remove();
                }
            }
        };
        dialog.setMovable(false);
        dialog.setSize(screenWidth * 1.5f / 3f, screenHeight / 3f);
        dialog.setPosition((screenWidth - dialog.getWidth()) / 2f, (screenHeight - dialog.getHeight()) / 2f);
        Label textLabel = new Label(text, skin);
        textLabel.setFontScale(BODY_LABEL_FONT_SCALE);
        textLabel.setColor(Color.BLACK);
        dialog.text(textLabel);
        TextButton button = new TextButton("No", skin, "small-with-big-font");
        button.getLabel().setFontScale(0.6f);
        dialog.button(button);
        TextButton button2 = new TextButton("Yes", skin, "small-with-big-font");
        button2.getLabel().setFontScale(0.6f);
        dialog.button(button2, confirmationTask);
        stage.addActor(dialog);
    }

    private void disableNonPopupInputs() {
        disableTouch = true;
        for(Actor a : stage.getActors()) {
            if(!a.equals(specializationChooser)) {
                a.setTouchable(Touchable.disabled);
            }
        }
    }

    private void enableNonPopupInputs() {
        disableTouch = false;
        for(Actor a : stage.getActors()) {
            a.setTouchable(Touchable.enabled);
        }
    }

    private void centerCameraOnPlayerRender() {
        playerRenderCamera.zoom = 1f;
        playerRenderCamera.position.set(0, 0, 0);
        playerRenderCamera.update();
        playerRenderCamera.position.set(-playerRenderCamera.unproject(new Vector3(editableAreaRightXBound / 2f, 0, 0)).x, 0, 0);
        playerRenderCamera.update();
    }

    @Override
    public void show() {
        //TODO remove this
        Mappers.player.get(player).setPixelPoints(main, 5000);
        inputMultiplexer.addProcessor(0, stage);

        disableTouch = false;
        unsavedChangesExist = false;
        isPanningCamera = false;
        isMovingCircle = false;
        isDraggingNewCircle = false;

        circleDeletionPp = 0;

        centerCameraOnPlayerRender();
        playerRenderOrigin.x = 0;
        playerRenderOrigin.y = 0;
        loadPlayerRenderFromPlayerEntity();
        deselectCircle();

        updateActors();
    }

    public void loadPlayerRenderFromPlayerEntity() {
        playerRender.clear();
        unsavedCircles.clear();
        allCircles.clear();

        for(CircleHitbox c : Mappers.hitbox.get(player).getCircles()) {
            CircleHitbox c2 = c.clone();
            playerRender.add(c2);
            allCircles.add(c2);
        }
        for(int i = 0; i < playerRender.size(); i++) {
            CircleHitbox c = playerRender.get(i);
            c.setPosition(c.getOriginalPosX() * MathUtils.cos(PLAYER_RENDER_ANGLE) - c.getOriginalPosY() * MathUtils.sin(PLAYER_RENDER_ANGLE),
                    c.getOriginalPosX() * MathUtils.sin(PLAYER_RENDER_ANGLE) + c.getOriginalPosY() * MathUtils.cos(PLAYER_RENDER_ANGLE));
        }
    }

    public void updateActors() {
        float pixelPoints;
        if(unsavedChangesExist) {
            pp.setColor(Color.RED);
            pixelPoints = calculateUnsavedPp();
        } else {
            pp.setColor(Color.BLACK);
            pixelPoints = Mappers.player.get(player).getPixelPoints();
        }
        pp.setText(formatNumber(pixelPoints) + "pp");

        if(selectedCircle == null) {
            playerStatsLabel.setText(getPlayerStringFormattedStats());
            attackPatternInfo.setBodyText("No circle selected");
            attackPatternInfo.upgrade.setVisible(false);
        } else {
            circleStatsLabel.setText(selectedCircle.getStringFormattedStats());
            attackPatternInfo.setBodyText(selectedCircle.getAttackPatternFormattedStats());
            attackPatternInfo.upgrade.setVisible(true);
        }

        if(selectedCircle != null && selectedCircle.isSpecializationAvailable()) {
            circleUpgradeButton.setText("Choose sp.");
            // TODO: change color of button
        } else {
            circleUpgradeButton.setText("Upgrade");
            // TODO: default color
        }

        boolean saveChangesDisabled = false;
        for(CircleHitbox c : allCircles) {
            if(c.isInvalidPosition()) {
                saveChangesDisabled = true;
            }
        }
        if(!unsavedChangesExist) {
            saveChangesDisabled = true;
            discardChangesButton.setDisabled(true);
        }
        saveChangesButton.setDisabled(saveChangesDisabled);
    }

    private String getPlayerStringFormattedStats() {
        float maxHealth = 0f;
        float health = 0f;
        float dps = 0f;
        int typeNone = 0;
        int typeDamage = 0;
        int typeHealth = 0;
        int typeUtility = 0;
        float lifestealPerSecond = 0;
        float maxSpeed = Options.PLAYER_BASE_MAX_SPEED;
        for(CircleHitbox c : allCircles) {
            if(c.getAttackPattern() != null) {
                dps += c.getAttackPattern().getDPS();
                lifestealPerSecond += c.getAttackPattern().getDPS() * c.getAttackPattern().getLifestealMultiplier();
            }
            CircleHitbox.Specialization sp = c.getSpecialization().getRootSpecialization();
            if(sp == CircleHitbox.Specialization.NONE) {
                typeNone++;
            } else if(sp == CircleHitbox.Specialization.DAMAGE) {
                typeDamage++;
            } else if(sp == CircleHitbox.Specialization.HEALTH) {
                typeHealth++;
            } else if(sp == CircleHitbox.Specialization.UTILITY) {
                typeUtility++;
            }
            maxHealth += c.getMaxHealth();
            health += c.getHealth();
            maxSpeed += c.getSpeedBoost();
        }

        String s = "Avg. DPS: " + formatNumber(dps) + "\n"
                + "Total max health: " + (int)Math.ceil(maxHealth) + "\n"
                + "Total health: " + (int)Math.ceil(health) + " (" + String.format("%.1f", (health/maxHealth) * 100f) + "%)\n"
                + "Circles count: " + (playerRender.size() + unsavedCircles.size()) + "\n"
                + "  No type: " + typeNone + "\n"
                + "  Damage type: " + typeDamage + "\n";
        s += "  Health sp.: " + typeHealth + "\n"
                + "  Utility sp.: " + typeUtility + "\n"
                + "Avg. lifesteal: " + formatNumber(lifestealPerSecond) + "/s\n"
                + "Max speed: " + formatNumber(maxSpeed) + "\n";
        //TODO: number of circles affected by each type of aura
        return s;
    }

    public static String formatNumber(float pp) {
        if(pp == 0) {
            return "0";
        } else if(Math.abs(pp) < 1) {
            String s = String.format("%.3f", pp);
            // Make sure string doesn't end in a 0 if there is a decimal point
            while((s.charAt(s.length() - 1) == '0' && s.contains(".")) || s.charAt(s.length() - 1) == '.') {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        } else if(Math.abs(pp) < 10) {
            String s = String.format("%.2f", pp);
            // Make sure string doesn't end in a 0 if there is a decimal point
            while((s.charAt(s.length() - 1) == '0' && s.contains(".")) || s.charAt(s.length() - 1) == '.') {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        } else if(Math.abs(pp) < 100) {
            String s = String.format("%.1f", pp);
            // Make sure string doesn't end in a 0 if there is a decimal point
            while((s.charAt(s.length() - 1) == '0' && s.contains(".")) || s.charAt(s.length() - 1) == '.') {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        } else {
            return String.valueOf(Math.round(pp));
        }
    }

    @Override
    public void render(float delta) {
        // Background color
        Gdx.gl.glClearColor(240 / 255f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        playerRenderCamera.update();

        playerRenderBatch.setProjectionMatrix(playerRenderCamera.combined);
        playerShapeRenderer.setProjectionMatrix(playerRenderCamera.combined);

        playerRenderBatch.begin();
        // Draw circles
        for(CircleHitbox c : allCircles) {
            int id;
            // Circle selection has priority for circle image
            if(selectedCircle != null && selectedCircle.equals(c)) {
                id = RenderSystem.HitboxTextureType.PLAYER_RENDER_SELECTED.getId();
            } else if(c.isInvalidPosition()) {
                id = RenderSystem.HitboxTextureType.PLAYER_RENDER_INVALID_POSITION.getId();
            } else {
                if(c.getColor() == null) {
                    id = c.getHitboxTextureType().getId();
                } else {
                    id = c.getColor().getId();
                }
            }
            if((selectedCircle != null && selectedCircle.equals(c) && !isMovingCircle) || !((selectedCircle != null && selectedCircle.equals(c)))) {
                main.getRenderSystem().bubbleDrawables[id].draw(playerRenderBatch, c.x + playerRenderOrigin.x - c.radius, c.y + playerRenderOrigin.y - c.radius, c.radius * 2, c.radius * 2);
            }
        }
        playerRenderBatch.end();

        Gdx.gl.glLineWidth(4f);
        playerShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Draw auras
        for(CircleHitbox c : allCircles) {
            if(((selectedCircle != null && selectedCircle.equals(c) && !isMovingCircle) || !((selectedCircle != null && selectedCircle.equals(c)))) && c.getSpecialization().hasAura()) {
                Color auraColor = null;
                if(c.getSpecialization().hasLifestealAura()) {
                    auraColor = LIFESTEAL_AURA_COLOR;
                } else if(c.getSpecialization().hasDamageTakenAura()) {
                    auraColor = DAMAGE_TAKEN_AURA_COLOR;
                } else if(c.getSpecialization().hasMaxHealthAura()) {
                    auraColor = MAX_HEALTH_AURA_COLOR;
                }
                playerShapeRenderer.setColor(auraColor);
                playerShapeRenderer.circle(c.x + playerRenderOrigin.x, c.y + playerRenderOrigin.y, c.radius + Options.CIRCLE_AURA_RANGE);
            }
        }
        playerShapeRenderer.end();

        // Draw player outline and center
        playerShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(CircleHitbox c : allCircles) {
            if((selectedCircle != null && selectedCircle.equals(c) && !isMovingCircle) || !((selectedCircle != null && selectedCircle.equals(c)))) {
                RenderSystem.HitboxTextureType hitboxTextureType = null;
                // Invalid position has priority for outline
                if(c.isInvalidPosition()) {
                    hitboxTextureType = RenderSystem.HitboxTextureType.PLAYER_RENDER_INVALID_POSITION;
                } else if(selectedCircle != null && selectedCircle.equals(c)) {
                    hitboxTextureType = RenderSystem.HitboxTextureType.PLAYER_RENDER_SELECTED;
                } else {
                    if(c.getColor() == null) {
                        hitboxTextureType = c.getHitboxTextureType();
                    } else {
                        hitboxTextureType = c.getColor();
                    }
                }

                playerShapeRenderer.setColor(hitboxTextureType.getOutlineColor());
                playerShapeRenderer.circle(c.x + playerRenderOrigin.x, c.y + playerRenderOrigin.y, c.radius);
            }
        }
        // Draw player editable radius
        playerShapeRenderer.set(ShapeRenderer.ShapeType.Line);
        playerShapeRenderer.setColor(Color.LIGHT_GRAY);
        playerShapeRenderer.circle(playerRenderOrigin.x, playerRenderOrigin.y, Mappers.player.get(player).getCustomizationRadius());
        playerShapeRenderer.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        // Background color of right half of screen
        staticShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        staticShapeRenderer.setColor(RIGHT_HALF_OF_SCREEN_COLOR);
        staticShapeRenderer.rect(editableAreaRightXBound, 0, screenWidth - editableAreaRightXBound, screenHeight);
        // Background color of new circle button
        staticShapeRenderer.rect(newBubbleButtonX, newBubbleButtonY, newBubbleButtonLength, newBubbleButtonLength);
        staticShapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.getBatch().begin();
        // Draw new circle button
        stage.getBatch().setColor(Color.WHITE);
        newBubbleDrawable.draw(stage.getBatch(), newBubbleButtonX + 10f, newBubbleButtonY + 10f, newBubbleButtonLength - 20f, newBubbleButtonLength - 20f);
        // Draw new circle being dragged
        if(isDraggingNewCircle) {
            int id;
            if(isInvalidPosition(newCircleDragCurrentPoint.x ,newCircleDragCurrentPoint.y, Options.DEFAULT_NEW_CIRCLE_RADIUS, null)) {
                id = RenderSystem.HitboxTextureType.PLAYER_RENDER_INVALID_POSITION.getId();
            } else {
                id = RenderSystem.HitboxTextureType.PLAYER.getId();
            }
            main.getRenderSystem().bubbleDrawables[id].draw(stage.getBatch(), newCircleDragCurrentPoint.x - selectedCircleRadiusInScreenUnits, newCircleDragCurrentPoint.y - selectedCircleRadiusInScreenUnits, selectedCircleRadiusInScreenUnits * 2, selectedCircleRadiusInScreenUnits * 2);
        }
        // Draw circle being moved
        else if(isMovingCircle) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            int id;
            if(isInvalidPosition(movingCircleDragCurrentPoint.x + movingCircleDragOffset.x, movingCircleDragCurrentPoint.y + movingCircleDragOffset.y, selectedCircle.radius, selectedCircle)) {
                id = RenderSystem.HitboxTextureType.PLAYER_RENDER_INVALID_POSITION.getId();
            } else {
                id = RenderSystem.HitboxTextureType.PLAYER_RENDER_SELECTED.getId();
            }
            // Draw aura, if any
            if(selectedCircle.getSpecialization().hasAura()) {
                stage.getBatch().end();
                playerShapeRenderer.begin(ShapeRenderer.ShapeType.Line);

                Color auraColor = null;
                if (selectedCircle.getSpecialization().hasLifestealAura()) {
                    auraColor = LIFESTEAL_AURA_COLOR;
                } else if (selectedCircle.getSpecialization().hasDamageTakenAura()) {
                    auraColor = DAMAGE_TAKEN_AURA_COLOR;
                } else if (selectedCircle.getSpecialization().hasMaxHealthAura()) {
                    auraColor = MAX_HEALTH_AURA_COLOR;
                }
                playerShapeRenderer.setColor(auraColor);
                Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(movingCircleDragCurrentPoint.x + movingCircleDragOffset.x, -(movingCircleDragCurrentPoint.y + movingCircleDragOffset.y - screenHeight), 0));
                playerShapeRenderer.circle(worldCoordinates.x, worldCoordinates.y, selectedCircle.radius + Options.CIRCLE_AURA_RANGE);

                playerShapeRenderer.end();
                stage.getBatch().begin();
            }

            // Draw circle
            main.getRenderSystem().bubbleDrawables[id].draw(stage.getBatch(), movingCircleDragCurrentPoint.x + movingCircleDragOffset.x - selectedCircleRadiusInScreenUnits,
                    movingCircleDragCurrentPoint.y + movingCircleDragOffset.y - selectedCircleRadiusInScreenUnits, selectedCircleRadiusInScreenUnits * 2, selectedCircleRadiusInScreenUnits * 2);

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        stage.getBatch().end();

        // Lines showing center of player customization circle
        playerShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        playerShapeRenderer.setColor(STATIC_SHAPE_RENDERER_GRID_LINE);
        float gridLineLength = Mappers.player.get(player).getCustomizationRadius() + 50f;
        playerShapeRenderer.rectLine(0, -gridLineLength, 0, gridLineLength, 0.5f);
        playerShapeRenderer.rectLine(-gridLineLength, 0, gridLineLength, 0, 0.5f);
        // Draw arrow showing circle is looking up
        playerShapeRenderer.rectLine(0, gridLineLength, -15f, gridLineLength - 15f, 0.5f);
        playerShapeRenderer.rectLine(0, gridLineLength, 15f, gridLineLength - 15f, 0.5f);
        // Draw arrow showing angle of selected circle's attack pattern aim
        if(selectedCircle != null && selectedCircle.getAttackPattern() != null && !isMovingCircle) {
            float angle = PLAYER_RENDER_ANGLE;
            if(isDraggingAttackPatternArrowAngle) {
                angle += draggingAttackPatternArrowAngle;
            } else {
                angle += selectedCircle.getAttackPattern().getAngleOffset();
            }
            float cos = MathUtils.cos(angle);
            float sin = MathUtils.sin(angle);
            playerShapeRenderer.setColor(selectedCircle.getSpecialization().getHitboxTextureType().getOutlineColor());
            playerShapeRenderer.rectLine(selectedCircle.x + playerRenderOrigin.x, selectedCircle.y + playerRenderOrigin.y,
                    selectedCircle.x + playerRenderOrigin.x + cos * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), selectedCircle.y + playerRenderOrigin.y + sin * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), 0.5f);
            playerShapeRenderer.rectLine(selectedCircle.x + playerRenderOrigin.x + cos * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), selectedCircle.y + playerRenderOrigin.y + sin * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH),
                    selectedCircle.x + playerRenderOrigin.x + cos * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH) + MathUtils.cos(angle + MathUtils.PI + MathUtils.PI / 4f) * 30f,
                    selectedCircle.y + playerRenderOrigin.y + sin * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH) + MathUtils.sin(angle + MathUtils.PI + MathUtils.PI / 4f)*30f, 0.5f);
            playerShapeRenderer.rectLine(selectedCircle.x + playerRenderOrigin.x + cos * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH), selectedCircle.y + playerRenderOrigin.y + sin * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH),
                    selectedCircle.x + playerRenderOrigin.x + cos * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH) + MathUtils.cos(angle + MathUtils.PI - MathUtils.PI / 4f) * 30f,
                    selectedCircle.y + playerRenderOrigin.y + sin * (selectedCircle.radius + ATTACK_PATTERN_ANGLE_ARROW_LENGTH) + MathUtils.sin(angle + MathUtils.PI - MathUtils.PI / 4f)*30f, 0.5f);
        }
        playerShapeRenderer.end();

        staticShapeRenderer.begin();
        staticShapeRenderer.setColor(STATIC_SHAPE_RENDERER_GRAY);
        // Vertical line separating editable area and non-editable area
        staticShapeRenderer.rectLine(editableAreaRightXBound, 0, editableAreaRightXBound, screenHeight, STATIC_SHAPE_RENDERER_LINE_WIDTH);
        // Bounds of new circle button
        staticShapeRenderer.setColor(STATIC_SHAPE_RENDERER_LIGHT_GRAY);
        staticShapeRenderer.rect(newBubbleButtonX, newBubbleButtonY, newBubbleButtonLength, newBubbleButtonLength);
        staticShapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    /**
     * Used for seeing if a circle is in a valid position as it is being dragged around
     */
    private boolean isInvalidPosition(float circleScreenCoordinatesX, float circleScreenCoordinatesY, float circleRadiusInWorldUnits, CircleHitbox circle) {
        Vector3 worldCoordinates = playerRenderCamera.unproject(new Vector3(circleScreenCoordinatesX, -(circleScreenCoordinatesY - screenHeight), 0));

        // Check if overlapping with other circles
        if(circle == null) {
            for (CircleHitbox c2 : allCircles) {
                if (Utils.getDistance(worldCoordinates.x, worldCoordinates.y, c2.x, c2.y) < circleRadiusInWorldUnits + c2.radius - CIRCLE_OVERLAP_LENIENCY) {
                    return true;
                }
            }
        } else {
            for (CircleHitbox c2 : allCircles) {
                if(!circle.equals(c2)) {
                    if (Utils.getDistance(worldCoordinates.x, worldCoordinates.y, c2.x, c2.y) < circleRadiusInWorldUnits + c2.radius - CIRCLE_OVERLAP_LENIENCY) {
                        return true;
                    }
                }
            }
        }

        // Check if out of bounds
        float angle = MathUtils.atan2(worldCoordinates.y, worldCoordinates.x);
        if(Utils.getDistance(worldCoordinates.x + circleRadiusInWorldUnits*MathUtils.cos(angle), worldCoordinates.y + circleRadiusInWorldUnits*MathUtils.sin(angle), 0, 0) > Mappers.player.get(player).getCustomizationRadius()) {
            return true;
        }

        return false;
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);

        playerRender.clear();
        unsavedCircles.clear();
        allCircles.clear();
    }

    @Override
    public void dispose() {

    }

    public void setPlayer(Entity player) {
        this.player = player;
    }
}
