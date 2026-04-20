package com.video.util;

import java.util.UUID;

public class TokenUtil {
    public static String generateToken(String username) {
        return username + "_" + UUID.randomUUID();
    }
}
