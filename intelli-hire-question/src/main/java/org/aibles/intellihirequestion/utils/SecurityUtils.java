package org.aibles.intellihirequestion.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {
    public static String getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String userFromHeader = attributes.getRequest().getHeader("X-User-Name");
            return (userFromHeader != null) ? userFromHeader : "SYSTEM";
        }
        return "SYSTEM";
    }
}