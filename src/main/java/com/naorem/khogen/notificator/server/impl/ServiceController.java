package com.naorem.khogen.notificator.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ServiceController {

	@RequestMapping(value = {"/jsp", "/"}, method = RequestMethod.GET)
	public String home(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
		return "index";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
		return "login";
	}
	@RequestMapping(value = {"/accessDenied"}, method = RequestMethod.GET)
	public String accessDenied(final HttpServletRequest request, final HttpServletResponse response, final Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null && authentication.isAuthenticated()) {
			return "index";
		}
		return "accessDenied";
	}
}
