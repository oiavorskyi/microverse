package com.jugglinhats.microverse.restricted;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringApplicationConfiguration( RestrictedApplication.class )
@WebIntegrationTest( randomPort = true )
public class RestrictedApplicationTests {

	@Test
	public void contextLoads() {
	}

}
