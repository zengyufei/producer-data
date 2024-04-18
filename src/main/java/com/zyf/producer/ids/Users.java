package com.zyf.producer.ids;

import cn.hutool.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class Users {

    public static List<String> userIds = new ArrayList<>();

    public static List<String> userNos = new ArrayList<>();

    static {
        for (int i = 1; i < 1000; i++) {
            userIds.add(String.valueOf(i));
            userNos.add(String.valueOf(i));
        }
    }

    public static String randomUserId() {
        return RandomUtil.randomEle(Users.userIds);
    }

    public static String randomUserNo() {
        return RandomUtil.randomEle(Users.userNos);
    }
}
