package com.sunbox.annotation;

import com.sunbox.configuration.AzureKeyVaultConfiguration;

import com.sunbox.util.KeyVaultUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({AzureKeyVaultConfiguration.class, KeyVaultUtil.class})
public @interface EnableKeyVault {
}
