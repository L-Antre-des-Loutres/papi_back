package org.antredesloutres.papi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationContextTest {

    @Autowired
    ApplicationContext context;

    @Test
    void contextLoads() {
        // arrange + act + assert
        assertThat(context).isNotNull();
    }

    @Test
    void mappersAreWired() {
        // arrange + act + assert
        assertThat(context.getBean("pkmnMapperImpl")).isNotNull();
        assertThat(context.getBean("typeMapperImpl")).isNotNull();
        assertThat(context.getBean("moveMapperImpl")).isNotNull();
        assertThat(context.getBean("abilityMapperImpl")).isNotNull();
        assertThat(context.getBean("movesetMapperImpl")).isNotNull();
        assertThat(context.getBean("typeMatchupMapperImpl")).isNotNull();
    }

    @Test
    void securityFilterChainIsWired() {
        // arrange + act + assert
        assertThat(context.getBean("filterChain")).isNotNull();
    }

    @Test
    void globalExceptionHandlerIsWired() {
        // arrange + act + assert
        assertThat(context.getBeansOfType(org.antredesloutres.papi.exception.GlobalExceptionHandler.class))
                .hasSize(1);
    }
}
