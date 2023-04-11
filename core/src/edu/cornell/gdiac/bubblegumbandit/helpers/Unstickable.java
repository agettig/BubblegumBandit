package edu.cornell.gdiac.bubblegumbandit.helpers;

import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;

public interface Unstickable {

    // TODO: Just have a sprite with the desired outline instead of scaling up a white version.
    // This would make the outline look better.
    float OUTLINE_SIZE = 1.15f;
    void drawWithOutline(GameCanvas canvas);
}
