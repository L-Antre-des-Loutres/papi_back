package org.antredesloutres.papi.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antredesloutres.papi.model.enumerated.Effectiveness;

@Entity
@Table(
        name = "type_matchup",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_type_matchup_attacker_defender",
                columnNames = {"attacking_type_id", "defending_type_id"}
        ),
        indexes = {
                @Index(name = "idx_type_matchup_defender", columnList = "defending_type_id")
        }
)
@Getter @Setter
public class TypeMatchup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attacking_type_id")
    private Type attackingType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "defending_type_id")
    private Type defendingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Effectiveness effectiveness = Effectiveness.EFFECTIVE;
}