package com.codenjoy.bomberman.domain;

import java.util.LinkedList;
import java.util.List;

import com.codenjoy.bomberman.utils.LengthToXY;

/**
 * User: oleksandr.baglai
 */
public class Board {
    private String board;
    private LengthToXY xyl;
    private int size;

    public Board(String boardString) {
        board = boardString.replaceAll("\n", "");
        size = boardSize();
        xyl = new LengthToXY(size);
    }

    public Point getBomberman() {
        List<Point> result = new LinkedList<Point>();
        result.addAll(findAll(Element.BOMBERMAN));
        result.addAll(findAll(Element.BOMB_BOMBERMAN));
        result.addAll(findAll(Element.DEAD_BOMBERMAN));
        return result.get(0);
    }

    public List<Point> getOtherBombermans() {
        List<Point> result = new LinkedList<Point>();
        result.addAll(findAll(Element.OTHER_BOMBERMAN));
        result.addAll(findAll(Element.OTHER_BOMB_BOMBERMAN));
        result.addAll(findAll(Element.OTHER_DEAD_BOMBERMAN));
        return result;
    }

    public boolean isMyBombermanDead() {
        return board.indexOf(Element.DEAD_BOMBERMAN.getChar()) != -1;
    }

    public boolean isAt(int x, int y, char ch) {
    	return isAt(x,y,Element.valueOf(ch));
    }
    
    public boolean isAt(int x, int y, Element element) {
        if (pt(x, y).isBad(size)) {
            return false;
        }
        return board.charAt(xyl.getLength(x, y)) == element.getChar();
    }

    public int boardSize() {
        return (int) Math.sqrt(board.length());
    }

    public List<Point> getBarriers() {
        List<Point> all = getMeatChoppers();
        all.addAll(getWalls());
        all.addAll(getBombs());
        all.addAll(getDestroyWalls());
        all.addAll(getOtherBombermans());

        return removeDuplicates(all);
    }

    private List<Point> removeDuplicates(List<Point> all) {
        List<Point> result = new LinkedList<Point>();
        for (Point point : all) {
            if (!result.contains(point)) {
                result.add(point);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("Board:\n%s\n" +
            "Bomberman at: %s\n" +
            "Other bombermans at: %s\n" +
            "Meat choppers at: %s\n" +
            "Destroy walls at: %s\n" +
            "Bombs at: %s\n" +
            "Blasts: %s\n" +
            "Expected blasts at: %s",
                boardAsString(),
                getBomberman(),
                getOtherBombermans(),
                getMeatChoppers(),
                getDestroyWalls(),
                getBombs(),
                getBlasts(),
                getFutureBlasts());
    }

    public List<Point> getMeatChoppers() {
        return findAll(Element.MEAT_CHOPPER);
    }

    private List<Point> findAll(Element element) {
        List<Point> result = new LinkedList<Point>();
        for (int i = 0; i < size*size; i++) {
            Point pt = xyl.getXY(i);
            if (isAt(pt.x, pt.y, element)) {
                result.add(pt);
            }
        }
        return result;
    }

    public List<Point> getWalls() {
        return findAll(Element.WALL);
    }

    public List<Point> getDestroyWalls() {
        return findAll(Element.DESTROY_WALL);
    }

    public List<Point> getBombs() {
        List<Point> result = new LinkedList<Point>();
        result.addAll(findAll(Element.BOMB_TIMER_1));
        result.addAll(findAll(Element.BOMB_TIMER_2));
        result.addAll(findAll(Element.BOMB_TIMER_3));
        result.addAll(findAll(Element.BOMB_TIMER_4));
        result.addAll(findAll(Element.BOMB_TIMER_5));
        result.addAll(findAll(Element.BOMB_BOMBERMAN));
        return result;
    }

    public List<Point> getBlasts() {
        return findAll(Element.BOOM);
    }

    public List<Point> getFutureBlasts() {
        List<Point> result = new LinkedList<Point>();
        List<Point> bombs = getBombs();
        bombs.addAll(findAll(Element.OTHER_BOMB_BOMBERMAN));
        bombs.addAll(findAll(Element.BOMB_BOMBERMAN));

        for (Point bomb : bombs) {
            result.add(bomb);
            result.add(new Point(bomb.x - 1, bomb.y));
            result.add(new Point(bomb.x + 1, bomb.y));
            result.add(new Point(bomb.x    , bomb.y - 1));
            result.add(new Point(bomb.x    , bomb.y + 1));
        }
        for (Point blast : result.toArray(new Point[0])) {
            if (blast.isBad(size) || getWalls().contains(blast)) {
                result.remove(blast);
            }
        }
        return removeDuplicates(result);
    }

    public boolean isAt(Point pt, Element element) {
        return isAt(pt.x, pt.y, element);
    }

    public boolean isAt(int x, int y, Element... elements) {
        for (Element c : elements) {
            if (isAt(x, y, c)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNear(int x, int y, char ch) {
    	return isNear(x,y,Element.valueOf(ch));
    }
    
    public boolean isNear(int x, int y, Element element) {
        if (pt(x, y).isBad(size)) {
            return false;
        }
        return isAt(x + 1, y, element) || isAt(x - 1, y, element) || isAt(x, y + 1, element) || isAt(x, y - 1, element);
    }

    public boolean isNear(Point pt, Element element) {
        return isNear(pt.x, pt.y, element);
    }

    public boolean isBarrierAt(int x, int y) {
        return getBarriers().contains(pt(x, y));
    }

    public int countNear(Point pt, Element element) {
        if (pt.isBad(size)) {
            return 0;
        }
        int count = 0;
        if (isAt(pt.x - 1, pt.y - 1, element)) count ++;
        if (isAt(pt.x - 1, pt.y    , element)) count ++;
        if (isAt(pt.x - 1, pt.y + 1, element)) count ++;

        if (isAt(pt.x    , pt.y - 1, element)) count ++;
        if (isAt(pt.x    , pt.y + 1, element)) count ++;

        if (isAt(pt.x + 1, pt.y - 1, element)) count ++;
        if (isAt(pt.x + 1, pt.y   , element)) count ++;
        if (isAt(pt.x + 1, pt.y + 1, element)) count ++;
        return count;
    }
    
    private Point pt(int x, int y) {
        return new Point(x, y);
    }

    public boolean isAt(int x, int y, String chars) {
        for (char c : chars.toCharArray()) {
            if (isAt(x, y, c)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDead() {
        return board.indexOf(Element.DEAD_BOMBERMAN.toString()) != -1;
    }

    public String fix() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= size - 1; i++) {
            buffer.append(board.substring(i*size, (i + 1)*size));
            buffer.append("\n");
        }
        return buffer.toString();
    }
    
    private String boardAsString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i <= size - 1; i++) {
            result.append(board.substring(i * size, (i + 1) * size));
            result.append("\n");
        }
        return result.toString();
    }
}