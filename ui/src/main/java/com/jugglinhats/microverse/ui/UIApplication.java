package com.jugglinhats.microverse.ui;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@SpringBootApplication
@EnableOAuth2Sso
@Controller
public class UIApplication {

	public static void main(String[] args) {
		SpringApplication.run(UIApplication.class, args);
	}

    @Autowired
        private OAuth2RestTemplate restTemplate;

    @RequestMapping( "/" )
    public String home( Model model, Principal principal ) {
        model.addAttribute("username", principal.getName());
        return "index";
    }

    @RequestMapping("/open-info")
    public String openInfo(Model model) {
        final ResponseEntity<Info> info = restTemplate.getForEntity
                ("http://localhost:8081/data", Info.class);
        model.addAttribute("info", info.getBody());
        return "info";
    }

    @Data
    public static class Info {

        private String type;
        private String id;
        private String content;

    }

}
