/*
 Abstract NPC class extending Character. To be extended by enemy and civilian NPC classes later.
 */

package com.adomas.stormbreaker;

public abstract class NPC extends Character {

    public NPC(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
    }

    // common behavior for all NPCs can be defined here



    // let subclasses define how they behave each frame
    @Override
    public abstract void update(float delta);
}