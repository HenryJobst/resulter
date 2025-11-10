package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor
@Getter
@Setter
public class DboResolvers {

    private @Nullable DboResolver<CupId, CupDbo> cupDboDboResolver = null;
    private @Nullable DboResolver<EventId, EventDbo> eventDboResolver = null;
    private @Nullable DboResolver<RaceId, RaceDbo> raceDboResolver = null;
    private @Nullable DboResolver<PersonId, PersonDbo> personDboResolver = null;
    private @Nullable DboResolver<CourseId, CourseDbo> courseDboResolver = null;
    private @Nullable DboResolver<OrganisationId, OrganisationDbo> organisationDboResolver = null;
    private @Nullable DboResolver<CountryId, CountryDbo> countryDboResolver = null;
    private @Nullable DboResolver<ResultListId, ResultListDbo> resultListDboResolver = null;
    private @Nullable DboResolver<CupScoreListId, CupScoreListDbo> cupScoreListDboResolver = null;
    private @Nullable DboResolver<SplitTimeListId, SplitTimeListDbo> splitTimeListDboResolver = null;
    private @Nullable DboResolver<MediaFileId, MediaFileDbo> mediaFileDboResolver = null;
    private @Nullable DboResolver<EventCertificateId, EventCertificateDbo> eventCertificateDboResolver = null;
    private @Nullable DboResolver<EventCertificateStatId, EventCertificateStatDbo> eventCertificateStatDboResolver = null;

    static DboResolvers empty() {
        return new DboResolvers();
    }

}
