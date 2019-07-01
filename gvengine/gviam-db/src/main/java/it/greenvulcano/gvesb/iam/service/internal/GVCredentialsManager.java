package it.greenvulcano.gvesb.iam.service.internal;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

<<<<<<< Updated upstream
=======
import org.apache.commons.codec.digest.DigestUtils;
import org.osgi.framework.ServiceReference;

>>>>>>> Stashed changes
import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.jpa.CredentialsJPA;
import it.greenvulcano.gvesb.iam.domain.jpa.UserJPA;
import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.repository.hibernate.CredentialsRepositoryHibernate;
import it.greenvulcano.gvesb.iam.service.CredentialsManager;
import it.greenvulcano.gvesb.iam.service.ExternalAuthenticationService;
import it.greenvulcano.gvesb.iam.service.ExternalAutheticatedUser;
import it.greenvulcano.gvesb.iam.service.UsersManager;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoHelperException;
import it.greenvulcano.util.crypto.CryptoUtilsException;

public class GVCredentialsManager implements CredentialsManager {

    private final static String TOKEN_PATTERN = "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x";

    private final SecureRandom secureRandom = new SecureRandom();

<<<<<<< Updated upstream
    private UsersManager usersManager;
    private CredentialsRepositoryHibernate credentialsRepository;
  
    private long tokenLifeTime = 24 * 60 * 60 * 1000;
    private int maxExpiredTokens = 2;
=======
    private List<ServiceReference<ExternalAuthenticationService>> externalAuthenticationServices;

    private UsersManager usersManager;
    private CredentialsRepositoryHibernate credentialsRepository;

    private long tokenLifeTime = 24 * 60 * 60 * 1000;

    public void setExternalCredentialsManagers(List<ServiceReference<ExternalAuthenticationService>> externalAuthenticationServices) {

        this.externalAuthenticationServices = externalAuthenticationServices;
    }
>>>>>>> Stashed changes

    public void setUsersManager(UsersManager usersManager) {

        this.usersManager = usersManager;
    }

    public void setCredentialsRepository(CredentialsRepositoryHibernate credentialsRepository) {

        this.credentialsRepository = credentialsRepository;
    }

    public void setTokenLifeTime(long tokenLifeTime) {

        this.tokenLifeTime = tokenLifeTime;
    }
<<<<<<< Updated upstream
    
    
    public void setMaxExpiredTokens(int maxExpiredTokens) {

        this.maxExpiredTokens = maxExpiredTokens;
    }
=======
>>>>>>> Stashed changes

    @Override
    public Credentials create(String username, String password, String clientUsername, String clientPassword)
            throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, UnverifiableUserException {

        User client = usersManager.validateUser(clientUsername, clientPassword);
        User resourceOwner = usersManager.validateUser(username, password);

        CredentialsJPA credentials = new CredentialsJPA();
        credentials.setClient(UserJPA.class.cast(client));
        credentials.setResourceOwner(UserJPA.class.cast(resourceOwner));

        String accessToken = generateToken();
<<<<<<< Updated upstream
        String refreshToken = generateToken();
        
        credentials.setAccessToken(encryptToken(accessToken));
        credentials.setRefreshToken(encryptToken(refreshToken));
=======

        String refreshToken = generateToken();

        credentials.setAccessToken(DigestUtils.sha256Hex(accessToken));
        credentials.setRefreshToken(DigestUtils.sha256Hex(refreshToken));
>>>>>>> Stashed changes

        credentials.setIssueTime(new Date());
        credentials.setLifeTime(tokenLifeTime);

        credentialsRepository.add(credentials);

        credentials.setAccessToken(accessToken);
        credentials.setRefreshToken(refreshToken);

        return credentials;
    }

    @Override
<<<<<<< Updated upstream
    public Credentials check(String accessToken) throws InvalidCredentialsException, CredentialsExpiredException {

        Credentials credentials = credentialsRepository.get(encryptToken(accessToken)).orElseThrow(InvalidCredentialsException::new);

        long tokenLife = System.currentTimeMillis() - credentials.getIssueTime().getTime();
        if (tokenLife > credentials.getLifeTime()) {
            throw new CredentialsExpiredException();
        }

        return credentials;
    }

    @Override
    public Credentials refresh(String refreshToken, String accessToken) throws InvalidCredentialsException {

        Credentials lastCredentials = credentialsRepository.get(encryptToken(accessToken)).orElseThrow(InvalidCredentialsException::new);

        if (lastCredentials.getRefreshToken().equals(encryptToken(refreshToken))) {

            /*
             * Exprired credentilas can be used multiple times, returning last valid token
             */
            Set<Credentials> userCredentials = credentialsRepository.getUserCredentials(lastCredentials.getResourceOwner().getUsername());

            // find current valid credentials
            CredentialsJPA credentials = userCredentials.stream()
                                                        .filter(Credentials::isValid)
                                                        .filter(existing -> !existing.equals(lastCredentials))                                                     
                                                        .map(CredentialsJPA.class::cast)
                                                        .findFirst()
                                                        .orElseGet(() -> {

                                                            CredentialsJPA newcredentials = new CredentialsJPA();
                                                            newcredentials.setClient(UserJPA.class.cast(lastCredentials.getClient()));
                                                            newcredentials.setResourceOwner(UserJPA.class.cast(lastCredentials.getResourceOwner()));

                                                            String newAccessToken = generateToken();
                                                            String newRefreshToken = generateToken();

                                                            newcredentials.setAccessToken(encryptToken(newAccessToken));
                                                            newcredentials.setRefreshToken(encryptToken(newRefreshToken));

                                                            newcredentials.setIssueTime(new Date());
                                                            newcredentials.setLifeTime(tokenLifeTime);

                                                            credentialsRepository.add(newcredentials);

                                                            return newcredentials;

                                                        });

            String plainAccessToken = decryptToken(credentials.getAccessToken());
            String plainRefreshToken = decryptToken(credentials.getRefreshToken());

            credentials.setAccessToken(plainAccessToken);
            credentials.setRefreshToken(plainRefreshToken);

            while (userCredentials.stream().filter(c -> !c.isValid()).count() > maxExpiredTokens) {

                Credentials expired = userCredentials.stream().filter(c -> !c.isValid()).sorted((c1, c2) -> c1.getIssueTime().compareTo(c2.getIssueTime())).findFirst().get();
                userCredentials.remove(expired);
                credentialsRepository.remove(expired);

            }
=======
    public Credentials create(String username, String provider) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, UnverifiableUserException {

        ExternalAuthenticationService authenticator = externalAuthenticationServices.stream()
                                                                                    .map(sr -> sr.getBundle().getBundleContext().getService(sr))
                                                                                    .filter(m -> m.getProviderID().equals(provider))
                                                                                    .findFirst()
                                                                                    .orElseThrow(() -> new IllegalArgumentException("Provider not supported: " + provider));

        ExternalAutheticatedUser extUser = authenticator.authenticate(username);

        User internalUser = null;

        try {
            internalUser = usersManager.createUser(extUser.getEmail(), generateToken());
        } catch (UserExistException e) {
            internalUser = usersManager.getUser(extUser.getEmail());
        } catch (InvalidUsernameException | InvalidPasswordException e) {
            throw new UnverifiableUserException(username);
        }

        CredentialsJPA credentials = new CredentialsJPA();
        credentials.setClient(UserJPA.class.cast(internalUser));
        credentials.setResourceOwner(UserJPA.class.cast(internalUser));

        String accessToken = generateToken();

        String refreshToken = generateToken();

        credentials.setAccessToken(DigestUtils.sha256Hex(accessToken));
        credentials.setRefreshToken(DigestUtils.sha256Hex(refreshToken));

        credentials.setIssueTime(new Date());
        credentials.setLifeTime(tokenLifeTime);

        credentialsRepository.add(credentials);

        credentials.setAccessToken(accessToken);
        credentials.setRefreshToken(refreshToken);

        return credentials;
    }

    @Override
    public Credentials check(String accessToken) throws InvalidCredentialsException, CredentialsExpiredException {

        Credentials credentials = credentialsRepository.get(DigestUtils.sha256Hex(accessToken)).orElseThrow(InvalidCredentialsException::new);

        long tokenLife = System.currentTimeMillis() - credentials.getIssueTime().getTime();
        if (tokenLife > credentials.getLifeTime()) {
            throw new CredentialsExpiredException();
        }

        return credentials;
    }

    @Override
    public Credentials refresh(String refreshToken, String accessToken) throws InvalidCredentialsException {

        Credentials lastCredentials = credentialsRepository.get(DigestUtils.sha256Hex(accessToken)).orElseThrow(InvalidCredentialsException::new);

        if (lastCredentials.getRefreshToken().equals(DigestUtils.sha256Hex(refreshToken))) {
            CredentialsJPA credentials = new CredentialsJPA();
            credentials.setClient(UserJPA.class.cast(lastCredentials.getClient()));
            credentials.setResourceOwner(UserJPA.class.cast(lastCredentials.getResourceOwner()));

            String newAccessToken = generateToken();
            String newRefreshToken = generateToken();

            credentials.setAccessToken(DigestUtils.sha256Hex(newAccessToken));
            credentials.setRefreshToken(DigestUtils.sha256Hex(newRefreshToken));

            credentials.setIssueTime(new Date());
            credentials.setLifeTime(tokenLifeTime);

            credentialsRepository.add(credentials);

            credentialsRepository.remove(lastCredentials);

            credentials.setAccessToken(newAccessToken);
            credentials.setRefreshToken(newRefreshToken);
>>>>>>> Stashed changes

            return credentials;
        } else {
            throw new InvalidCredentialsException();
        }

    }

<<<<<<< Updated upstream
    private String encryptToken(String token) {

        try {
            return CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, token, false);
        } catch (CryptoHelperException | CryptoUtilsException e) {
            throw new IllegalArgumentException("Encryption failed", e);
        }
    }

    private String decryptToken(String token) {
        try {
            return CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, token, true);
        } catch (CryptoHelperException | CryptoUtilsException e) {
            throw new IllegalArgumentException("Encryption failed", e);
        }
    }

=======
>>>>>>> Stashed changes
    private String generateToken() {

        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        return String.format(Locale.US, TOKEN_PATTERN, IntStream.range(0, token.length).mapToObj(i -> Byte.valueOf(token[i])).toArray());
    }

}
