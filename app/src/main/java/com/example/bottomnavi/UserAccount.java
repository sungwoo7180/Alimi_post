package com.example.bottomnavi;
//사용자 계정 정보 모델 클래스

public class UserAccount {

    private String idToken;         //Firebase Uid (고유 토큰정보)
    private String emailId;
    private String password;
    private String nickname;

    public UserAccount() {}

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String emailId) {
        this.password = password;
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname;}
}
