
package com.example.tab.util;

import com.example.tab.exception.BusinessException;
import com.example.tab.exception.BusinessException;

import cn.dev33.satoken.stp.StpUtil;

public class PermissionUtils {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentRole() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(401, "请先登录");
        }

        Object role = StpUtil.getSession().get("role");
        if (role == null) {
            throw new BusinessException(500, "用户角色信息不存在，请重新登录");
        }
        return role.toString();
    }

    /**
     * 是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return ROLE_SUPER_ADMIN.equals(getCurrentRole());
    }

    /**
     * 是否为教师
     */
    public static boolean isTeacher() {
        return ROLE_TEACHER.equals(getCurrentRole());
    }

    /**
     * 是否为教师或超级管理员
     */
    public static boolean isTeacherOrAbove() {
        String role = getCurrentRole();
        return ROLE_SUPER_ADMIN.equals(role)
                || ROLE_TEACHER.equals(role);
    }

    /**
     * 仅允许教师或超级管理员操作
     */
    public static void checkTeacherOrAdmin() {
        if (!isTeacherOrAbove()) {
            throw new BusinessException(403, "权限不足，仅教师或超级管理员可以操作");
        }
    }

    /**
     * 检查是否为学生
     */
    public static boolean isStudent() {
        return ROLE_STUDENT.equals(getCurrentRole());
    }

    /**
     * 检查是否为学生
     */
    public static void checkStudent() {
        if (!isStudent()) {
            throw new BusinessException(403, "权限不足，仅学生可以操作");
        }
    }

    /**
     * 校验资源归属
     * 超级管理员可以操作所有资源
     * 教师只能操作自己创建的资源
     */
    public static void checkOwnerOrAdmin(Long ownerUserId) {
        checkTeacherOrAdmin();

        Long currentUserId = StpUtil.getLoginIdAsLong();

        if (isSuperAdmin()) {
            return;
        }

        if (ownerUserId == null || !ownerUserId.equals(currentUserId)) {
            throw new BusinessException(403, "无权操作他人创建的资源");
        }
    }
}