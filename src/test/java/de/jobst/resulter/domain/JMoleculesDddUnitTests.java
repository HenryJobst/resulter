package de.jobst.resulter.domain;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

@SuppressWarnings("unused")
@AnalyzeClasses(packages = "de.jobst.resulter")
class JMoleculesRulesUnitTest {

    @ArchTest
    ArchRule dddRules = JMoleculesDddRules.all();

    @ArchTest
    ArchRule layeredRules = JMoleculesArchitectureRules.ensureLayering();

    @ArchTest
    ArchRule hexagonalRules = JMoleculesArchitectureRules.ensureHexagonal();
}
