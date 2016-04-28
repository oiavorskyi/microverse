package com.jugglinhats.microverse.restricted;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class Controller {

    @PreAuthorize("#oauth2.hasScope('microverse.restricted')")
    @RequestMapping( "/data" )
    public Map<String, Object> data() {
        Map<String, Object> model = new HashMap<>();
        model.put("type", "restricted");
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello Secret World");
        return model;
    }
}
