package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.CupScore;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.PersonId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@Table(name = "cup_score")
@AllArgsConstructor
public class CupScoreDbo implements Comparable<CupScoreDbo> {

    @NonNull
    @Column("person_id")
    private AggregateReference<PersonDbo, Long> person;

    @Nullable
    @Column("organisation_id")
    private AggregateReference<OrganisationDbo, Long> organisation;

    @NonNull
    @Column("class_result_short_name")
    private String classResultShortName;

    @Setter
    @Getter
    @Column("score")
    private Double score;

    public static CupScoreDbo from(CupScore cupScore) {
        return new CupScoreDbo(
            AggregateReference.to(cupScore.personId().value()),
            AggregateReference.to(cupScore.organisationId().value()),
            cupScore.classResultShortName().value(),
            cupScore.score());
    }

    public static List<CupScore> asCupScores(Collection<CupScoreDbo> cupScoreDbos) {
        return cupScoreDbos.stream()
            .map(it -> CupScore.of(
                PersonId.of(it.getPerson().getId()),
                it.getOrganisation() != null ?
                OrganisationId.of(it.getOrganisation().getId()) : null,
                ClassResultShortName.of(it.getClassResultShortName()),
                it.getScore()))
            .toList();
    }

    @Override
    public int compareTo(@NonNull CupScoreDbo o) {
        int val = Double.compare(score, o.score);
        if (val == 0) {
            val = classResultShortName.compareTo(o.classResultShortName);
        }
        if (val == 0) {
            val = Objects.compare(person.getId(), o.person.getId(), Long::compareTo);
        }
        return val;
    }

}
