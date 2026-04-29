package org.antredesloutres.papi.model.domain;

import jakarta.persistence.*;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;

import java.util.Objects;

@Entity
@Table(name = "movesets", indexes = {
        @Index(name = "idx_movesets_pkmn", columnList = "pkmn_id"),
        @Index(name = "idx_movesets_move", columnList = "move_id")
})
public class Moveset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pkmn_id")
    private Pkmn pkmn;

    @ManyToOne
    @JoinColumn(name = "move_id")
    private Move move;

    @Enumerated(EnumType.STRING)
    private MoveLearnMethod learnMethod;

    /** Level at which the move is learned (only for LEVEL_UP, otherwise null). */
    private Integer learnLevel;

    public Moveset() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pkmn getPkmn() {
        return pkmn;
    }

    public void setPkmn(Pkmn pkmn) {
        this.pkmn = pkmn;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public MoveLearnMethod getLearnMethod() {
        return learnMethod;
    }

    public void setLearnMethod(MoveLearnMethod learnMethod) {
        this.learnMethod = learnMethod;
    }

    public Integer getLearnLevel() {
        return learnLevel;
    }

    public void setLearnLevel(Integer learnLevel) {
        this.learnLevel = learnLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Moveset moveset)) return false;
        return Objects.equals(id, moveset.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
