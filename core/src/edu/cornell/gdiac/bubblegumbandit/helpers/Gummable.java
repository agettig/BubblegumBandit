package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.bubblegumbandit.models.level.TileModel;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

/** This is an interface that lets anything be gummable. All of these methods are implemented in Obstacle except
 * setGummedTexture(), but Obstacle itself isn't Gummable. So to make something Gummable, all you have to
 * do is add "implements Gummable" and initialize collidedObs. */
public interface Gummable extends Unstickable {

    public void startCollision(Obstacle ob);

    public void endCollision(Obstacle ob);

    public ObjectSet<Obstacle> getCollisions();

    public void clearCollisions();

    public void setGummed(boolean value);

    public boolean getGummed();

    public void setStuck(boolean value);

    public boolean getStuck();
}
