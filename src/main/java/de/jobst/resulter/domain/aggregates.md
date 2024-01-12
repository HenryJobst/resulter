### Aggregates, Root-Entities(RE), Entities (E), ValueObjects (VO)  ###

- Cup (RE)
    - CupId (VO)
    - CupName (VO)
    - CupType (VO)
    - List of EventId

- Event (RE)
    - EventId (VO)
    - EventName (VO)
    - startTime (VO)
    - endTime (VO)
    - EventState (VO)
    - List of OrganisationId
    - List of ResultListId

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
    - List of OrganisationId (VO)

- Country (RE)
    - CountryId
    - CountryCode (VO)
    - CountryName (VO)

- ResultList (RE)
    - EventId (VO)
    - List of ClassResult (E) +
        - ClassResultName (VO)
        - ClassResultShortName (VO)
        - Gender (VO)
        - List of PersonResult (E) +
            - PersonId (VO)
            - OrganisationId (VO)
            - List of PersonRaceResult (E) +
                - RaceNumber (VO)
                - startTime (VO)
                - finishTime (VO)
                - PunchTime (VO)
                - Position (VO)
                - ResultStatus (VO)
                - List of SplitTime (E) +
                    - ControlCode (VO)
                    - PunchTime (VO)

- CupScoreList (RE)
    - EventId (VO)
    - List of ClassResultBase (E) +
        - ClassResultName (VO)
        - List of PersonResultBase (E) +
            - PersonId (VO)
            - List of PersonResultBase (E) +
                - RaceNumber (VO)
                - Map of CupScore by CupType (E) +
                    - CupType (VO)
                    - Score (VO)
