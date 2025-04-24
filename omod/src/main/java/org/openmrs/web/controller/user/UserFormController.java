/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PasswordException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.propertyeditor.RoleEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.UserValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.user.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Used for creating/editing User
 */
@Controller
public class UserFormController {
	
	protected static final Log log = LogFactory.getLog(UserFormController.class);
	
	@Autowired
	private UserValidator userValidator;
	
	@Autowired
	private UserService userService;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Role.class, new RoleEditor());
	}
	
	// the personId attribute is called person_id so that spring MVC doesn't try to bind it to the personId property of user
	@ModelAttribute("user")
	public User formBackingObject(WebRequest request, @RequestParam(required = false, value = "person_id") Integer personId) {
		String userId = request.getParameter("userId");
		User u = null;
		if (!StringUtils.isEmpty(userId)) {
			try {
				u = Context.getUserService().getUser(Integer.valueOf(userId));
			}
			catch (Exception ex) {
				log.error("Error while getting user", ex);
			}
		}
		
		if (u == null) {
			u = new User();
		}
		if (personId != null) {
			u.setPerson(Context.getPersonService().getPerson(personId));
		} else if (u.getPerson() == null) {
			Person p = new Person();
			p.addName(new PersonName());
			u.setPerson(p);
		}
		return u;
	}
	
	@ModelAttribute("allRoles")
	public List<Role> getRoles(WebRequest request) {
		List<Role> roles = Context.getUserService().getAllRoles();
		if (roles == null) {
			roles = new Vector<Role>();
		}
		
		for (String s : OpenmrsConstants.AUTO_ROLES()) {
			Role r = new Role(s);
			roles.remove(r);
		}
		return roles;
	}
	
	@RequestMapping(value = "/admin/users/user.form", method = RequestMethod.GET)
	public String showForm(@RequestParam(required = false, value = "userId") Integer userId,
	        @RequestParam(required = false, value = "createNewPerson") String createNewPerson,
	        @ModelAttribute("user") User user, ModelMap model) {

		try {
			boolean isPlatform22OrNewer = isPlatform22OrNewer();
			if (isPlatform22OrNewer) {
				Field emailField = getEmailField();
				model.addAttribute("hasEmailField", isPlatform22OrNewer);
				model.addAttribute("email", emailField.get(user));	
			}
		} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
			log.warn("Email field not available for setting", e);
		}
		
		// the formBackingObject method above sets up user, depending on userId and personId parameters
		
		model.addAttribute("isNewUser", isNewUser(user));
		if (isNewUser(user) || Context.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS)) {
			model.addAttribute("modifyPasswords", true);
		}
		
		if (createNewPerson != null) {
			model.addAttribute("createNewPerson", createNewPerson);
		}
		
		if (!isNewUser(user)) {
			model.addAttribute("changePassword", new UserProperties(user.getUserProperties()).isSupposedToChangePassword());
			
			model.addAttribute("secretQuestion", userService.getSecretQuestion(user));
		}
		
		if (user.getPerson().getId() != null
		        && !Context.getProviderService().getProvidersByPerson(user.getPerson()).isEmpty()) {
			model.addAttribute("isProvider", true);
			model.addAttribute("providerList", Context.getProviderService().getProvidersByPerson(user.getPerson()));
		} else {
			model.addAttribute("isProvider", false);
		}
		
		// not using the default view name because I'm converting from an existing form
		return "module/legacyui/admin/users/userForm";
	}
	
	/**
	 * @should work for an example
	 */
	@RequestMapping(value = "/admin/users/user.form", method = RequestMethod.POST)
	public String handleSubmission(WebRequest request, HttpSession httpSession, ModelMap model,
	        @RequestParam(required = false, value = "action") String action,
	        @RequestParam(required = false, value = "userFormOldPassword") String oldPassword,
	        @RequestParam(required = false, value = "userFormPassword") String password,
	        @RequestParam(required = false, value = "secretQuestion") String secretQuestion,
	        @RequestParam(required = false, value = "secretAnswer") String secretAnswer,
	        @RequestParam(required = false, value = "confirm") String confirm,
	        @RequestParam(required = false, value = "forcePassword") Boolean forcePassword,
	        @RequestParam(required = false, value = "roleStrings") String[] roles,
	        @RequestParam(required = false, value = "createNewPerson") String createNewPerson,
	        @RequestParam(required = false, value = "providerCheckBox") String addToProviderTableOption,
	        @RequestParam(required = false, value = "email") String email,
	        @ModelAttribute("user") User user, BindingResult errors, HttpServletResponse response) {
		
		UserService us = Context.getUserService();
		MessageSourceService mss = Context.getMessageSourceService();
		
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		} else if (mss.getMessage("User.assumeIdentity").equals(action)) {
			Context.becomeUser(user.getSystemId());
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.assumeIdentity.success");
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ARGS, user.getPersonName());
			return "redirect:/index.htm";
			
		} else if (mss.getMessage("User.delete").equals(action)) {
			try {
				Context.getUserService().purgeUser(user);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.delete.success");
				return "redirect:users.list";
			}
			catch (Exception ex) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "User.delete.failure");
				log.error("Failed to delete user", ex);
				return "redirect:/admin/users/user.form?userId=" + request.getParameter("userId");
			}
			
		} else if (mss.getMessage("User.retire").equals(action)) {
			String retireReason = request.getParameter("retireReason");
			if (!(StringUtils.hasText(retireReason))) {
				errors.rejectValue("retireReason", "User.disableReason.empty");
				return showForm(user.getUserId(), createNewPerson, user, model);
			} else {
				us.retireUser(user, retireReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.retiredMessage");
			}
			
		} else if (mss.getMessage("User.unRetire").equals(action)) {
			us.unretireUser(user);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.unRetiredMessage");
		} else {
			// check if username is already in the database
			if (us.hasDuplicateUsername(user)) {
				errors.rejectValue("username", "error.username.taken");
			}
			
			// check if password and password confirm are identical
			if (password == null || password.equals("XXXXXXXXXXXXXXX")) {
				password = "";
			}
			if (confirm == null || confirm.equals("XXXXXXXXXXXXXXX")) {
				confirm = "";
			}
			
			if (!password.equals(confirm)) {
				errors.reject("error.password.match");
			}
			
			if (password.length() == 0 && isNewUser(user)) {
				errors.reject("options.login.password.null");
			}
			
			//check password strength
			if (password.length() > 0) {
				try {
					OpenmrsUtil.validatePassword(user.getUsername(), password, user.getSystemId());
				}
				catch (PasswordException e) {
					errors.reject(e.getMessage());
				}
			}
			
			if (isPlatform22OrNewer() && isEmailValid(email)) {
				Field emailField;
				try {
					emailField = getEmailField();
					emailField.set(user, email);
				} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
					log.error("Error while setting the email field", e);
				}
			} else {
				log.warn("Invalid or unspecified email: " + (email != null ? "'" + email + "'" : "not provided"));
			}

			Set<Role> newRoles = new HashSet<Role>();
			if (roles != null) {
				for (String r : roles) {
					// Make sure that if we already have a detached instance of this role in the
					// user's roles, that we don't fetch a second copy of that same role from
					// the database, or else hibernate will throw a NonUniqueObjectException.
					Role role = null;
					if (user.getRoles() != null) {
						for (Role test : user.getRoles()) {
							if (test.getRole().equals(r)) {
								role = test;
							}
						}
					}
					if (role == null) {
						role = us.getRole(r);
						user.addRole(role);
					}
					newRoles.add(role);
				}
			}
			
			if (user.getRoles() == null) {
				newRoles.clear();
			} else {
				user.getRoles().retainAll(newRoles);
			}
			
			String[] keys = request.getParameterValues("property");
			String[] values = request.getParameterValues("value");
			
			if (keys != null && values != null) {
				for (int x = 0; x < keys.length; x++) {
					String key = keys[x];
					String val = values[x];
					user.setUserProperty(key, val);
				}
			}
			
			if (StringUtils.hasLength(secretQuestion) && !StringUtils.hasLength(secretAnswer)) {
				errors.reject("error.User.secretAnswer.empty");
			} else if (!StringUtils.hasLength(secretQuestion) && StringUtils.hasLength(secretAnswer)) {
				errors.reject("error.User.secretQuestion.empty");
			}
			
			new UserProperties(user.getUserProperties()).setSupposedToChangePassword(forcePassword);
			
			userValidator.validate(user, errors);
			
			if (errors.hasErrors()) {
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return showForm(user.getUserId(), createNewPerson, user, model);
			}
			
			if (isNewUser(user)) {
				us.createUser(user, password);
			} else {
				us.saveUser(user);
				
				if (!"".equals(password) && Context.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS)) {
					if (log.isDebugEnabled()) {
						log.debug("calling changePassword for user " + user + " by user " + Context.getAuthenticatedUser());
					}
					us.changePassword(user, oldPassword, password);
				}
			}
			
			if (StringUtils.hasLength(secretQuestion) && StringUtils.hasLength(secretAnswer)) {
				us.changeQuestionAnswer(user, secretQuestion, secretAnswer);
			}
			
			//Check if admin wants the person associated with the user to be added to the Provider Table
			if (addToProviderTableOption != null) {
				Provider provider = new Provider();
				provider.setPerson(user.getPerson());
				provider.setIdentifier(user.getSystemId());
				Context.getProviderService().saveProvider(provider);
			}
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.saved");
		}
		return "redirect:users.list";
	}
	
	/**
	 * Superficially determines if this form is being filled out for a new user (basically just
	 * looks for a primary key (user_id)
	 * 
	 * @param user
	 * @return true/false if this user is new
	 */
	private Boolean isNewUser(User user) {
		return user == null ? true : user.getUserId() == null;
	}
	
	/**
	 * @return true if email is valid or false otherwise
	 * @param email
	 */
	private boolean isEmailValid(String email) {
		return StringUtils.hasLength(email) && EmailValidator.getInstance().isValid(email);
	}
	
	/**
	 * @return an email field
	 * @param user
	 */
	private Field getEmailField() {
		try {
			Field emailField = User.class.getDeclaredField("email");
			emailField.setAccessible(true);
			return emailField;
		} catch (NoSuchFieldException | SecurityException e) {
			log.warn("Email field not available for setting", e);
			return null;
		}
	}
	
	/**
	 * @return true if the platform version is 2.2 or higher
	 */
	private boolean isPlatform22OrNewer() {
		String platformVersion = OpenmrsConstants.OPENMRS_VERSION_SHORT.substring(0, 3);
		try {
			float versionFloat = Float.parseFloat(platformVersion);
			if (versionFloat >= 2.2) {
				return true;
			}
		}
		catch (NumberFormatException e) {
			log.error("Unable to parse platform value text to float", e);
		}
		return false;
	}
}
