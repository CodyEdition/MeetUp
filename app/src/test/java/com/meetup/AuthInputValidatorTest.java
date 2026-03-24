package com.meetup;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthInputValidatorTest {

    @Test
    public void validateLogin_bothFilled_isValid() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin("a@b.com", "secret");
        assertTrue(r.isValid());
        assertFalse(r.emailEmpty);
        assertFalse(r.passwordEmpty);
    }

    @Test
    public void validateLogin_emptyEmail_invalid() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin("", "secret");
        assertFalse(r.isValid());
        assertTrue(r.emailEmpty);
        assertFalse(r.passwordEmpty);
    }

    @Test
    public void validateLogin_whitespaceEmail_invalid() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin("   ", "secret");
        assertFalse(r.isValid());
        assertTrue(r.emailEmpty);
    }

    @Test
    public void validateLogin_emptyPassword_invalid() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin("a@b.com", "");
        assertFalse(r.isValid());
        assertFalse(r.emailEmpty);
        assertTrue(r.passwordEmpty);
    }

    @Test
    public void validateLogin_nullEmailTreatedAsEmpty() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin(null, "x");
        assertTrue(r.emailEmpty);
    }

    @Test
    public void validateLogin_nullPasswordTreatedAsEmpty() {
        AuthInputValidator.LoginResult r = AuthInputValidator.validateLogin("a@b.com", null);
        assertTrue(r.passwordEmpty);
    }

    @Test
    public void validateSignUp_allValid_isValid() {
        AuthInputValidator.SignUpResult r = AuthInputValidator.validateSignUp(
                "user", "a@b.com", "123456", "123456");
        assertTrue(r.isValid());
    }

    @Test
    public void validateSignUp_emptyUsername_invalid() {
        AuthInputValidator.SignUpResult r = AuthInputValidator.validateSignUp(
                "", "a@b.com", "123456", "123456");
        assertFalse(r.isValid());
        assertTrue(r.usernameEmpty);
    }

    @Test
    public void validateSignUp_passwordTooShort_invalid() {
        AuthInputValidator.SignUpResult r = AuthInputValidator.validateSignUp(
                "user", "a@b.com", "12345", "12345");
        assertFalse(r.isValid());
        assertTrue(r.passwordTooShort);
    }

    @Test
    public void validateSignUp_passwordMismatch_invalid() {
        AuthInputValidator.SignUpResult r = AuthInputValidator.validateSignUp(
                "user", "a@b.com", "123456", "654321");
        assertFalse(r.isValid());
        assertTrue(r.passwordsMismatch);
    }

    @Test
    public void validateSignUp_emptyConfirm_invalid() {
        AuthInputValidator.SignUpResult r = AuthInputValidator.validateSignUp(
                "user", "a@b.com", "123456", "");
        assertFalse(r.isValid());
        assertTrue(r.confirmPasswordEmpty);
    }
}
