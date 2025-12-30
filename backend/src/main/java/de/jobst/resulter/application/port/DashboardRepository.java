package de.jobst.resulter.application.port;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

/**
 * Repository interface for dashboard statistics.
 * Provides count methods for various entities in the system.
 */
@Repository
@SecondaryPort
public interface DashboardRepository {

    /**
     * Count all events in the system.
     *
     * @return total number of events
     */
    long countEvents();

    /**
     * Count all cups in the system.
     *
     * @return total number of cups
     */
    long countCups();

    /**
     * Count all persons in the system.
     *
     * @return total number of persons
     */
    long countPersons();

    /**
     * Count all organisations excluding OTHER type.
     *
     * @return total number of organisations (excludes OTHER type)
     */
    long countOrganisationsExcludingIndividuals();

    /**
     * Count all split times across all split time lists.
     *
     * @return total number of split time entries
     */
    long countSplitTimes();

    /**
     * Count all races in the system.
     *
     * @return total number of races
     */
    long countRaces();

    /**
     * Count all result lists in the system.
     *
     * @return total number of result lists
     */
    long countResultLists();

    /**
     * Count all generated certificates.
     *
     * @return total number of certificate generation records
     */
    long countCertificates();
}
