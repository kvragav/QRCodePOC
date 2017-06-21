package com.survey.glance.core.web.service;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class AuthenticationSuccessHandlerImpl implements
		AuthenticationSuccessHandler {

	protected Logger logger = LoggerFactory
			.getLogger(AuthenticationSuccessHandlerImpl.class);

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request,
			final HttpServletResponse response,
			final Authentication authentication) throws IOException {

		handle(request, response, authentication);
		clearAuthenticationAttributes(request);
	}

	protected void handle(final HttpServletRequest request,
			final HttpServletResponse response,
			final Authentication authentication) throws IOException {
		
		final String targetUrl = determineTargetUrl(authentication);
		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to "
					+ targetUrl);
			return;
		}

		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(final Authentication authentication) {
		String targetUrl = "/login";
		if (authentication != null ) {
			final Collection<? extends GrantedAuthority> authorities = authentication
					.getAuthorities();
			for (final GrantedAuthority grantedAuthority : authorities) {
				if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
					targetUrl = "/welcome";
					break;
				} else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
					targetUrl = "/admin";
					break;
				} else if (grantedAuthority.getAuthority().equals("ROLE_DBA")) {
					targetUrl = "/dba";
					break;
				}
			}
		}
		return targetUrl;
	}

	/**
	 * @param request
	 */
	protected void clearAuthenticationAttributes(
			final HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}

	/**
	 * @param redirectStrategy
	 */
	public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	/**
	 * @return
	 */
	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}
}
