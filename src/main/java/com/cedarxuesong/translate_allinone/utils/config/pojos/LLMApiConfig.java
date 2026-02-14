package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class LLMApiConfig {

    @ConfigEntry.Gui.Tooltip
    public List<ApiInstance> api_instances = new ArrayList<>(List.of(new ApiInstance()));

    /**
     * Finds an API instance by its name.
     * Returns the first instance if no match is found, or a default instance if the list is empty.
     */
    public ApiInstance findByName(String name) {
        if (name == null || name.isEmpty()) {
            return getDefaultInstance();
        }
        for (ApiInstance instance : api_instances) {
            if (name.equals(instance.name)) {
                return instance;
            }
        }
        return getDefaultInstance();
    }

    private ApiInstance getDefaultInstance() {
        if (api_instances != null && !api_instances.isEmpty()) {
            return api_instances.get(0);
        }
        return new ApiInstance();
    }
}
