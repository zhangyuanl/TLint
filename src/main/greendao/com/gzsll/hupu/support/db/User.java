package com.gzsll.hupu.support.db;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table USER.
 */
public class User {

    private Long id;
    private String userName;
    private String uid;
    private String token;
    private String icon;
    private Integer syncTime;
    private Integer sex;
    private Integer level;
    private Boolean isLogin;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public User(Long id, String userName, String uid, String token, String icon, Integer syncTime, Integer sex, Integer level, Boolean isLogin) {
        this.id = id;
        this.userName = userName;
        this.uid = uid;
        this.token = token;
        this.icon = icon;
        this.syncTime = syncTime;
        this.sex = sex;
        this.level = level;
        this.isLogin = isLogin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Integer syncTime) {
        this.syncTime = syncTime;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(Boolean isLogin) {
        this.isLogin = isLogin;
    }

}
