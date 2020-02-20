package com.gsuite.app.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.reports.model.Activity;

import com.gsuite.app.repo.UserRepository;
import com.gsuite.app.service.UserService;


@RestController
@RequestMapping(value = "/gsuite")
public class UserController {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final UserRepository userRepository;

	private final UserService userService;

	public UserController(UserRepository userRepository, UserService userService) {
		this.userRepository = userRepository;
		this.userService = userService;
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public List<User> getAllUsers() throws GeneralSecurityException, IOException {
		return userService.getAllUsers();
	}
	
	@RequestMapping(value = "/activities", method = RequestMethod.GET)
	public List<Activity> getUserActivities() throws GeneralSecurityException, IOException {
		return userService.getUserActivities();
	}

}