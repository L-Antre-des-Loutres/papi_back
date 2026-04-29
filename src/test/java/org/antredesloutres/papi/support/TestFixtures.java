package org.antredesloutres.papi.support;

import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Moveset;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.domain.TypeMatchup;
import org.antredesloutres.papi.model.domain.User;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;

public final class TestFixtures {

    private TestFixtures() {}

    public static Type type(Integer id, String symbol) {
        Type t = new Type(symbol, "#abcdef", "Name " + symbol);
        t.setId(id);
        return t;
    }

    public static Ability ability(Integer id, String symbol) {
        Ability a = new Ability(symbol);
        a.setId(id);
        return a;
    }

    public static Move move(Integer id, String symbol, Type type) {
        Move m = new Move(symbol, "Name " + symbol, type, 80, 100, 15);
        m.setId(id);
        return m;
    }

    public static Pkmn pkmn(Integer id, String symbol) {
        Pkmn p = new Pkmn();
        p.setId(id);
        p.setSymbol(symbol);
        return p;
    }

    public static Moveset moveset(Integer id, Pkmn pkmn, Move move) {
        Moveset ms = new Moveset();
        ms.setId(id);
        ms.setPkmn(pkmn);
        ms.setMove(move);
        ms.setLearnMethod(MoveLearnMethod.LEVEL_UP);
        ms.setLearnLevel(10);
        return ms;
    }

    public static TypeMatchup matchup(Long id, Type attacker, Type defender, Effectiveness eff) {
        TypeMatchup m = new TypeMatchup();
        m.setId(id);
        m.setAttackingType(attacker);
        m.setDefendingType(defender);
        m.setEffectiveness(eff);
        return m;
    }

    public static User user(Long id, String username, String role) {
        return User.builder()
                .id(id)
                .username(username)
                .password("$2a$12$dummy.bcrypt.hash.value.for.testing.purposes.only.padding.x")
                .role(role)
                .build();
    }
}
