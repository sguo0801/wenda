package com.nowcoder.wenda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

public class InitDatabaseTest {
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = WendaApplication.class)
    @Sql("/init-schema.sql")
    public class WendaApplicationTests {

        @Test
        public void contextLoads() {
        }

    }

}
