package edu.cornell.gdiac.bubblegumbandit.models.level.gum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController;
import edu.cornell.gdiac.bubblegumbandit.helpers.Unstickable;
import edu.cornell.gdiac.bubblegumbandit.models.enemy.RollingEnemyModel;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;

import java.util.HashSet;

import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_GUM;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.CATEGORY_PROJECTILE;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_GUM;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_GUM_LIMIT;
import static edu.cornell.gdiac.bubblegumbandit.controllers.CollisionController.MASK_PROJECTILE;

/**
 * Class to represent a "stuck" Bubblegum. This Bubblegum
 * is not a projectile; instead, it is instantiated when
 * a gum projectile hits an Obstacle.
 */
public class GumModel extends WheelObstacle implements Unstickable {

    /**
     * Set of obstacles the gum is stuck to
     * */
    private ObjectSet<Obstacle> obstacles;

    /**
     * The maximum number of obstacles a gum can stick to
     * */
    private final int MAX_OBSTACLES = 3;

    private boolean onTile;

    private TextureRegion outline;

    private TextureRegion rollingOutline;

    private float outlineHeight;

    private Fixture sensorFixture;
    private CircleShape sensorShape;


    /**
     * Creates a Bubblegum projectile.
     * */
    public GumModel(float x, float y, float radius){
        super(x, y, radius);
        obstacles = new ObjectSet<>();
        onTile = false;
        outlineHeight = 48;
        sensorShape = new CircleShape();
        sensorShape.setRadius(radius * 1.5f);
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }

        setFilter(CATEGORY_GUM, MASK_GUM);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorDef.shape = sensorShape;
        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(this);

        Filter filter = sensorFixture.getFilterData();
        filter.maskBits = 0;
        sensorFixture.setFilterData(filter);

        return true;
    }


    /**
     * Checks if obstacle is not already stuck to gum and gum has not reached max stuck obstacles
     *
     * @param o Obstacle to add to obstacles
     * */
    public boolean canAddObstacle(Obstacle o){
        return obstacles.size < MAX_OBSTACLES && !obstacles.contains(o);
    }

    /**
     * Adds an obstacle to the obstacle set
     *
     * @param o Obstacle added to the obstacle set
     * */
    public void addObstacle(Obstacle o){
        obstacles.add(o);
    }

    public boolean getOnTile() {return onTile;}

    public void setOnTile(boolean value) {onTile = value;}

    /**
     * Checks if gum is at obstacle capacity and sets collision filter
     * to stop collisions between gum and enemies
     * */
    public void setCollisionFilters(){
        if (obstacles.size == MAX_OBSTACLES){
            setFilter(CATEGORY_GUM, MASK_GUM_LIMIT);
        }
    }

    /** Sets the outline used for this gum model. */
    public void setOutline(TextureRegion t) {
        outline = t;
        rollingOutline = new TextureRegion(t);
    }

    public void drawWithOutline(GameCanvas canvas) {
        for (Obstacle ob : obstacles) {
            if (ob instanceof RollingEnemyModel && ((RollingEnemyModel) ob).isUnsticking()) {
                rollingOutline.setRegionHeight((int) (((RollingEnemyModel) ob).getUnstickingFraction() * 38 + 10));
                canvas.draw(rollingOutline, Color.WHITE,origin.x,origin.y,getX()*drawScale.x- 5,getY()*drawScale.x-5,getAngle(),1,1);
                return;
            }
        }
        if (outline != null) {
            canvas.draw(outline, Color.WHITE,origin.x,origin.y,getX()*drawScale.x-5,getY()*drawScale.x-5,getAngle(),1,1);
        } else {
            super.draw(canvas);
        }

    }
}