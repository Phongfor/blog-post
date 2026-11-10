package org.example.blogpost.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CacheErrorHandlerConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler(){
        return new SimpleCacheErrorHandler(){
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key){
                log.warn("Cache get failed ({}::{}), falling back to database: {}", cache.getName(), key, e.toString());
            }
            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put failed ({}::{}): {}", cache.getName(), key, e.toString());
            }
        };
    }
}
