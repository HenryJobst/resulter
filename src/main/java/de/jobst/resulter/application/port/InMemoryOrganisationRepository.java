package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryOrganisationRepository implements OrganisationRepository {

    private final Map<OrganisationId, Organisation> organisations = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<Organisation> savedOrganisations = new ArrayList<>();

    @Override
    public Organisation save(Organisation organisation) {
        if (ObjectUtils.isEmpty(organisation.getId()) || organisation.getId().value() == 0) {
            organisation.setId(OrganisationId.of(sequence.incrementAndGet()));
        }
        organisations.put(organisation.getId(), organisation);
        savedOrganisations.add(organisation);
        return organisation;
    }

    @Override
    public List<Organisation> findAll() {
        return List.copyOf(organisations.values());
    }

    @Override
    public Optional<Organisation> findById(OrganisationId organisationId) {
        return Optional.ofNullable(organisations.get(organisationId));
    }

    @Override
    public Organisation findOrCreate(Organisation organisation) {
        return organisations.values()
            .stream()
            .filter(it -> Objects.equals(it.getName(), organisation.getName()))
            .findAny()
            .orElseGet(() -> save(organisation));
    }

    @Override
    public Collection<Organisation> findOrCreate(Collection<Organisation> organisations) {
        return organisations.stream().map(this::findOrCreate).toList();
    }

    @Override
    public void deleteOrganisation(Organisation organisation) {
        if (ObjectUtils.isEmpty(organisation.getId()) || organisation.getId().value() == 0) {
            return;
        }
        organisations.remove(organisation.getId());
        savedOrganisations.remove(organisation);
    }

    @Override
    public Map<OrganisationId, Organisation> findAllById(Set<OrganisationId> idSet) {
        return null;
    }

    @Override
    public Map<OrganisationId, Organisation> loadOrganisationTree(Set<OrganisationId> idSet) {
        return null;
    }

    @SuppressWarnings("unused")
    public List<Organisation> savedOrganisations() {
        return savedOrganisations;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedOrganisations.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedOrganisations.clear();
    }

}
