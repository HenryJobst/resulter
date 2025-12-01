package de.jobst.resulter;

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

