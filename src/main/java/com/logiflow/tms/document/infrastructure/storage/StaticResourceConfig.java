package com.logiflow.tms.document.infrastructure.storage;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sert les fichiers stockés localement sous {@code /fichiers/**} (protégé comme tout endpoint). */
@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {

  private final StorageProperties storageProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String emplacement = Path.of(storageProperties.localPath()).toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/fichiers/**").addResourceLocations(emplacement);
  }
}
