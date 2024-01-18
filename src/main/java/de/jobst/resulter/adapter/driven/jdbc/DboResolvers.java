package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@NoArgsConstructor
@Getter
@Setter
public class DboResolvers {

    private DboResolver<CupId, CupDbo> cupDboDboResolver = null;
    private DboResolver<EventId, EventDbo> eventDboResolver = null;
    private DboResolver<PersonId, PersonDbo> personDboResolver = null;
    private DboResolver<OrganisationId, OrganisationDbo> organisationDboResolver = null;
    private DboResolver<CountryId, CountryDbo> countryDboResolver = null;
    private DboResolver<ResultListId, ResultListDbo> resultListDboResolver = null;
    private DboResolver<CupScoreIdDbo, CupScoreDbo> cupScoreDboResolver = null;
    private DboResolver<SplitTimeListId, SplitTimeListDbo> splitTimeListDboResolver = null;

    @NonNull
    static DboResolvers empty() {
        return new DboResolvers();
    }
}
