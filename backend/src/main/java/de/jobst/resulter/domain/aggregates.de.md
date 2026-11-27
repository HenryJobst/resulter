### Aggregate, Root-Entities (RE), Entities (E), ValueObjects (VO)  ###

> **Deutsch** · [English](aggregates.md)

- Cup (RE)
    - CupId (VO)
    - CupName (VO)
    - CupType (VO)
    - Liste von EventId *

- Event (RE)
    - EventId (VO)
    - EventName (VO)
    - startTime (VO)
    - endTime (VO)
    - EventState (VO)
    - Liste von OrganisationId *
    - Liste von ResultListId *

- Person (RE)
    - PersonId (VO)
    - PersonName (E)
        - FamilyName (VO)
        - GivenName (VO)
    - BirthDate (VO)
    - Gender (VO)

- Organisation (RE)
    - OrganisationId (VO)
    - OrganisationName (VO)
    - OrganisationShortName (VO)
    - Organisation_type (VO)
    - CountryId
    - Liste von OrganisationId (VO) *

- Country (RE)
    - CountryId
    - CountryCode (VO)
    - CountryName (VO)

- Course (RE)
    - CourseId
    - EventId (VO) *
    - CourseName (VO)
    - CourseLength (VO)
    - CourseClimb (VO)
    - CourseControls (VO)

- Race (RE)
    - RaceId
    - EventId (VO) *
    - RaceName (VO)
    - RaceNumber (VO)

- ResultList (RE)
    - ResultListId (VO) *
    - EventId (VO) *
    - RaceId (VO) *
    - Creator (VO)
    - CreateTime (VO)
    - Status (VO)
    - Liste von ClassResult (E) +
        - CourseId (VO) *
        - ClassResultName (VO)
        - ClassResultShortName (VO)
        - Gender (VO)
        - Liste von PersonResult (E) +
            - PersonId (VO) *
            - OrganisationId (VO) *
            - Liste von PersonRaceResult (E) +
                - startTime (VO)
                - finishTime (VO)
                - PunchTime (VO)
                - Position (VO)
                - ResultStatus (VO)
                - SplitTimeListId (VO) *
                - CupScoreListId (VO) *

- SplitTimeList (RE) - Zwischenzeiten für ein Rennen und eine Person
    - SplitTimeListId (VO) *
    - EventId (VO) *
    - ResultListId (VO) *
    - PersonId (VO) *
    - ClassResultShortName (VO) *
    - RaceNumber(VO) *
    - Liste von SplitTime (E) +
        - ControlCode (VO)
        - PunchTime (VO)

- CupScoreList (RE) - Cup-Punkte für ein Rennen und eine Person
    - Id/CupScoreListId (VO)
    - EventId (VO) *
    - ResultListId (VO) *
    - ClassResultShortName (VO)
    - PersonId (VO) *
    - RaceNumber(VO) *
    - Map von CupScore je CupType (E) +
        - CupType (VO)
        - Score (VO)

    - CupScoreList (RE)
        - CupScoreListId (VO)
        - ResultListId (VO) *
        - CupId (VO)
        - Creator (VO)
        - CreateTime (VO)
        - Status (VO)
        - Liste von CupScore (E) +
            - ClassResultShortName (VO)
            - PersonId (VO) *
            - Score (VO) *
