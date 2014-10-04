package com.naorem.khogen.account.server.auth;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import com.naorem.khogen.account.server.core.AccountManager;
import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.server.common.Cache;
import com.naorem.khogen.server.common.GlobalConstants;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.Result;

//import org.springframework.stereotype.Component

public class UserLoginAuthProvider implements AuthenticationProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginAuthProvider.class);
	@Autowired
	private AccountManager accountManager;

	private Cache cache;

	private Map<String, Set<String>> userRoles = new HashMap<String, Set<String>>();

	public UserLoginAuthProvider(final Properties properties) throws IOException {
		cache = Cache.getInstance();
		for (String property : properties.stringPropertyNames()) {
			String propertyValue = properties.getProperty(property);
			String[] users = propertyValue.split(",");
			for (String user : users) {
				user = user.trim();
				if (!user.isEmpty()) {
					Set<String> roles = userRoles.get(user);
					if (roles == null) {
						roles = new HashSet<String>();
						userRoles.put(user, roles);
					}
					roles.add(property);
				}
			}
		}
	}

	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		boolean isAuthenticated = false;
		try {
			Account account = (Account) cache.get(authentication.getName());
			if(account != null) {
				if (authentication.getCredentials().equals(account.getCredential().getPassword())) {
					isAuthenticated = true;
				}
			}
			if (!isAuthenticated) {
				Result<Account> result = accountManager.getAccount(authentication.getName(), false);
				if (result.getError() == Error.SUCCESS) {
					account = result.getValue();
					cache.put(authentication.getName(), account);
					if (authentication.getCredentials().equals(account.getCredential().getPassword())) {
						isAuthenticated = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Authentication throws exception", e);
		}
		if (!isAuthenticated) {
			LOGGER.error("Failed to authenticate user " + authentication.getPrincipal());
			throw new BadCredentialsException("Failed to authenticate user " + authentication.getPrincipal());
		}
		AuthenticationImpl authenticationImpl = new AuthenticationImpl();
		authenticationImpl.setName(authentication.getName());
		authenticationImpl.setPrincipal(authentication.getPrincipal());
		authenticationImpl.setGrantedAuthorities(getGrantedAuthorities((String) authentication.getPrincipal()));
		authenticationImpl.setDetails(authentication.getDetails());
		authenticationImpl.setCredentials(authentication.getCredentials());
		authenticationImpl.setAuthenticated(isAuthenticated);
		return authenticationImpl;
	}

	public boolean supports(Class<?> clazz) {
		return Authentication.class.isAssignableFrom(clazz);
	}

	private Collection<GrantedAuthority> getGrantedAuthorities(final String user) {
		final Collection<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
		Set<String> roles = userRoles.get(user);
		if (roles != null) {
			for (String role : roles) {
				grantedAuthorities.add(new GrantedAuthorityImpl(role));
			}
		}
		Set<String> allRoles = userRoles.get(GlobalConstants.ROLE_ALL_USER);
		if (allRoles != null) {
			for (String role : allRoles) {
				grantedAuthorities.add(new GrantedAuthorityImpl(role));
			}
		}
		return grantedAuthorities;
	}

	public static class GrantedAuthorityImpl implements GrantedAuthority {
		private static final long serialVersionUID = 1L;
		private final String role;

		public GrantedAuthorityImpl(final String role) {
			this.role = role;
		}

		@Override
		public String getAuthority() {
			return role;
		}

		@Override
		public boolean equals(Object object) {
			if (object == null) {
				return false;
			}
			if (this.getClass() != object.getClass()) {
				return false;
			}
			GrantedAuthorityImpl other = (GrantedAuthorityImpl) object;
			return role.equals(other.role);
		}

	}

	public static class AuthenticationImpl implements Authentication {
		private static final long serialVersionUID = 1L;
		private String name;
		private Object credentials;
		private Object details;
		private Object principal;
		private boolean authenticated;
		private Collection<GrantedAuthority> grantedAuthorities;

		public AuthenticationImpl() {

		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return grantedAuthorities;
		}

		@Override
		public Object getCredentials() {
			return credentials;
		}

		@Override
		public Object getDetails() {
			return details;
		}

		@Override
		public Object getPrincipal() {
			return principal;
		}

		@Override
		public boolean isAuthenticated() {
			return authenticated;
		}

		@Override
		public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
			this.authenticated = authenticated;

		}

		public Collection<GrantedAuthority> getGrantedAuthorities() {
			return grantedAuthorities;
		}

		public void setGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities) {
			this.grantedAuthorities = grantedAuthorities;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCredentials(Object credentials) {
			this.credentials = credentials;
		}

		public void setDetails(Object details) {
			this.details = details;
		}

		public void setPrincipal(Object principal) {
			this.principal = principal;
		}
	}
}