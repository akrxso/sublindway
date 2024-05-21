package com.seohyun.sublindwaya;

public class UserManager {
    private static UserManager instance;
    private String userId;
    private boolean isLoggedIn = false;

    private UserManager() {}  // Private constructor to prevent instantiation

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void clear() {
        userId = null;
        isLoggedIn = false;
    }
}

