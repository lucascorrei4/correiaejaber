package controllers;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import controllers.howtodo.AdminPub;
import controllers.howtodo.ApplicationPub;
import models.User;
import play.cache.Cache;
import util.Utils;

public class Security extends Secure.Security {
	
	static boolean authenticate(String username, String password) {
		String psw;
		try {
			psw = Utils.encode(Utils.decodeUrl(password.trim()));
			return User.connect(username, psw) != null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return false;
	}

	static boolean check(String profile) {
		if ("admin".equals(profile)) {
			return User.find("byEmail", connected()).<User> first().isAdmin;
		}
		return false;
	}

	static void onDisconnected() {
		AdminPub.loggedUserInstitution = null;
		Cache.delete(AdminPub.getLoggedUserInstitution().getCurrentSession());
		session.remove("username");
		ApplicationPub.index();
	}

	static void onAuthenticated() {
		User connectedUser = User.find("byEmail", session.get("username")).first();
		connect(connectedUser);
		if (connectedUser.getInstitutionId() == 0) {
			AdminPub.firstStep();
		} else {
			AdminPub.index();
		}
	}

	static void connect(User connectedUser) {
		/* Setting main object on connect user from login form */
		setCurrentSessionParameters(connectedUser);
	}
	
	static void setCurrentSessionParameters(User connectedUser) {
		/* Verify if I am */
		Map<String, Object> sessionParameters = new HashMap<String, Object>();
		sessionParameters.put("poweradmin", "lucascorreiaevangelista@gmail.com".equals(connectedUser.getEmail()) ? "true" : "false");
		sessionParameters.put("logged", connectedUser.id);
		sessionParameters.put("enableUser", enableMenu() ? "true" : "false");
		sessionParameters.put("idu", connectedUser.getId());
		sessionParameters.put("id", AdminPub.getLoggedUserInstitution().getInstitution() != null ? AdminPub.getLoggedUserInstitution().getInstitution().getId() : null);
		Cache.set(session.get("username"), sessionParameters);
	}
	
	public static boolean enableMenu() {
		if (AdminPub.getLoggedUserInstitution().getInstitution() != null && AdminPub.validateLicenseDate(AdminPub.getLoggedUserInstitution())) {
			return true;
		}
		return false;
	}
	
}
