package edu.cornell.gdiac.json;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import com.badlogic.gdx.utils.Disposable;


/** Fields of vision for enemies */
public class Vision {

    /** The color of the FOV graphic. */
    private Color color;

    /** The color of the rays of the FOV during debug mode. */
    private Color DEBUGCOLOR = Color.RED;

    /** The direction of the FOV. In LibGdx, 0 is up. */
    private float direction;
    /** The range in radians of the FOV */
    private float range;
    /** The length of the FOV. */
    private float radius;
    /** The number of rays being cast */
    private int numRays = 10;

    /** End points of all casted rays */
    private Array<Vector2> rays = new Array<>(numRays);
    private Array<Body> bodies = new Array<>();


    public Vision(float radius, float direction, float range, Color color) {
        this.color = new Color(color.r, color.g, color.b, .5f);
        this.radius = radius;
        this.direction = direction;
        this.range = range;
        for(int i = 0; i<numRays; i++) this.rays.add(new Vector2());
    }

    /** Creates a sphere of vision */
    public Vision(float radius, Color color, Obstacle origin) {
        this.color = new Color(color.r, color.g, color.b, .5f);
        this.radius = radius;
        this.direction = direction;
        this.range = range;
        for(int i = 0; i<numRays; i++) this.rays.add(new Vector2());
    }

    public void update(World world, Vector2 origin) {
        float startAngle = (direction - range / 2) % (float) Math.PI;
        for (int i = 0; i < numRays; i++) {
            float angle = startAngle + i * (range / (numRays )) % (float) Math.PI;
            final int finalI = i;
            Vector2 end = new Vector2(origin.x + radius * (float) Math.cos(angle),
                origin.y + radius * (float) Math.sin(angle));
            rays.get(finalI).set(end);
            RayCastCallback ray = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point,
                                              Vector2 normal, float fraction) {
                    rays.get(finalI).set(point);
                    return -1f;
                }
            };
            world.rayCast(ray, origin, end);

        }
    }


    /** modifies direction but maintains range */
    public void setDirection(float direction) {
        this.direction = direction;
    }


    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, float x, float y, float scalex, float scaley) {

        canvas.drawFOV(color, rays, x, y, radius, scalex, scaley);
//            canvas.drawRays(color, rays, x, y, radius, scalex, scaley);
    }

    /**
     * Draws the outline of the physics object.
     */
    public void drawDebug(GameCanvas canvas, float x, float y, float scalex, float scaley) {

//        canvas.drawFOV(color, rays, x, y, radius, scalex, scaley);
        canvas.drawRays(DEBUGCOLOR, rays, x, y, radius, scalex, scaley);
    }








}
