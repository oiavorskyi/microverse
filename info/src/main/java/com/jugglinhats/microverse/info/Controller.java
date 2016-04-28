package com.jugglinhats.microverse.info;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class Controller {

    @PreAuthorize("#oauth2.hasScope('microverse.info')")
    @RequestMapping( "/data" )
    public Map<String, Object> data() {
        Map<String, Object> model = new HashMap<>();
        model.put("type", "open");
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello World");
        return model;
    }
}
