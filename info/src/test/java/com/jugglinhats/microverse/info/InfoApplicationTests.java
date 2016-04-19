package com.jugglinhats.microverse.info;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringApplicationConfiguration( InfoApplication.class )
@WebIntegrationTest( randomPort = true )
public class InfoApplicationTests {

    @Value( "${local.server.port}" )
    private int port;

    private RestTemplate template = new TestRestTemplate();

    @Test
    public void rejectsUnauthenticatedRequests() {
        ResponseEntity<String> response = template.getForEntity
                ("http://localhost:{port}/info/", String.class, port);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String auth = response.getHeaders().getFirst("WWW-Authenticate");
        assertTrue("Wrong header: " + auth, auth.startsWith("Bearer"));
    }

}
