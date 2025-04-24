package com.stormbreaker;

import java.util.Random;

public class DamageModel {
    public enum BodyPart {
        HEAD, TORSO, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
    }

    public static class HitResult {
        public final BodyPart part;
        public final float multiplier;
        public final float bleedChance;
        public HitResult(BodyPart part, float multiplier, float bleedChance) {
            this.part = part;
            this.multiplier = multiplier;
            this.bleedChance = bleedChance;
        }
    }

    private static final Random rand = new Random();

    public static HitResult getHitResult() {
        float r = rand.nextFloat();
        if (r < 0.10f) {
            // Head: 10%
            float mult = 2.5f + rand.nextFloat() * 0.5f; // 2.5–3.0
            return new HitResult(BodyPart.HEAD, mult, 0.30f);
        } else if (r < 0.50f) {
            // Torso: 40%
            return new HitResult(BodyPart.TORSO, 1.0f, 0.15f);
        } else if (r < 0.575f) {
            // Left Arm: 7.5%
            return new HitResult(BodyPart.LEFT_ARM, 0.75f, 0.20f);
        } else if (r < 0.65f) {
            // Right Arm: 7.5%
            return new HitResult(BodyPart.RIGHT_ARM, 0.75f, 0.20f);
        } else if (r < 0.825f) {
            // Left Leg: 17.5%
            return new HitResult(BodyPart.LEFT_LEG, 0.8f, 0.25f);
        } else {
            // Right Leg: 17.5%
            return new HitResult(BodyPart.RIGHT_LEG, 0.8f, 0.25f);
        }
    }
}