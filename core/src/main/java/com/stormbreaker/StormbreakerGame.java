package com.stormbreaker;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


public class StormbreakerGame extends Game {
    public Music menuMusic;
    public Array<LevelConfig> levelConfigs = new Array<>();

    @Override
    public void create() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("MusicMenuDef.mp3"));
        menuMusic.setLooping(true); 
        menuMusic.setVolume(1.0f); 

        levelConfigs.add(new LevelConfig(
            "maps/test_map.tmx",
            new Vector2(100, 100),
            new Array<>(new LevelConfig.EnemySpawn[]{
                new LevelConfig.EnemySpawn(400, 300, Enemy.EnemyType.PASSIVE),
                new LevelConfig.EnemySpawn(600, 400, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(800, 200, Enemy.EnemyType.PASSIVE),
                new LevelConfig.EnemySpawn(800, 600, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(100, 500, Enemy.EnemyType.BOMBER),
                new LevelConfig.EnemySpawn(200, 300, Enemy.EnemyType.BOMBER) 
            })
        ));

        levelConfigs.add(new LevelConfig(
            "maps/map_desert.tmx",
            new Vector2(100, 10),
            new Array<>(new LevelConfig.EnemySpawn[]{
                new LevelConfig.EnemySpawn(240, 32, Enemy.EnemyType.PASSIVE),
                new LevelConfig.EnemySpawn(288, 48, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(48, 272, Enemy.EnemyType.PASSIVE),
                new LevelConfig.EnemySpawn(304, 304, Enemy.EnemyType.BOMBER),
                new LevelConfig.EnemySpawn(416, 304, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(416, 240, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(560, 224, Enemy.EnemyType.BOMBER),
                new LevelConfig.EnemySpawn(464, 64, Enemy.EnemyType.AGGRESSIVE),
                new LevelConfig.EnemySpawn(608, 96, Enemy.EnemyType.PASSIVE)
             
            })
        ));
    

        levelConfigs.add(new LevelConfig(
            "maps/Map Stone House.tmx",
            new Vector2(100, 100),
            new Array<>(new LevelConfig.EnemySpawn[]{
                new LevelConfig.EnemySpawn(736, 192, Enemy.EnemyType.AGGRESSIVE), // (23,18)
                new LevelConfig.EnemySpawn(110, 224, Enemy.EnemyType.AGGRESSIVE), // (4,17)
                new LevelConfig.EnemySpawn(384, 320, Enemy.EnemyType.BOMBER), // (12,14)
                new LevelConfig.EnemySpawn(576, 256, Enemy.EnemyType.AGGRESSIVE), // (18,16)
                new LevelConfig.EnemySpawn(1184, 672, Enemy.EnemyType.AGGRESSIVE), // (37,3)
                new LevelConfig.EnemySpawn(1120, 448, Enemy.EnemyType.BOMBER), // (35,10)
                new LevelConfig.EnemySpawn(1088, 160, Enemy.EnemyType.AGGRESSIVE), // (34,19)
                new LevelConfig.EnemySpawn(928, 96, Enemy.EnemyType.AGGRESSIVE), // (29,22)
                new LevelConfig.EnemySpawn(800, 608, Enemy.EnemyType.AGGRESSIVE), // (25,5)
                new LevelConfig.EnemySpawn(96, 640, Enemy.EnemyType.PASSIVE), // (3,4)
                new LevelConfig.EnemySpawn(96, 576, Enemy.EnemyType.PASSIVE), // (3,6)
                new LevelConfig.EnemySpawn(192, 608, Enemy.EnemyType.PASSIVE), // (6,5)
                new LevelConfig.EnemySpawn(160, 640, Enemy.EnemyType.PASSIVE), // (5,4)
                new LevelConfig.EnemySpawn(224, 608, Enemy.EnemyType.PASSIVE), // (7,5)
                
            })
        ));
    

        setScreen(new SplashScreen(this));
    }

    public String[] levelPaths = {
        "maps/test_map.tmx",
        "maps/map_desert.tmx",
        "maps/Map Stone House.tmx"
    };
    public int currentLevelIndex = 0;
    
}
