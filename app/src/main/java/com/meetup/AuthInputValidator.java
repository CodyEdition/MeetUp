package com.meetup;

/**
 * Pure validation for login and sign-up forms. Used by activities and covered by JVM unit tests.
 */
public final class AuthInputValidator {

    /** Matches Firebase Auth minimum password length. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    private AuthInputValidator() {
    }

    public static LoginResult validateLogin(String email, String password) {
        String e = email != null ? email.trim() : "";
        String p = password != null ? password : "";
        return new LoginResult(e.isEmpty(), p.isEmpty());
    }

    public static SignUpResult validateSignUp(String username, String email, String password,
            String confirmPassword) {
        String u = username != null ? username.trim() : "";
        String e = email != null ? email.trim() : "";
        String p = password != null ? password : "";
        String c = confirmPassword != null ? confirmPassword : "";

        boolean usernameEmpty = u.isEmpty();
        boolean emailEmpty = e.isEmpty();
        boolean passwordEmpty = p.isEmpty();
        boolean passwordTooShort = !passwordEmpty && p.length() < MIN_PASSWORD_LENGTH;
        boolean confirmEmpty = c.isEmpty();
        boolean passwordsMismatch = !confirmEmpty && !p.equals(c);

        return new SignUpResult(usernameEmpty, emailEmpty, passwordEmpty, passwordTooShort,
                confirmEmpty, passwordsMismatch);
    }

    public static final class LoginResult {
        public final boolean emailEmpty;
        public final boolean passwordEmpty;

        LoginResult(boolean emailEmpty, boolean passwordEmpty) {
            this.emailEmpty = emailEmpty;
            this.passwordEmpty = passwordEmpty;
        }

        public boolean isValid() {
            return !emailEmpty && !passwordEmpty;
        }
    }

    public static final class SignUpResult {
        public final boolean usernameEmpty;
        public final boolean emailEmpty;
        public final boolean passwordEmpty;
        public final boolean passwordTooShort;
        public final boolean confirmPasswordEmpty;
        public final boolean passwordsMismatch;

        SignUpResult(boolean usernameEmpty, boolean emailEmpty, boolean passwordEmpty,
                boolean passwordTooShort, boolean confirmPasswordEmpty, boolean passwordsMismatch) {
            this.usernameEmpty = usernameEmpty;
            this.emailEmpty = emailEmpty;
            this.passwordEmpty = passwordEmpty;
            this.passwordTooShort = passwordTooShort;
            this.confirmPasswordEmpty = confirmPasswordEmpty;
            this.passwordsMismatch = passwordsMismatch;
        }

        public boolean isValid() {
            return !usernameEmpty && !emailEmpty && !passwordEmpty && !passwordTooShort
                    && !confirmPasswordEmpty && !passwordsMismatch;
        }
    }
}
