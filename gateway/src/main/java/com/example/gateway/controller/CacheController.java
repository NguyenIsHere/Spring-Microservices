package com.example.gateway.controller;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class CacheController {

  @Autowired
  private CacheManager cacheManager;

  @SuppressWarnings("unchecked")
  @GetMapping("/cache/keys")
  public Set<Object> getCacheKeys() {
    @SuppressWarnings("rawtypes")
    Cache cache = (Cache) cacheManager.getCache("gateway-cache").getNativeCache();
    return cache.asMap().keySet();
  }

  @SuppressWarnings("unchecked")
  @GetMapping("/cache/values")
  public Set<Object> getCacheValues() {
    @SuppressWarnings("rawtypes")
    Cache cache = (Cache) cacheManager.getCache("gateway-cache").getNativeCache();
    return Set.copyOf(cache.asMap().values());
  }
}
