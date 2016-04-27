package com.jugglinhats.microverse.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
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

    @RequestMapping( "/" )
    public String home( Model model, Principal principal ) {
        model.addAttribute("username", principal.getName());
        return "index";
    }

}
