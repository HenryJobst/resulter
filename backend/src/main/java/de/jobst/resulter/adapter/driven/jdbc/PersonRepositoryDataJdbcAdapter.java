package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.BirthDate;
import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class PersonRepositoryDataJdbcAdapter implements PersonRepository {

    private final PersonJdbcRepository personJdbcRepository;
    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;
    private final JdbcClient jdbcClient;

    public PersonRepositoryDataJdbcAdapter(
            PersonJdbcRepository personJdbcRepository,
            FilterStringConverter filterStringConverter,
            JdbcClient jdbcClient) {
        this.personJdbcRepository = personJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Person save(Person person) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setPersonDboResolver(
                id -> personJdbcRepository.findById(id.value()).orElseThrow());
        PersonDbo personEntity = PersonDbo.from(person, dboResolvers);
        PersonDbo savedPersonEntity = personJdbcRepository.save(personEntity);
        return savedPersonEntity.asPerson();
    }

    @Override
    public List<Person> findAll() {
        return personJdbcRepository.findAll().stream()
                .map(it -> it.asPerson())
                .sorted()
                .toList();
    }

    @Override
    public Optional<Person> findById(PersonId personId) {
        Optional<PersonDbo> personEntity = personJdbcRepository.findById(personId.value());
        return personEntity.map(it -> it.asPerson());
    }

    @Override
    public PersonPerson findOrCreate(Person person) {
        Optional<PersonDbo> personEntity = personJdbcRepository.findByFamilyNameAndGivenNameAndBirthDateAndGender(
                person.personName().familyName().value(),
                person.personName().givenName().value(),
                Optional.ofNullable(person.birthDate()).map(BirthDate::value).orElse(null),
                person.gender());

        return personEntity
                .map(personDbo -> new PersonPerson(person, personDbo.asPerson()))
                .orElseGet(() -> new PersonPerson(person, save(person)));
    }

    @Override
    @Transactional
    public Collection<PersonPerson> findOrCreate(Collection<Person> persons) {
        if (persons.isEmpty()) {
            return List.of();
        }

        // Step 1: Batch-load all existing persons matching the composite keys
        Map<Person.DomainKey, Person> existingPersons = batchFindExistingPersons(persons);

        // Step 2: Separate into found and not found
        List<PersonPerson> results = new ArrayList<>();
        List<Person> toCreate = new ArrayList<>();

        for (Person person : persons) {
            Person.DomainKey key = person.getDomainKey();
            Person existing = existingPersons.get(key);
            if (existing != null) {
                results.add(new PersonPerson(person, existing));
            } else {
                toCreate.add(person);
            }
        }

        // Step 3: Batch insert new persons
        if (!toCreate.isEmpty()) {
            List<Person> created = batchInsertPersons(toCreate);
            for (int i = 0; i < toCreate.size(); i++) {
                results.add(new PersonPerson(toCreate.get(i), created.get(i)));
            }
        }

        return results;
    }

    private Map<Person.DomainKey, Person> batchFindExistingPersons(Collection<Person> persons) {
        StringBuilder sql = new StringBuilder("SELECT * FROM person WHERE ");
        Map<String, Object> params = new java.util.HashMap<>();
        List<String> conditions = new ArrayList<>();

        int idx = 0;
        for (Person person : persons) {
            String familyName = person.personName().familyName().value();
            String givenName = person.personName().givenName().value();
            LocalDate birthDate = Optional.ofNullable(person.birthDate())
                    .map(BirthDate::value)
                    .orElse(null);
            Gender gender = person.gender();

            String condition = "(family_name = :fn" + idx
                    + " AND given_name = :gn" + idx
                    + " AND birth_date " + (birthDate == null ? "IS NULL" : "= :bd" + idx)
                    + " AND gender = :g" + idx + ")";
            conditions.add(condition);

            params.put("fn" + idx, familyName);
            params.put("gn" + idx, givenName);
            if (birthDate != null) {
                params.put("bd" + idx, birthDate);
            }
            params.put("g" + idx, gender.name());
            idx++;
        }

        sql.append(String.join(" OR ", conditions));

        List<PersonDbo> found = jdbcClient
                .sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    PersonDbo dbo = new PersonDbo();
                    dbo.setId(rs.getLong("id"));
                    dbo.setFamilyName(rs.getString("family_name"));
                    dbo.setGivenName(rs.getString("given_name"));
                    dbo.setGender(Gender.of(rs.getString("gender")));
                    java.sql.Date bd = rs.getDate("birth_date");
                    dbo.setBirthDate(bd != null ? bd.toLocalDate() : null);
                    return dbo;
                })
                .list();

        return found.stream().map(dbo -> dbo.asPerson()).collect(Collectors.toMap(Person::getDomainKey, p -> p));
    }

    private List<Person> batchInsertPersons(List<Person> persons) {
        // Use individual saves for now - Spring Data JDBC doesn't have native batch insert
        // returning generated IDs, but we avoid N+1 by doing single check query
        List<Person> created = new ArrayList<>();
        for (Person person : persons) {
            Person saved = save(person);
            created.add(saved);
        }
        return created;
    }

    @Override
    public Page<Person> findAll(@Nullable String filter, Pageable pageable) {
        Page<PersonDbo> page;
        Pageable dboPageable = mapPageableToDbo(pageable);
        if (filter != null) {
            PersonDbo personDbo = new PersonDbo();
            AtomicReference<ExampleMatcher> matcher = new AtomicReference<>(ExampleMatcher.matching());
            MappingFilterNodeTransformResult transformResult = parseFilter(filter);
            applyTransformToExample(transformResult, personDbo, matcher);

            page = personJdbcRepository.findAll(Example.of(personDbo, matcher.get()), dboPageable);
        } else {
            page = personJdbcRepository.findAll(dboPageable);
        }
        return mapDboPageToDomain(page);
    }

    private MappingFilterNodeTransformResult parseFilter(String filter) {
        FilterNode filterNode = filterStringConverter.convert(filter);
        log.info("FilterNode: {}", filterNode);
        return filterNodeTransformer.transform(filterNode);
    }

    private Pageable mapPageableToDbo(Pageable pageable) {
        return FilterAndSortConverter.mapOrderProperties(pageable, PersonDbo::mapOrdersDomainToDbo);
    }

    private Page<Person> mapDboPageToDomain(Page<PersonDbo> page) {
        return new PageImpl<>(
                page.stream().map(dbo -> dbo.asPerson()).toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    private void applyTransformToExample(
            MappingFilterNodeTransformResult transformResult,
            PersonDbo personDbo,
            AtomicReference<ExampleMatcher> matcher) {
        transformResult.filterMap().forEach((key, value) -> {
            String unquotedValue = value.value().replace("'", "");
            switch (key) {
                case "familyName" -> {
                    personDbo.setFamilyName(unquotedValue);
                    matcher.set(matcher.get().withMatcher("familyName", m -> m.stringMatcher(value.matcher())));
                }
                case "givenName" -> {
                    personDbo.setGivenName(unquotedValue);
                    matcher.set(matcher.get().withMatcher("givenName", m -> m.stringMatcher(value.matcher())));
                }
                case "id" -> {
                    personDbo.setId(Long.parseLong(unquotedValue));
                    matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                }
            }
        });
    }

    private SqlParts buildSqlWhereAndParamsFromTransform(MappingFilterNodeTransformResult transform) {
        List<String> where = new ArrayList<>();
        Map<String, Object> params = new java.util.HashMap<>();
        transform.filterMap().forEach((key, val) -> {
            String unquoted = val.value().replace("'", "");
            switch (key) {
                case "familyName" -> addStringFilter(
                        where, params, "p" + ".family_name", "familyName", unquoted, val.matcher());
                case "givenName" -> addStringFilter(
                        where, params, "p" + ".given_name", "givenName", unquoted, val.matcher());
                case "id" -> {
                    where.add("p" + ".id = :id");
                    params.put("id", Long.parseLong(unquoted));
                }
            }
        });
        String whereSql = where.isEmpty() ? "" : (" WHERE " + String.join(" AND ", where));
        return new SqlParts(whereSql, params);
    }

    private record SqlParts(String whereSql, Map<String, Object> params) {}

    @Override
    public Page<Person> findDuplicates(@Nullable String filter, Pageable pageable) {
        // Base join for duplicates
        String base =
                "FROM person p JOIN (SELECT family_name, given_name FROM person GROUP BY family_name, given_name HAVING COUNT(*) > 1) d ON p.family_name = d.family_name AND p.given_name = d.given_name";

        // Build WHERE and params using shared filter parsing
        SqlParts sqlParts;
        if (filter != null) {
            MappingFilterNodeTransformResult transform = parseFilter(filter);
            sqlParts = buildSqlWhereAndParamsFromTransform(transform);
        } else {
            sqlParts = new SqlParts("", new java.util.HashMap<>());
        }

        Pageable dboPageable = mapPageableToDbo(pageable);
        String orderBy = buildOrderBySql(dboPageable.getSort());
        int limit = dboPageable.isPaged() ? dboPageable.getPageSize() : Integer.MAX_VALUE;
        int offset = dboPageable.isPaged() ? (dboPageable.getPageNumber() * dboPageable.getPageSize()) : 0;

        String selectSql = "SELECT p.id, p.family_name, p.given_name, p.gender, p.birth_date " + base
                + sqlParts.whereSql + orderBy + " LIMIT :limit OFFSET :offset";
        sqlParts.params.put("limit", limit);
        sqlParts.params.put("offset", offset);

        List<Person> content = jdbcClient
                .sql(selectSql)
                .params(sqlParts.params)
                .query((rs, rowNum) -> mapPerson(rs))
                .list();

        String countSql = "SELECT COUNT(*) " + base + sqlParts.whereSql;
        Long total = jdbcClient
                .sql(countSql)
                .params(sqlParts.params)
                .query(Long.class)
                .optional()
                .orElse(0L);

        Page<Person> page = new PageImpl<>(content, dboPageable, total);
        return new PageImpl<>(
                page.getContent(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    private static void addStringFilter(
            List<String> where,
            Map<String, Object> params,
            String column,
            String paramBase,
            String value,
            ExampleMatcher.StringMatcher matcher) {
        String pattern;
        String valLower = value.toLowerCase();
        switch (matcher) {
            case STARTING -> pattern = valLower + "%";
            case ENDING -> pattern = "%" + valLower;
            case EXACT -> {
                where.add("LOWER(" + column + ") = :" + paramBase);
                params.put(paramBase, valLower);
                return;
            }
            default -> pattern = "%" + valLower + "%";
        }
        where.add("LOWER(" + column + ") LIKE :" + paramBase);
        params.put(paramBase, pattern);
    }

    private static String buildOrderBySql(@Nullable Sort sort) {
        if (sort == null || !sort.isSorted()) return "";
        List<String> orders = new ArrayList<>();
        for (Sort.Order o : sort) {
            String col =
                    switch (o.getProperty()) {
                        case "id" -> "p.id";
                        case "givenName" -> "p.given_name";
                        case "gender" -> "p.gender";
                        case "birthDate" -> "p.birth_date";
                        default -> "p.family_name";
                    };
            orders.add(col + (o.isAscending() ? " ASC" : " DESC"));
        }
        return orders.isEmpty() ? "" : (" ORDER BY " + String.join(", ", orders));
    }

    private static Person mapPerson(ResultSet rs) throws SQLException {
        PersonDbo dbo = new PersonDbo();
        dbo.setId(rs.getLong("id"));
        dbo.setFamilyName(rs.getString("family_name"));
        dbo.setGivenName(rs.getString("given_name"));
        String gender = rs.getString("gender");
        dbo.setGender(de.jobst.resulter.domain.Gender.of(gender));
        java.sql.Date bd = rs.getDate("birth_date");
        dbo.setBirthDate(bd != null ? bd.toLocalDate() : null);
        return dbo.asPerson();
    }

    @Override
    public void delete(Person person) {
        personJdbcRepository.deleteById(person.id().value());
    }

    @Override
    public Map<PersonId, Person> findAllById(Set<PersonId> idSet) {
        return StreamSupport.stream(
                        personJdbcRepository
                                .findAllById(idSet.stream().map(PersonId::value).toList())
                                .spliterator(),
                        true)
                .map(p -> PersonDbo.asPerson(p))
                .collect(Collectors.toMap(Person::id, person -> person));
    }
}
