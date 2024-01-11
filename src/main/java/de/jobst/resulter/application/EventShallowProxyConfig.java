package de.jobst.resulter.application;

public record EventShallowProxyConfig(Boolean shallowClassResults,
                                      Boolean shallowPersonResults,
                                      Boolean shallowPersonRaceResults,
                                      Boolean shallowSplitTimes,
                                      Boolean shallowCupScores,
                                      Boolean shallowPersons,
                                      Boolean shallowOrganisations,
                                      Boolean shallowEventOrganisations) {
}