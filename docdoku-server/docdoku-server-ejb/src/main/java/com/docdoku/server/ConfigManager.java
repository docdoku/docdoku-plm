package com.docdoku.server;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

/**
 * Get config from resources
 *
 * @author: Morgan Guimard
 */
@ApplicationScoped
public class ConfigManager {

    @Resource(name="docdokuplm.config")
    private Properties properties;

    public String getCodebase(){
        return String.valueOf(properties.get("codebase"));
    }

    public String getVaultPath(){
        return String.valueOf(properties.get("vaultPath"));
    }

}
