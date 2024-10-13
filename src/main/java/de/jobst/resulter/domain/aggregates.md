### Aggregates, Root-Entities(RE), Entities (E), ValueObjects (VO)  ###

- Cup (RE)
    - CupId (VO)
    - CupName (VO)
    - CupType (VO)
    - List of EventId *

- Event (RE)
    - EventId (VO)
    - EventName (VO)
    - startTime (VO)
    - endTime (VO)
    - EventState (VO)
    - List of OrganisationId *
    - List of ResultListId *

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
    - OrganisationType (VO)
    - CountryId
    - List of OrganisationId (VO) *

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
    - List of ClassResult (E) +
        - CourseId (VO) *
        - ClassResultName (VO)
        - ClassResultShortName (VO)
        - Gender (VO)
        - List of PersonResult (E) +
            - PersonId (VO) *
            - OrganisationId (VO) *
            - List of PersonRaceResult (E) +
                - startTime (VO)
                - finishTime (VO)
                - PunchTime (VO)
                - Position (VO)
                - ResultStatus (VO)
                - SplitTimeListId (VO) *
                - CupScoreListId (VO) *

- SplitTimeList (RE) - Split times for a race and person
    - SplitTimeListId (VO) *
    - EventId (VO) *
    - ResultListId (VO) *
    - PersonId (VO) *
    - ClassResultShortName (VO) *
    - RaceNumber(VO) *
    - List of SplitTime (E) +
        - ControlCode (VO)
        - PunchTime (VO)

- CupScoreList (RE) - Cup scores for a race and person
    - Id/CupScoreListId (VO)
    - EventId (VO) *
    - ResultListId (VO) *
    - ClassResultShortName (VO)
    - PersonId (VO) *
    - RaceNumber(VO) *
    - Map of CupScore by CupType (E) +
        - CupType (VO)
        - Score (VO)

    - CupScoreList (RE)
        - CupScoreListId (VO)
        - ResultListId (VO) *
        - CupId (VO)
        - Creator (VO)
        - CreateTime (VO)
        - Status (VO)
        - List of CupScore (E) +
            - ClassResultShortName (VO)
            - PersonId (VO) *
            - Score (VO) *
