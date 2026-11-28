package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SvgSanitizerTest {

    private static String run(String svg) {
        return new String(SvgSanitizer.sanitize(svg.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    @Test
    void removesScriptHandlersAndExternalLinks() {
        String out = run("<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' onload='x()'>"
                + "<script>alert(1)</script><foreignObject><div/></foreignObject>"
                + "<a xlink:href='javascript:alert(1)'><rect onclick='x()' width='1' height='1'/></a>"
                + "<image href='http://evil/x.png'/><style>@import url(http://evil/a.css);</style></svg>");
        assertThat(out).doesNotContain("script", "foreignObject", "onload", "onclick", "javascript", "evil", "@import");
        assertThat(out).contains("<rect");
    }

    @Test
    void keepsHarmlessContent() {
        String out = run("<svg xmlns='http://www.w3.org/2000/svg'><defs><linearGradient id='g'/></defs>"
                + "<rect fill='url(#g)' style='fill:url(#g)' width='1' height='1'/></svg>");
        assertThat(out).contains("url(#g)", "linearGradient");
    }

    @Test
    void rejectsDoctypeAndNonSvg() {
        assertThatThrownBy(() -> run("<!DOCTYPE svg [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><svg xmlns='http://www.w3.org/2000/svg'>&x;</svg>"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> run("<html/>")).isInstanceOf(IllegalArgumentException.class);
    }
}
