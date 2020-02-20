package com.gsuite.app.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.reports.model.Activity;

public interface UserService {

	List<User> getAllUsers() throws GeneralSecurityException, IOException;
	
	List<Activity> getUserActivities() throws GeneralSecurityException, IOException;

}