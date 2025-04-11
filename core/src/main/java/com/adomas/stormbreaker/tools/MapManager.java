package com.adomas.stormbreaker.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Array<CollisionRectangle> collisionRectangles;

    public MapManager(String mapPath) {
        loadMap(mapPath);
    }

    public void loadMap(String mapPath) {
        // Load the Tiled map
        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map); // Initialize the renderer

        // Extract collision rectangles from the "CollisionLayer"
        collisionRectangles = new Array<>();
        for (MapObject object : map.getLayers().get("CollisionLayer").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                collisionRectangles.add(new CollisionRectangle(rect.x, rect.y, (int) rect.width, (int) rect.height));
            }
        }
    }

    public void render(OrthographicCamera camera) {
        mapRenderer.setView(camera); // Set the camera view
        mapRenderer.render(); // Render the map
    }

    public Array<CollisionRectangle> getCollisionRectangles() {
        return collisionRectangles;
    }

    public float getMapWidth() {
        return map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
    }

    public float getMapHeight() {
        return map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);
    }

    public void dispose() {
        if (map != null) {
            map.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
    }
}