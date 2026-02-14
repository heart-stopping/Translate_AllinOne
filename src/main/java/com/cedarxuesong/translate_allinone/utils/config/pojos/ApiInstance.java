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

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public OpenaiApi openapi = new OpenaiApi();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public OllamaApi ollama = new OllamaApi();

    public static class OpenaiApi {
        @ConfigEntry.Gui.PrefixText
        public String baseUrl = "https://api.openai.com/v1";
        public String apiKey = "sk-xxxxxx";
        public String modelId = "gpt-4o";
        @ConfigEntry.Gui.Tooltip
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }

    public static class OllamaApi {
        @ConfigEntry.Gui.PrefixText
        public String ollamaUrl = "http://localhost:11434";
        public String modelId = "qwen3:0.6b";
        public String keep_alive_time = "1m";
        @ConfigEntry.Gui.Tooltip
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }
}
