package br.com.updev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

//@RunWith(SpringRunner.class)
@SpringBootTest
class BootApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> context.getBean(BootApplication.class));

    }

}
