package com.codenjoy.dojo.sample.model;

/**
 * Тут указана легенда всех возможных объектов и их состояний.
 * Важно помнить, что для каждой енумной константы надо создать спрайт в папке \src\main\webapp\resources\sprite.
 * Впрочем, если забудешь - тесты подскажут.
 */
public enum Elements {

    NONE(' '),       // например это пустое место, куда можно перейти герою
    WALL('☼'),       // а это стенка, через которую я хочу чтобы проходить нельзя было
    HERO('☺'),       // а это мой герой
    OTHER_HERO('☻'), // это герои других игроков
    DEAD_HERO('X'),  // а это временное явление - трупик моего героя, которое пропадет в следующем такте
    GOLD('$'),       // это то, за чем будет охота
    BOMB('x');       // а это бомба, на которой можно подорваться

    char ch;

    public char getChar() {
        return ch;
    }

    Elements(char ch) {
        this.ch = ch;
    }

    @Override
    public String toString() {
        return String.valueOf(ch);
    }

}
