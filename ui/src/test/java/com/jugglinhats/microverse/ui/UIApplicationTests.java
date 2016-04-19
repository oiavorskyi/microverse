package com.jugglinhats.microverse.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringApplicationConfiguration( UIApplication.class )
@WebIntegrationTest( randomPort = true )
public class UIApplicationTests {

	@Test
	public void contextLoads() {
	}

}
