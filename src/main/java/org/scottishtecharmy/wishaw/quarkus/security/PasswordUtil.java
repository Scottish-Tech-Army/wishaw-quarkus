package org.scottishtecharmy.wishaw.quarkus.security;

import io.quarkus.elytron.security.common.BcryptUtil;

/**
 * Utility class for hashing passwords using bcrypt.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hash a plain-text password using bcrypt.
     */
    public static String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }
}
