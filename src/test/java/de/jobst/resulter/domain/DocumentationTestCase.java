package de.jobst.resulter.domain;

import de.jobst.resulter.springapp.ResulterApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class DocumentationTestCase {

    ApplicationModules modules = ApplicationModules.of(ResulterApplication.class);

    @Test
    void writeDocumentationSnippets() {

        new Documenter(modules)
            .writeDocumentation()
            .writeAggregatingDocument()
            .writeModuleCanvases()
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}

