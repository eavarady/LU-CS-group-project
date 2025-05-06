package com.stormbreaker;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LevelConfig {
    public String mapPath;
    public Vector2 playerSpawn;
    public Array<EnemySpawn> enemySpawns;
    public EnemySpawn[] enemies;

    public LevelConfig(String mapPath, Vector2 playerSpawn, Array<EnemySpawn> enemySpawns) {
        this.mapPath = mapPath;
        this.playerSpawn = playerSpawn;
        this.enemySpawns = enemySpawns;
    }

    public static class EnemySpawn {
        public Vector2 position;
        public Enemy.EnemyType type;

        public EnemySpawn(float x, float y, Enemy.EnemyType type) {
            this.position = new Vector2(x, y);
            this.type = type;
        }
    }
}
