package com.morning.star.retail.shiro.credentials;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

import com.morning.star.retail.exception.LoginAccountErrorCode;

/**
 * 身份验证匹配（限制验证次数）
 *
 * @author jiangyf
 * @date 2017年7月28日 下午2:44:46
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {
    // 限制验证次数
    private static final int LIMIT_RETRY_COUNT = 5;
    private Cache<String, AtomicInteger> passwordRetryCache;

    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
        passwordRetryCache = cacheManager.getCache("passwordRetryCache");
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        String username = (String) token.getPrincipal();
        // retryCount + 1
        AtomicInteger retryCount = passwordRetryCache.get(username);
        if (retryCount == null) {
            retryCount = new AtomicInteger(0);
            passwordRetryCache.put(username, retryCount);
        }
        if (retryCount.incrementAndGet() >= LIMIT_RETRY_COUNT) {
            throw LoginAccountErrorCode.INPUT_PWD_OUT_LIMIT.build();
        }
        // 身份验证成功
        boolean matches = super.doCredentialsMatch(token, info);
        if (matches) {
            // 清除请求登录重试记录
            passwordRetryCache.remove(username);
        }
        return matches;
    }
}
