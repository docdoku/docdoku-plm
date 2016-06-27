package com.docdoku.server.filters;

import com.docdoku.core.common.Account;
import com.docdoku.core.exceptions.AccountNotFoundException;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.server.jwt.RsaJsonWebKeyFactory;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JwtFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(JwtFilter.class.getName());

    @Inject
    private IAccountManagerLocal accountManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(!FilterUtils.isAuthenticated(request)) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String authHeaderVal = httpRequest.getHeader("Authorization");

            if (authHeaderVal != null && authHeaderVal.startsWith("Bearer")) {
                String authorization = httpRequest.getHeader("Authorization");
                String[] splitAuthorization = authorization.split(" ");
                if (splitAuthorization.length == 2) {
                    String jwt = splitAuthorization[1];
                    Account account = validateToken(jwt);
                    if (account != null) {
                        // TODO log user in : login module ?
                        httpRequest.getSession();
                        FilterUtils.authenticate(request);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    private Account validateToken(String jwt) {
        RsaJsonWebKey rsaJsonWebKey = RsaJsonWebKeyFactory.createKey();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireSubject()
                .setVerificationKey(rsaJsonWebKey.getKey())
                .build();

        Account account = null;

        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            String userLogin = (String) jwtClaims.getClaimValue("sub");
            account = accountManager.getAccount(userLogin);
        } catch (InvalidJwtException e) {
            LOGGER.log(Level.SEVERE,null,e);
        } catch (AccountNotFoundException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }

        return account;

    }
}
