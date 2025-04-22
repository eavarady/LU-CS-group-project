package com.adomas.stormbreaker.tools;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class AStarPathfinder {
    private final int gridWidth;
    private final int gridHeight;
    private final float cellSize;
    private final boolean[][] walkable;
    private final float buffer;

    public static class Node {
        public int x, y;
        public Node parent;
        public float g, h, f;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public AStarPathfinder(float mapWidth, float mapHeight, float cellSize, Array<CollisionRectangle> obstacles, float buffer) {
        this.gridWidth = (int) Math.ceil(mapWidth / cellSize);
        this.gridHeight = (int) Math.ceil(mapHeight / cellSize);
        this.cellSize = cellSize;
        this.buffer = buffer;
        this.walkable = new boolean[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                float worldX = x * cellSize + cellSize / 2f;
                float worldY = y * cellSize + cellSize / 2f;
                boolean blocked = false;
                for (CollisionRectangle rect : obstacles) {
                    // Inflate the rectangle by buffer
                    float rx = rect.getX() - buffer;
                    float ry = rect.getY() - buffer;
                    float rw = rect.getWidth() + 2 * buffer;
                    float rh = rect.getHeight() + 2 * buffer;
                    if (rx <= worldX && worldX <= rx + rw &&
                        ry <= worldY && worldY <= ry + rh) {
                        blocked = true;
                        break;
                    }
                }
                walkable[x][y] = !blocked;
            }
        }
    }

    // Keep the old constructor for backward compatibility
    public AStarPathfinder(float mapWidth, float mapHeight, float cellSize, Array<CollisionRectangle> obstacles) {
        this(mapWidth, mapHeight, cellSize, obstacles, 0f);
    }

    public Array<Vector2> findPath(Vector2 start, Vector2 end) {
        Node startNode = toNode(start);
        Node endNode = toNode(end);
        if (!isWalkable(endNode.x, endNode.y)) return null;
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        HashSet<Node> closed = new HashSet<>();
        startNode.g = 0;
        startNode.h = heuristic(startNode, endNode);
        startNode.f = startNode.h;
        open.add(startNode);
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.equals(endNode)) {
                return reconstructPath(current);
            }
            closed.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = current.x + dx;
                    int ny = current.y + dy;
                    if (!isWalkable(nx, ny)) continue;
                    Node neighbor = new Node(nx, ny);
                    if (closed.contains(neighbor)) continue;
                    float tentativeG = current.g + ((dx == 0 || dy == 0) ? 1f : 1.414f);
                    boolean inOpen = open.contains(neighbor);
                    if (!inOpen || tentativeG < neighbor.g) {
                        neighbor.parent = current;
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, endNode);
                        neighbor.f = neighbor.g + neighbor.h;
                        if (!inOpen) open.add(neighbor);
                    }
                }
            }
        }
        return null; // No path found
    }

    private Array<Vector2> reconstructPath(Node node) {
        Array<Vector2> path = new Array<>();
        while (node != null) {
            path.add(toWorld(node));
            node = node.parent;
        }
        path.reverse();
        return path;
    }

    private float heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean isWalkable(int x, int y) {
        return x >= 0 && y >= 0 && x < gridWidth && y < gridHeight && walkable[x][y];
    }

    private Node toNode(Vector2 pos) {
        int x = (int) (pos.x / cellSize);
        int y = (int) (pos.y / cellSize);
        return new Node(x, y);
    }

    private Vector2 toWorld(Node node) {
        return new Vector2(node.x * cellSize + cellSize / 2f, node.y * cellSize + cellSize / 2f);
    }
}
