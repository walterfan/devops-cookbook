package com.github.walterfan.kanban.domain;

/**
 * 
 * @author Shine
 */
public class SecurityOptions extends BaseObject {
    /**
     * DOCUMENT ME!
     */
    private long customerID;

    /**
     * DOCUMENT ME!
     */
    private int pwdMinLength;

    /**
     * DOCUMENT ME!
     */
    private int pwdMinNumber;

    /**
     * DOCUMENT ME!
     */
    private int pwdMinAlpha;

    /**
     * DOCUMENT ME!
     */
    private int pwdMinSpecialChars;

    /**
     * DOCUMENT ME!
     */
    private int pwdMixedCase;

    /**
     * DOCUMENT ME!
     */
    private String pwdForbiddenString;

    /**
     * DOCUMENT ME!
     */
    private int pwdForbiddenOld;

    /**
     * DOCUMENT ME!
     */
    private int pwdExpireDays;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedStrict;

    /**
     * DOCUMENT ME!
     */
    private int allowChangeUserName;

    /**
     * DOCUMENT ME!
     */
    private int lockFailedLogin;

    /**
     * DOCUMENT ME!
     */
    private int failedAttempts;

    /**
     * DOCUMENT ME!
     */
    private int reAttemptMinutes;

    /**
     * DOCUMENT ME!
     */
    private int accountExpireDays;

    /**
     * DOCUMENT ME!
     */
    private int viewNewPwdTimes;

    /**
     * DOCUMENT ME!
     */
    private int emailNoPassword;

    /**
     * DOCUMENT ME!
     */
    private int remindEmailDays;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedChange;

    /**
     * DOCUMENT ME!
     */
    private int accountDeactive;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedMinLength;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedMinNumber;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedMinAlpha;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedMinSpecialChars;

    /**
     * DOCUMENT ME!
     */
    private int pwdNeedForbiddenString;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (customerID ^ (customerID  >>> 32));

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SecurityOptions other = (SecurityOptions) obj;

        if (customerID != other.customerID) {
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String result = getClass().getName();
        result += ("[customerID=" + customerID + ",");
        result += (", pwdMinLength=" + pwdMinLength);
        result += (", PwdMinNumber=" + pwdMinNumber);
        result += (", PwdMinAlpha=" + pwdMinAlpha);
        result += (", PwdMinSpecialChars=" + pwdMinSpecialChars);
        result += (", PwdMixedCase=" + pwdMixedCase);
        result += (", PwdForbiddenString=" + pwdForbiddenString);
        result += (", PwdForbiddenOld=" + pwdForbiddenOld);
        result += (", PwdExpireDays=" + pwdExpireDays);
        result += (", PwdNeedStrict=" + pwdNeedStrict);
        result += (", allowChangeUserName=" + allowChangeUserName);
        result += (", LockFailedLogin=" + lockFailedLogin);
        result += (", FailedAttempts=" + failedAttempts);
        result += (", ReAttemptMinutes=" + reAttemptMinutes);
        result += (", AccountExpireDays=" + accountExpireDays);
        result += (", ViewNewPwdTimes=" + viewNewPwdTimes);
        result += (", EmailNoPassword=" + emailNoPassword);
        result += (", RemindEmailDays=" + remindEmailDays);
        result += (", AccountDeactive=" + accountDeactive);
        result += (", PwdNeedChange=" + pwdNeedChange);
        result += (", PwdNeedMinLength=" + pwdNeedMinLength);
        result += (", PwdNeedMinAlpha=" + pwdNeedMinAlpha);
        result += (", PwdNeedMinNumber=" + pwdNeedMinNumber);
        result += (", PwdNeedMinSpecialChars=" + pwdNeedMinSpecialChars);
        result += (", PwdNeedForbiddenString=" + pwdNeedForbiddenString);

        return result + "]";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getAccountDeactive() {
        return accountDeactive;
    }

    /**
     * DOCUMENT ME!
     *
     * @param accountDeactive DOCUMENT ME!
     */
    public void setAccountDeactive(int accountDeactive) {
        this.accountDeactive = accountDeactive;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getAccountExpireDays() {
        return accountExpireDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @param accountExpireDays DOCUMENT ME!
     */
    public void setAccountExpireDays(int accountExpireDays) {
        this.accountExpireDays = accountExpireDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getAllowChangeUserName() {
        return allowChangeUserName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param allowChangeUserName DOCUMENT ME!
     */
    public void setAllowChangeUserName(int allowChangeUserName) {
        this.allowChangeUserName = allowChangeUserName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getCustomerID() {
        return customerID;
    }

    /**
     * DOCUMENT ME!
     *
     * @param customerID DOCUMENT ME!
     */
    public void setCustomerID(long customerID) {
        this.customerID = customerID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getEmailNoPassword() {
        return emailNoPassword;
    }

    /**
     * DOCUMENT ME!
     *
     * @param emailNoPassword DOCUMENT ME!
     */
    public void setEmailNoPassword(int emailNoPassword) {
        this.emailNoPassword = emailNoPassword;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * DOCUMENT ME!
     *
     * @param failedAttempts DOCUMENT ME!
     */
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getLockFailedLogin() {
        return lockFailedLogin;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lockFailedLogin DOCUMENT ME!
     */
    public void setLockFailedLogin(int lockFailedLogin) {
        this.lockFailedLogin = lockFailedLogin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdExpireDays() {
        return pwdExpireDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdExpireDays DOCUMENT ME!
     */
    public void setPwdExpireDays(int pwdExpireDays) {
        this.pwdExpireDays = pwdExpireDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdForbiddenOld() {
        return pwdForbiddenOld;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdForbiddenOld DOCUMENT ME!
     */
    public void setPwdForbiddenOld(int pwdForbiddenOld) {
        this.pwdForbiddenOld = pwdForbiddenOld;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPwdForbiddenString() {
        return pwdForbiddenString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdForbiddenString DOCUMENT ME!
     */
    public void setPwdForbiddenString(String pwdForbiddenString) {
        this.pwdForbiddenString = pwdForbiddenString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdMinAlpha() {
        return pwdMinAlpha;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdMinAlpha DOCUMENT ME!
     */
    public void setPwdMinAlpha(int pwdMinAlpha) {
        this.pwdMinAlpha = pwdMinAlpha;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdMinLength() {
        return pwdMinLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdMinLength DOCUMENT ME!
     */
    public void setPwdMinLength(int pwdMinLength) {
        this.pwdMinLength = pwdMinLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdMinNumber() {
        return pwdMinNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdMinNumber DOCUMENT ME!
     */
    public void setPwdMinNumber(int pwdMinNumber) {
        this.pwdMinNumber = pwdMinNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdMinSpecialChars() {
        return pwdMinSpecialChars;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdMinSpecialChars DOCUMENT ME!
     */
    public void setPwdMinSpecialChars(int pwdMinSpecialChars) {
        this.pwdMinSpecialChars = pwdMinSpecialChars;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdMixedCase() {
        return pwdMixedCase;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdMixedCase DOCUMENT ME!
     */
    public void setPwdMixedCase(int pwdMixedCase) {
        this.pwdMixedCase = pwdMixedCase;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedChange() {
        return pwdNeedChange;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedChange DOCUMENT ME!
     */
    public void setPwdNeedChange(int pwdNeedChange) {
        this.pwdNeedChange = pwdNeedChange;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedForbiddenString() {
        return pwdNeedForbiddenString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedForbiddenString DOCUMENT ME!
     */
    public void setPwdNeedForbiddenString(int pwdNeedForbiddenString) {
        this.pwdNeedForbiddenString = pwdNeedForbiddenString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedMinAlpha() {
        return pwdNeedMinAlpha;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedMinAlpha DOCUMENT ME!
     */
    public void setPwdNeedMinAlpha(int pwdNeedMinAlpha) {
        this.pwdNeedMinAlpha = pwdNeedMinAlpha;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedMinLength() {
        return pwdNeedMinLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedMinLength DOCUMENT ME!
     */
    public void setPwdNeedMinLength(int pwdNeedMinLength) {
        this.pwdNeedMinLength = pwdNeedMinLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedMinNumber() {
        return pwdNeedMinNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedMinNumber DOCUMENT ME!
     */
    public void setPwdNeedMinNumber(int pwdNeedMinNumber) {
        this.pwdNeedMinNumber = pwdNeedMinNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedMinSpecialChars() {
        return pwdNeedMinSpecialChars;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedMinSpecialChars DOCUMENT ME!
     */
    public void setPwdNeedMinSpecialChars(int pwdNeedMinSpecialChars) {
        this.pwdNeedMinSpecialChars = pwdNeedMinSpecialChars;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPwdNeedStrict() {
        return pwdNeedStrict;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pwdNeedStrict DOCUMENT ME!
     */
    public void setPwdNeedStrict(int pwdNeedStrict) {
        this.pwdNeedStrict = pwdNeedStrict;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getReAttemptMinutes() {
        return reAttemptMinutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param reAttemptMinutes DOCUMENT ME!
     */
    public void setReAttemptMinutes(int reAttemptMinutes) {
        this.reAttemptMinutes = reAttemptMinutes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getRemindEmailDays() {
        return remindEmailDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @param remindEmailDays DOCUMENT ME!
     */
    public void setRemindEmailDays(int remindEmailDays) {
        this.remindEmailDays = remindEmailDays;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getViewNewPwdTimes() {
        return viewNewPwdTimes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param viewNewPwdTimes DOCUMENT ME!
     */
    public void setViewNewPwdTimes(int viewNewPwdTimes) {
        this.viewNewPwdTimes = viewNewPwdTimes;
    }
}
