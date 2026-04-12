package com.example.development_01.core.data.firebase;

/**
 * Interface for authentication data access.
 * Defines contract for any authentication repository implementation.
 * Used for testability and dependency injection.
 */
public interface AuthRepository {

    /**
     * Checks if email exists in the authentication database.
     *
     * @param email the email address to check
     * @return true if email exists, false otherwise
     */
    //boolean emailExists(String email);

    /**
     * Retrieves the stored password for given email.
     *
     * @param email the email address
     * @return stored password or null if email not found
     */
    //String getPasswordForEmail(String email);
}
