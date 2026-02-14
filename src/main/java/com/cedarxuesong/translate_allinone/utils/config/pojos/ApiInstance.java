package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ApiInstance {

    @ConfigEntry.Gui.Tooltip
    public String name = "default";

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Provider llm_provider = Provider.OPENAI;

    public String baseUrl = "https://api.openai.com/v1";

    @ConfigEntry.Gui.Tooltip
    public String apiKey = "sk-xxxxxx";

    public String modelId = "gpt-4o";

    @ConfigEntry.Gui.Tooltip
    public String keep_alive_time = "1m";

    @ConfigEntry.Gui.Tooltip
    public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
}
