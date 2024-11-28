package ru.t1.java.demo.config;

import net.javacrumbs.shedlock.core.AbstractSimpleLock;
import net.javacrumbs.shedlock.core.ClockProvider;
import net.javacrumbs.shedlock.core.ExtensibleLockProvider;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.support.LockException;
import net.javacrumbs.shedlock.support.annotation.NonNull;

import java.time.Duration;
import java.util.Optional;

import static net.javacrumbs.shedlock.support.Utils.getHostname;
import static net.javacrumbs.shedlock.support.Utils.toIsoString;
import static redis.clients.jedis.params.SetParams.setParams;

public class JedisLockProvider implements ExtensibleLockProvider {

    private static final String KEY_PREFIX = "job-lock";

    private final JedisClient jedisClient;

    public JedisLockProvider(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    @Override
    public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
        long expireTime = Duration.between(ClockProvider.now(), lockConfiguration.getLockAtLeastUntil()).toMillis();
        String key = "%s:%s".formatted(KEY_PREFIX, lockConfiguration.getName());
        String rez = jedisClient.set(key, buildValue(), setParams().nx().px(expireTime));

        if ("OK".equals(rez)) {
            return Optional.of(new RedisLock(key, this, lockConfiguration));
        }

        return Optional.empty();
    }

    private Optional<SimpleLock> extend(LockConfiguration lockConfiguration) {
        long expireTime = Duration.between(ClockProvider.now(), lockConfiguration.getLockAtLeastUntil()).toMillis();
        String key = "%s:%s".formatted(KEY_PREFIX, lockConfiguration.getName());
        String rez = jedisClient.set(key, buildValue(), setParams().xx().px(expireTime));

        if ("OK".equals(rez)) {
            return Optional.of(new RedisLock(key, this, lockConfiguration));
        }

        return Optional.empty();
    }

    private void extendKeyExpiration(String key, long expiration) {
        jedisClient.set(key, buildValue(), setParams().xx().px(expiration));
    }

    private void deleteKey(String key) {
        jedisClient.del(key);
    }

    private static String buildValue() {
        return "ADDED:%s@%s".formatted(toIsoString(ClockProvider.now()), getHostname());
    }

    private static final class RedisLock extends AbstractSimpleLock {
        private final String key;
        private final JedisLockProvider jedisLockProvider;

        private RedisLock(String key, JedisLockProvider jedisLockProvider, LockConfiguration lockConfiguration) {
            super(lockConfiguration);
            this.key = key;
            this.jedisLockProvider = jedisLockProvider;
        }

        @Override
        public void doUnlock() {
            long keepLockFor = Duration.between(ClockProvider.now(), lockConfiguration.getLockAtLeastUntil()).toMillis();

            if (keepLockFor <= 0) {
                try {
                    jedisLockProvider.deleteKey(key);
                } catch (Exception e) {
                    throw new LockException("Can not remove node", e);
                }
            } else {
                jedisLockProvider.extendKeyExpiration(key, keepLockFor);
            }
        }

        @Override
        @NonNull
        protected Optional<SimpleLock> doExtend(@NonNull LockConfiguration newConfiguration) {
            return jedisLockProvider.extend(newConfiguration);
        }
    }
}
