package com.codenjoy.dojo.loderunner.model;

import com.codenjoy.dojo.loderunner.services.LoderunnerEvents;
import com.codenjoy.dojo.services.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import static com.codenjoy.dojo.services.PointImpl.pt;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * User: sanja
 * Date: 17.12.13
 * Time: 4:47
 */
public class LoderunnerTest {

    private Loderunner game;
    private Hero hero;
    private Dice dice;
    private EventListener listener;
    private Player player;
    private EnemyAI ai;
    private Joystick enemy = new EnemyJoystick();

    @Before
    public void setup() {
        dice = mock(Dice.class);
        ai = mock(EnemyAI.class);
    }

    private void dice(int...ints) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int i : ints) {
            when = when.thenReturn(i);
        }
    }

    private void givenFl(String board) {
        LevelImpl level = new LevelImpl(board);
        level.setAI(ai);

        Hero hero = null;
        if (level.getHeroes().isEmpty()) {
            // Если героя на карте нет его надо замокать, чтобы все работало в движке
            hero = new Hero(pt(-1, -1), Direction.DOWN);
        } else {
            hero = level.getHeroes().get(0);
        }

        game = new Loderunner(level, dice);   // Ужас! :)
        listener = mock(EventListener.class);
        player = new Player(listener);
        game.newGame(player);
        player.hero = hero;
        hero.init(game);
        this.hero = game.getHeroes().get(0);
    }

    private void assertE(String expected) {
        assertE(new Printer(game.getSize(), new LoderunnerPrinter(game, player)), expected);
    }

    public static void assertE(Object printer, String expected) {
        int size = (int) Math.sqrt(expected.length());
        String result = inject(expected, size, "\n");
        assertEquals(result, printer.toString());
    }

    @Test
    public void testInject() {
        assertEquals("1234^*5678^*90AB^*CDEF^*HIJK^*LMNO^*PQRS^*TUVW^*XYZ", inject("1234567890ABCDEFHIJKLMNOPQRSTUVWXYZ", 4, "^*"));
        assertEquals("1234^*5678^*90AB^*CDEF^*HIJK^*LMNO^*PQRS^*TUVW^*", inject("1234567890ABCDEFHIJKLMNOPQRSTUVW", 4, "^*"));
        assertEquals("1234^*5678^*90AB^*CDEF^*HIJK^*LMNO^*PQRS^*TUV", inject("1234567890ABCDEFHIJKLMNOPQRSTUV", 4, "^*"));
    }

    public static String inject(String string, int position, String substring) {
        StringBuilder result = new StringBuilder();
        for (int index = 1; index < string.length() / position + 1; index++) {
            result.append(string.substring((index - 1)*position, index*position)).append(substring);
        }
        result.append(string.substring((string.length() / position) * position, string.length()));
        return result.toString();
    }

    private class EnemyJoystick implements Joystick {
        @Override
        public void down() {
            ai(Direction.DOWN);
        }

        @Override
        public void up() {
            ai(Direction.UP);
        }

        @Override
        public void left() {
            ai(Direction.LEFT);
        }

        @Override
        public void right() {
            ai(Direction.RIGHT);
        }

        @Override
        public void act(int... p) {
            ai(null);
        }
    }

    // есть карта со мной
    @Test
    public void shouldFieldAtStart() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // я могу сверлить дырки
    @Test
    public void shouldDrillLeft() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼.Я ☼" +
                "☼*##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldDrillRight() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ R.☼" +
                "☼##*☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ R ☼" +
                "☼## ☼" +
                "☼☼☼☼☼");
    }

    // я могу ходить влево и вправо
    @Test
    public void shouldMoveLeft() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldMoveRight() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  ►☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // если небыло команды я никуда не иду
    @Test
    public void shouldStopWhenNoMoreRightCommand() {
        shouldMoveRight();

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  ►☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // я останавливаюсь возле границы
    @Test
    public void shouldStopWhenWallRight() {
        shouldMoveRight();

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  ►☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldStopWhenWallLeft() {
        shouldMoveLeft();

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // яма заростает со временем
    @Test
    public void shouldDrillFilled() {
        shouldDrillLeft();

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼4##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼3##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼2##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼1##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // В просверленную яму я легко могу упасть
    @Test
    public void shouldFallInPitLeft() {
        shouldDrillLeft();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼]  ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼◄##☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldFallInPitRight() {
        shouldDrillRight();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ R ☼" +
                "☼## ☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  [☼" +
                "☼## ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼##►☼" +
                "☼☼☼☼☼");
    }

    // я если упал то не могу передвигаться влево и вправо поскольку мне мешают стены
    @Test
    public void shouldCantGoLeftIfWall() {
        shouldFallInPitRight();

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼##◄☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldCantGoRightIfWall() {
        shouldFallInPitLeft();

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼►##☼" +
                "☼☼☼☼☼");
    }

    // я если упал, то могу перемещаться влево и вправо, если мне не мешают стены
    @Test
    public void shouldСanGoInPitIfNoWall() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼.Я ☼" +
                "☼*##☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  ►☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");

        hero.left();
        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ .Я☼" +
                "☼ *#☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ] ☼" +
                "☼  #☼" +
                "☼☼☼☼☼");

        hero.left();  // при падении я не могу передвигаться
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ◄#☼" +
                "☼☼☼☼☼");

        hero.left(); // а вот в яме куда угодно, пока есть место
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼◄ #☼" +
                "☼☼☼☼☼");

        hero.right(); // пока стенки не заростут
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ►#☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼4►#☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼◄ #☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼2►#☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼◄3#☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼#►#☼" +
                "☼☼☼☼☼");

    }

    // если стенка замуровывается вместе со мной, то я умираю и появляюсь в рендомном месте
    @Test
    public void shouldIDieIPitFillWithMe() {
        shouldDrillLeft();

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼2##☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼]  ☼" +
                "☼1##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼Ѡ##☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(2, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼ [ ☼" +
                "☼   ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldIDieIPitFillWithMe2() {
        shouldDrillLeft();

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼3##☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼]  ☼" +
                "☼2##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼◄##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼Ѡ##☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(2, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼ [ ☼" +
                "☼   ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

    }

    // я не могу сверлить уже отсверленную дырку, я даже не делаю вид что пытался
    @Test
    public void shouldCantDrillPit() {
        shouldDrillLeft();

        hero.right();
        game.tick();
        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");

        hero.left();
        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");
    }

    // выполнения команд left + act не зависят от порядка - если они сделаны в одном тике, то будет дырка слева без перемещения
    @Test
    public void shouldDrillLeft_otherCommandSequence() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

//        hero.act();
        hero.left();
        hero.act();

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼.Я ☼" +
                "☼*##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");
    }

    // если я повернут в какую-то сторону и просто нажимаю сверлить то будет с той стороны дырка
    @Test
    public void shouldDrillLeft_onyActCommand() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼.Я ☼" +
                "☼*##☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Я ☼" +
                "☼ ##☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldDrillRight_onyActCommand() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ► ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ R.☼" +
                "☼##*☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ R ☼" +
                "☼## ☼" +
                "☼☼☼☼☼");
    }

    // на карте появляется золото, если я его беру то получаю +
    @Test
    public void shouldGoldOnMap_iCanGetIt() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►$☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        dice(2, 3);
        hero.right();
        game.tick();
        verify(listener).event(LoderunnerEvents.GET_GOLD);

        assertE("☼☼☼☼☼" +
                "☼ $ ☼" +
                "☼  ►☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼ $ ☼" +
                "☼ ◄ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // проверить, что если новому обекту не где появится то программа не зависает - там бесконечный цикл потенциальный есть
    @Test(timeout = 1000)
    public void shouldNoDeadLoopWhenNewObjectCreation() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►$☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        dice(4, 4);
        hero.right();
        game.tick();
        verify(listener).event(LoderunnerEvents.GET_GOLD);

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  ►☼" +
                "☼###☼" +
                "$☼☼☼☼");
    }

    // на карте появляются лестницы, я могу зайти на нее и выйти обратно
    @Test
    public void shouldICanGoOnLadder() {
        givenFl("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ ►H☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ ◄H☼" +
                "☼☼☼☼☼");
    }

    // я могу карабкаться по лестнице вверх
    @Test
    public void shouldICanGoOnLadderUp() {
        givenFl("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ ►H☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");
    }

    // я не могу вылезти с лестницей за границы
    @Test
    public void shouldICantGoOnBarrierFromLadder() {
        shouldICanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");
    }

    // я могу спустится вниз, но не дальше границы экрана
    @Test
    public void shouldICanGoOnLadderDown() {
        shouldICanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Y☼" +
                "☼☼☼☼☼");
    }

    // я мошгу в любой момент спрыгнуть с лестницы и я буду падать до тех пор пока не наткнусь на препятствие
    @Test
    public void shouldICanFly() {
        shouldICanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Y☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼ ]H☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼ ]H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ ◄H☼" +
                "☼☼☼☼☼");
    }

    // под стеной я не могу сверлить
    @Test
    public void shouldICantDrillUnderLadder() {
        givenFl("☼☼☼☼☼" +
                "☼  H☼" +
                "☼ ►H☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼ ►H☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // под золотом я не могу сверлить
    @Test
    public void shouldICantDrillUnderGold() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►$☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►$☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // я могу поднятся по лестнице и зайти на площадку
    @Test
    public void shouldICanGoFromLadderToArea() {
        givenFl("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼H◄ ☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼Y  ☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼Y# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼Y  ☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H► ☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        // и упасть
        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H [☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H#[☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼H ►☼" +
                "☼☼☼☼☼");

    }

    // я не могу сверлить бетон
    @Test
    public void shouldICantDrillWall() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ► ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ► ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");
    }

    // пока я падаю я не могу двигаться влево и справо, даже если там есть площадки
    @Test
    public void shouldICantMoveWhenFall() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  ►  ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼  ]  ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼  [  ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼  ]  ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼##◄##☼" +
                "☼☼☼☼☼☼☼");
    }

    // появляются на карте трубы, если я с площадки захожу на трубу то я ползу по ней
    @Test
    public void shouldIPipe() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼►~~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ }~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~}~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~} ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");
    }

    // с трубы я могу спрыгунть и тогда я буду падать до препятствия
    @Test
    public void shouldIFallFromPipe() {
        shouldIPipe();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~} ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~[☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#   [☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#    ☼" +
                "☼    [☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼    ►☼" +
                "☼☼☼☼☼☼☼");
    }

    // если по дороге я встречаюсь с золотом, то я его захвачу
    @Test
    public void shouldIGetGoldWhenFallenFromPipe() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ $~~◄☼" +
                "☼ $  #☼" +
                "☼ $   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ $~{ ☼" +
                "☼ $  #☼" +
                "☼ $   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ${~ ☼" +
                "☼ $  #☼" +
                "☼ $   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        dice(1, 5);
        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼$    ☼" +
                "☼ ]~~ ☼" +
                "☼ $  #☼" +
                "☼ $   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        dice(2, 5);
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼$$   ☼" +
                "☼  ~~ ☼" +
                "☼ ]  #☼" +
                "☼ $   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        dice(3, 5);
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼$$$  ☼" +
                "☼  ~~ ☼" +
                "☼    #☼" +
                "☼ ]   ☼" +
                "☼ $   ☼" +
                "☼☼☼☼☼☼☼");

        dice(4, 5);
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼$$$$ ☼" +
                "☼  ~~ ☼" +
                "☼    #☼" +
                "☼     ☼" +
                "☼ ◄   ☼" +
                "☼☼☼☼☼☼☼");

        verify(listener, times(4)).event(LoderunnerEvents.GET_GOLD);
    }

    // если я просверлил дырку и падаю в нее, а под ней ничего нет - то я падаю пока не найду препятствие
    @Test
    public void shouldIFallWhenUnderPitIsFree() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  ◄  ☼" +
                "☼#####☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ .Я  ☼" +
                "☼#*###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ ]   ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼#]###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼ ]   ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ ]   ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ◄   ☼" +
                "☼☼☼☼☼☼☼");
    }

    // если в процессе падения я вдург наткнулся на трубу то я повисаю на ней
    @Test
    public void shouldIPipeWhenFall() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  ◄  ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ ]   ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼#]###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼ ]   ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ {~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ {~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");
    }

    // я не могу просверлить дырку под другим камнем
    @Test
    public void shouldICantDrillUnderBrick() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►#☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►#☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // я могу спрыгнуть с трубы
    @Test
    public void shouldCanJumpFromPipe() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  ◄  ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼  ]  ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~{~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼  ]  ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼  ◄  ☼" +
                "☼☼☼☼☼☼☼");
    }
    
    // бага: мне нельзя спускаться с лестницы в бетон, так же как и подниматься
    // плюс я должен иметь возможность спустится по лестнице
    @Test
    public void shouldCantWalkThroughWallDown() {
        givenFl("☼☼☼☼☼" +
                "☼ ◄ ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Y ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Y ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldCantWalkThroughWallUp() {
        givenFl("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ H ☼" +
                "☼ H◄☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ H ☼" +
                "☼ Y ☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ Y ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ Y ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");
    }

    // бага: мне нельзя проходить с лестницы через бетон направо или налево
    @Test
    public void shouldCantWalkThroughWallLeftRight() {
        givenFl("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼H☼☼" +
                "☼ H◄☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼H☼☼" +
                "☼ Y ☼" +
                "☼☼☼☼☼");

        hero.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Y☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Y☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Y☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");
    }

    // бага: мне нельзя проходить через бетон
    @Test
    public void shouldCantWalkThroughWallLeftRight2() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼◄☼☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼◄☼☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼►☼☼" +
                "☼☼☼☼☼");
    }

    // бага: мне нельзя спрыгивать с трубы что сразу над бетоном, протелая сквозь него
    @Test
    public void shouldCantJumpThroughWall() {
        givenFl("☼☼☼☼☼☼" +
                "☼  ◄ ☼" +
                "☼  ~ ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  { ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  { ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");
    }

    @Test
    public void shouldBoardIsFree() {
        givenFl("☼☼☼☼☼☼" +
                "☼~◄$H☼" +
                "☼####☼" +
                "☼☼☼☼☼☼" +
                "☼☼☼☼☼☼" +
                "☼☼☼☼☼☼");

        for (int x = 0; x < game.getSize(); x ++) {
            for (int y = 0; y < game.getSize(); y ++) {
                assertFalse(game.isFree(x, y));
            }

        }
    }

    // Есть хитрый хак, когда спрыгиваешь с трубы, то можно при падении просверливать под собой
    // но она многоптточная
    @Test
    public void shouldDrillDown() {
        givenFl("☼☼☼☼☼☼" +
                "☼  ◄ ☼" +
                "☼  ~ ☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  { ☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.down();
        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  { ☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  ~ ☼" +
                "☼  ◄ ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  ~ ☼" +
                "☼ .Я ☼" +
                "☼#*##☼" +
                "☼☼☼☼☼☼");
    }

    // если я сам себя закопаю, то получу ли я за это очки? не должен!
    @Test
    public void shouldNoScoreWhenKamikadze() {
        givenFl("☼☼☼☼" +
                "☼ ◄☼" +
                "☼##☼" +
                "☼☼☼☼");

        hero.act();
        game.tick();
        hero.left();
        game.tick();
        game.tick();

        assertE("☼☼☼☼" +
                "☼  ☼" +
                "☼◄#☼" +
                "☼☼☼☼");

        for (int c = 3; c < Brick.DRILL_TIMER; c++) {
            game.tick();
        }

        assertE("☼☼☼☼" +
                "☼  ☼" +
                "☼Ѡ#☼" +
                "☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼" +
                "☼  ☼" +
                "☼Ѡ#☼" +
                "☼☼☼☼");

        // TODO если после кончины героя не сделать в том же тике newGame то с каждым тиком будут начисляться штрафные очки.
        // может пора уже все игрушки перевести в режим - я сама себя восстанавливаю, а не PlayerServiceImpl
        verify(listener, times(2)).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);
    }

    // я могу сверлить стенки под стенками, если те разрушены
    @Test
    public void shouldDrillUnderDrilledBrick() {
        givenFl("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ ◄  ☼" +
                "☼####☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.act();
        game.tick();

        hero.right();
        game.tick();

        hero.left();
        hero.act();
        game.tick();

        hero.right();
        game.tick();

        hero.left();
        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  .Я☼" +
                "☼  *#☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.left();
        game.tick();
        game.tick();

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼ ◄ #☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        hero.right();
        hero.act();
        game.tick();

        hero.left();
        hero.act();
        game.tick();

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼2Я #☼" +
                "☼ # #☼" +
                "☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼]3 #☼" +
                "☼ # #☼" +
                "☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼#24#☼" +
                "☼◄# #☼" +
                "☼☼☼☼☼☼");
    }

    // если я спрыгива с последней секции лестницы, то как-то неудачно этто делаю. Бага!
    @Test
    public void shouldJumpFromLadderDown() {
        givenFl("☼☼☼☼☼☼" +
                "☼ ◄  ☼" +
                "☼ H##☼" +
                "☼#H  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        hero.down();
        game.tick();

        hero.down();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#Y  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#Y  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#H[ ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#H  ☼" +
                "☼  ► ☼" +
                "☼☼☼☼☼☼");
    }

    private void ai(Direction value) {
        when(ai.getDirection(any(Field.class), any(Point.class))).thenReturn(value, null);
    }

    // чертик двигается так же как и обычный игрок - мжет ходить влево и вправо
    @Test
    public void shouldEnemyMoveLeft() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼«  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldEnemyMoveRight() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  »☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // если небыло команды чертик никуда не идет
    @Test
    public void shouldEnemyStopWhenNoMoreRightCommand() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  «☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // Чертик останавливается возле границы
    @Test
    public void shouldEnemyStopWhenWallRight() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  »☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  »☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldEnemyStopWhenWallLeft() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼«  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼«  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // В просверленную яму чертик легко может упасть
    @Test
    public void shouldEnemyFallInPitLeft() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼« ◄☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ »Я☼" +
                "☼# #☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  Я☼" +
                "☼#X#☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldEnemyFallInPitRight() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄ «☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        hero.act();
        game.tick();

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼R« ☼" +
                "☼# #☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼R  ☼" +
                "☼#X#☼" +
                "☼☼☼☼☼");
    }

    // при падении чертик не может передвигаться влево и вправо - ему мешают стены
    @Test
    public void shouldEnemyCantGoLeftIfWall() {
        shouldEnemyFallInPitRight();

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼R  ☼" +
                "☼#X#☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldEnemyCantGoRightIfWall() {
        shouldEnemyFallInPitLeft();

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  Я☼" +
                "☼#X#☼" +
                "☼☼☼☼☼");
    }

    // монстр сидит в ямке некоторое количество тиков, потом он вылазит
    @Test
    public void shouldMonsterGetUpFromPit() {
        shouldEnemyFallInPitLeft();

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼  Я☼" +
                "☼#X#☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ »Я☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        enemy.left(); // после этого он может двигаться
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼« Я☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // если чертик попадает на героя - тот погибает
    @Test
    public void shouldHeroDieWhenMeetWithEnemy() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄ «☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Ѡ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(1, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте причем чертик остается на своем месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼[  ☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // другой кейс, когда оба двигаются на встречу друг к другу
    @Test
    public void shouldHeroDieWhenMeetWithEnemy2() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄« ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼Ѡ  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(3, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте причем чертик остается на своем месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼  [☼" +
                "☼«  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // другой кейс, когда игрок идет на чертика
    @Test
    public void shouldHeroDieWhenMeetWithEnemy_whenHeroWalk() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄« ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Ѡ ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(3, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте причем чертик остается на своем месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼  [☼" +
                "☼ « ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // другой кейс, когда чертик идет на игрока
    @Test
    public void shouldHeroDieWhenMeetWithEnemy_whenEnemyWalk() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼◄« ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼Ѡ  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        verify(listener).event(LoderunnerEvents.KILL_HERO);
        verifyNoMoreInteractions(listener);

        dice(3, 3);
        game.tick();         // ну а после смерти он появляется в рендомном месте причем чертик остается на своем месте
        game.newGame(player);

        assertE("☼☼☼☼☼" +
                "☼  [☼" +
                "☼«  ☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // Чертик может зайти на лестницу и выйти обратно
    @Test
    public void shouldEnemyCanGoOnLadder() {
        givenFl("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ «H☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ «H☼" +
                "☼☼☼☼☼");
    }

    // Чертик может карабкаться по лестнице вверх
    @Test
    public void shouldEnemyCanGoOnLadderUp() {
        givenFl("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ «H☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");
    }

    // Чертик не может вылезти с лестницей за границы
    @Test
    public void shouldEnemyCantGoOnBarrierFromLadder() {
        shouldEnemyCanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");
    }

    // Чертик может спустится вниз, но не дальше границы экрана
    @Test
    public void shouldEnemyCanGoOnLadderDown() {
        shouldEnemyCanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼  Q☼" +
                "☼☼☼☼☼");
    }

    // Чертик может в любой момент спрыгнуть с лестницы и будет падать до тех пор пока не наткнется на препятствие
    @Test
    public void shouldEnemyCanFly() {
        shouldEnemyCanGoOnLadderUp();

        assertE("☼☼☼☼☼" +
                "☼  Q☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼ «H☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼ «H☼" +
                "☼  H☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼  H☼" +
                "☼  H☼" +
                "☼ «H☼" +
                "☼☼☼☼☼");
    }

    // Чертик может поднятся по лестнице и зайти на площадку
    @Test
    public void shouldEnemyCanGoFromLadderToArea() {
        givenFl("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼H« ☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼Q  ☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼Q# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼Q  ☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H» ☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        // и упасть
        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H »☼" +
                "☼H# ☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H#»☼" +
                "☼H  ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼H  ☼" +
                "☼H# ☼" +
                "☼H »☼" +
                "☼☼☼☼☼");
    }

    // пока чертик падает он не может двигаться влево и справо, даже если там есть площадки
    @Test
    public void shouldEnemyCantMoveWhenFall() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  »  ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼  »  ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼  »  ☼" +
                "☼     ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼  »  ☼" +
                "☼## ##☼" +
                "☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼##»##☼" +
                "☼☼☼☼☼☼☼");
    }

    // если чертик с площадки заходит на трубу то он ползет по ней
    @Test
    public void shouldEnemyPipe() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼»~~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ >~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~>~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~> ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");
    }

    // с трубы чертик может спрыгунть и тогда он будет падать до препятствия
    @Test
    public void shouldEnemyFallFromPipe() {
        shouldEnemyPipe();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~> ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~»☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#   »☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#    ☼" +
                "☼    »☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼#    ☼" +
                "☼     ☼" +
                "☼    »☼" +
                "☼☼☼☼☼☼☼");
    }

    // чертик может похитить 1 золото в падении
    @Test
    public void shouldEnemyGetGoldWhenFallenFromPipe() {
        givenFl("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼  $~~«☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼  $~< ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼  $<~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼  «~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        game.tick();      // чертик не берет больше 1 кучки золота

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  «  #☼" +
                "☼  $   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  «   ☼" +
                "☼  $  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  «  ◄☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.right();
        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $».Я☼" +
                "☼####*#☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.right();     // если чертик с золотом падает в ямку - он оставляет золото на поверхности
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $ »Я☼" +
                "☼#### #☼" +
                "☼☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $ $Я☼" +
                "☼####X#☼" +
                "☼☼☼☼☼☼☼☼");
    }

    // я могу ходить по монстру, который в ямке
    @Test
    public void shouldIWalkOnEnemy() {
        shouldEnemyGetGoldWhenFallenFromPipe();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼      ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $ $Я☼" +
                "☼####X#☼" +
                "☼☼☼☼☼☼☼☼");

        dice(1, 6);
        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼$     ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $ ◄ ☼" +
                "☼####X#☼" +
                "☼☼☼☼☼☼☼☼");

        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼$     ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  $◄  ☼" +
                "☼####X#☼" +
                "☼☼☼☼☼☼☼☼");

        dice(2, 6);
        hero.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼$$    ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  ◄   ☼" +
                "☼####X#☼" +
                "☼☼☼☼☼☼☼☼");

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼☼☼☼" +
                "☼$$    ☼" +
                "☼   ~~ ☼" +
                "☼  $  #☼" +
                "☼  $   ☼" +
                "☼  ◄ » ☼" +
                "☼######☼" +
                "☼☼☼☼☼☼☼☼");
    }

    // я не могу просверлить дырку непосредственно под монстром
    @Test
    public void shouldICantDrillUnderEnemy() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►»☼" +
                "☼###☼" +
                "☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ►»☼" +
                "☼###☼" +
                "☼☼☼☼☼");
    }

    // если я просверлил дырку монстр падает в нее, а под ней ничего нет - монстр не проваливается сквозь
    @Test
    public void shouldEnemyStayOnPitWhenUnderPitIsFree() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼» ◄  ☼" +
                "☼#####☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        hero.act();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼».Я  ☼" +
                "☼#*###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ »Я  ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼  Я  ☼" +
                "☼#X###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼  Я  ☼" +
                "☼#X###☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ »Я  ☼" +
                "☼#####☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");
    }

    // если в процессе падения чертик вдург наткнулся на трубу то он повисаю на ней
    @Test
    public void shouldEnemyPipeWhenFall() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  »  ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼ «   ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼#«###☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼ «   ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ <~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼# ###☼" +
                "☼     ☼" +
                "☼ <~~ ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");
    }

    // чертик может спрыгнуть с трубы
    @Test
    public void shouldEnemyCanJumpFromPipe() {
        givenFl("☼☼☼☼☼☼☼" +
                "☼  «  ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼  «  ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~<~ ☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼  «  ☼" +
                "☼     ☼" +
                "☼☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼☼" +
                "☼     ☼" +
                "☼     ☼" +
                "☼ ~~~ ☼" +
                "☼     ☼" +
                "☼  «  ☼" +
                "☼☼☼☼☼☼☼");
    }

    // чертику нельзя спускаться с лестницы в бетон, так же как и подниматься
    // плюс чертик должен иметь возможность спустится по лестнице
    @Test
    public void shouldEnemyCantWalkThroughWallDown() {
        givenFl("☼☼☼☼☼" +
                "☼ « ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Q ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ Q ☼" +
                "☼☼☼☼☼" +
                "☼☼☼☼☼");
    }

    @Test
    public void shouldEnemyCantWalkThroughWallUp() {
        givenFl("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ H ☼" +
                "☼ H«☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ H ☼" +
                "☼ Q ☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ Q ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼ Q ☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");
    }

    // Чертику нельзя проходить с лестницы через бетон направо или налево
    @Test
    public void shouldEnemyCantWalkThroughWallLeftRight() {
        givenFl("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼H☼☼" +
                "☼ H«☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼H☼☼" +
                "☼ Q ☼" +
                "☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Q☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Q☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼☼☼☼☼" +
                "☼☼Q☼☼" +
                "☼ H ☼" +
                "☼☼☼☼☼");
    }

    // чертику нельзя проходить через бетон
    @Test
    public void shouldEnemyCantWalkThroughWallLeftRight2() {
        givenFl("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼«☼☼" +
                "☼☼☼☼☼");

        enemy.left();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼«☼☼" +
                "☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼☼»☼☼" +
                "☼☼☼☼☼");
    }

    // Чертику нельзя спрыгивать с трубы что сразу над бетоном, протелая сквозь него
    @Test
    public void shouldEnemyCantJumpThroughWall() {
        givenFl("☼☼☼☼☼☼" +
                "☼  » ☼" +
                "☼  ~ ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  > ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼  > ☼" +
                "☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");
    }

    // если чертик спрыгивает с последней секции лестницы
    @Test
    public void shouldEnemyJumpFromLadderDown() {
        givenFl("☼☼☼☼☼☼" +
                "☼ »  ☼" +
                "☼ H##☼" +
                "☼#H  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        enemy.down();
        game.tick();

        enemy.down();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#Q  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#Q  ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        enemy.right();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#H» ☼" +
                "☼    ☼" +
                "☼☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H##☼" +
                "☼#H  ☼" +
                "☼  » ☼" +
                "☼☼☼☼☼☼");
    }

    // Чертик не может прыгять вверх :)
    @Test
    public void shouldEnemyCantJump() {
        givenFl("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼ »  ☼" +
                "☼☼☼☼☼☼");

        enemy.up();
        game.tick();

        assertE("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼ »  ☼" +
                "☼☼☼☼☼☼");
    }

    // я могу прыгнуть на голову монстру и мне ничего не будет
    @Test
    public void shouldICanJumpAtEnemyHead () {
        givenFl("☼☼☼☼☼" +
                "☼ ◄ ☼" +
                "☼   ☼" +
                "☼ » ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼ » ☼" +
                "☼☼☼☼☼");

        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼ ◄ ☼" +
                "☼ » ☼" +
                "☼☼☼☼☼");

        enemy.right();     // если он отойдет - я упаду дальше
        game.tick();

        assertE("☼☼☼☼☼" +
                "☼   ☼" +
                "☼   ☼" +
                "☼ ◄»☼" +
                "☼☼☼☼☼");
    }

    // если чертик после того, как упал в ямку оставил золото, то когда он выберется - он свое золото заберет

    // если монстр не успел вылезти из ямки и она заросла то монстр умирает
    // когда монстр умирает, то на карте появляется новый

    // сверлить находясь на трубе нельзя, в оригинале только находясь на краю трубы

    // карта намного больше, чем квардартик вьюшка, и я подходя к границе просто передвигаю вьюшку
    // повляется многопользовательский режим игры в формате "стенка на стенку"


}
