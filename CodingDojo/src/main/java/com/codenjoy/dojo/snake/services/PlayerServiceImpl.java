package com.codenjoy.dojo.snake.services;

import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.snake.console.SnakePrinterImpl;
import com.codenjoy.dojo.snake.model.*;
import com.codenjoy.dojo.snake.model.artifacts.ArtifactGenerator;
import com.codenjoy.dojo.snake.model.artifacts.BasicWalls;
import com.codenjoy.dojo.snake.model.middle.SnakeEvented;
import com.codenjoy.dojo.services.playerdata.PlayerData;
import com.codenjoy.dojo.snake.services.playerdata.SnakePlotsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: oleksandr.baglai
 * Date: 10/1/12
 * Time: 6:48 AM
 */
@Component("playerService")
public class PlayerServiceImpl implements PlayerService {
    private static Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);
    public static final int BOARD_SIZE = 15;

    @Autowired
    private ScreenSender screenSender;

    @Autowired
    private PlayerController playerController;

    @Autowired
    private ArtifactGenerator artifactGenerator;

    private List<Player> players = new ArrayList<Player>();
    private List<Board> boards = new ArrayList<Board>();

    private ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @Override
    public Player addNewPlayer(final String name, final String callbackUrl) {
        lock.writeLock().lock();
        try {
            int minScore = getPlayersMinScore();
            final PlayerScores playerScores = new SnakePlayerScores(minScore);
            final InformationCollector informationCollector = new InformationCollector(playerScores);

            Board board = newBoard(informationCollector);

            Player player = new Player(name, callbackUrl, playerScores, informationCollector);
            players.add(player);
            boards.add(board);
            return player;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Board newBoard(final InformationCollector informationCollector) {
        return new BoardImpl(artifactGenerator, new SnakeFactory() {
                    @Override
                    public Snake create(int x, int y) {
                        return new SnakeEvented(informationCollector, x, y);
                    }
                }, new BasicWalls(BOARD_SIZE), BOARD_SIZE);
    }

    private int getPlayersMinScore() {
        int result = 0;
        for (Player player : players) {
            result = Math.min(player.getScore(), result);
        }
        return result;
    }

    @Override
    public void nextStepForAllGames() {
        lock.writeLock().lock();
        try {
            for (Board board : boards) {
                if (board.isGameOver()) {
                    board.newGame();
                }
                board.tact();
                //logBoardState(board);
            }

            HashMap<Player, PlayerData> map = new HashMap<Player, PlayerData>();
            HashMap<Player, List<Plot>> droppedPlotsMap = new HashMap<Player, List<Plot>>();
            for (int i = 0; i < boards.size(); i++) {
                Board board = boards.get(i);
                Snake snake = board.getSnake();
                List<Plot> plots = new ArrayList<Plot>();
                plots.addAll(new SnakePlotsBuilder(board).get());

                Player player = players.get(i);

                map.put(player, new PlayerData(board.getSize(),
                        plots,
                        player.getScore(),
                        board.getMaxLength(),
                        snake.getLength(),
                        player.getCurrentLevel() + 1,
                        player.getMessage()));
            }

            screenSender.sendUpdates(map);

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                Board board = boards.get(i);
                try {
                    playerController.requestControl(player, board.getSnake(), board.toString());
                } catch (IOException e) {
                    logger.error("Unable to send control request to player " + player.getName() +
                            " URL: " + player.getCallbackUrl(), e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void logBoardState(Board board) {
        if (logger.isDebugEnabled()) {
            logger.debug("Board after tact:\n" + new SnakePrinterImpl().print(board));
        }
    }

    @Override
    public List<Player> getPlayers() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(players);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean alreadyRegistered(String playerName) {
        lock.readLock().lock();
        try {
            return findPlayer(playerName) != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Player findPlayer(String playerName) {
        lock.readLock().lock();
        try {
            for (Player player : players) {
                if (player.getName().equals(playerName)) {
                    return player;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updatePlayer(Player player) {
        lock.writeLock().lock();
        try {
            for (Player playerToUpdate : players) {
                if (playerToUpdate.getName().equals(player.getName())) {
                    playerToUpdate.setCallbackUrl(player.getCallbackUrl());
                    return;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeAll() {
        lock.writeLock().lock();
        try {
            players.clear();
            boards.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    List<Board> getBoards() {
        return boards;
    }

    @Override
    public Player findPlayerByIp(String ip) {
        lock.readLock().lock();
        try {
            for (Player player : players) {
                if (player.getCallbackUrl().contains(ip)) {
                    return player;
                }
            }
            return new NullPlayer();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void removePlayer(String ip) {
        lock.writeLock().lock();
        try {
            int index = players.indexOf(findPlayerByIp(ip));
            if (index < 0) return;
            players.remove(index);
            boards.remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }
}