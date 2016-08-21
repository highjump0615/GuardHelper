package com.highjump.guardhelper.model;

/**
 * Created by Administrator on 2016/7/28.
 */
public class UserData {

    // 特殊账号
    public static String DEFAULT_USER = "khjy";

    // 用户名
    private String mStrUsername;

    // 保存实例, 后来用于获取当前用户
    private static UserData mInstance = null;

    public UserData(String username) {
        mStrUsername = username;

        mInstance = this;
    }

    public String getUsername() {
        return mStrUsername;
    }

    /**
     * 获取当前用户
     * @return - 用户模型实例
     */
    public static UserData currentUser() {
//        if (mInstance == null) {
//            new UserData("测试");
//        }

        return mInstance;
    }
}
