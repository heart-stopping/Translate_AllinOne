package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ChatTranslateConfig {

    @ConfigEntry.Gui.CollapsibleObject
    public ChatOutputTranslateConfig output = new ChatOutputTranslateConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public ChatInputTranslateConfig input = new ChatInputTranslateConfig();

    public static class ChatOutputTranslateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean auto_translate = false;

        @ConfigEntry.Gui.Tooltip
        public String target_language = "Chinese";


        @ConfigEntry.Gui.Tooltip
        public boolean streaming_response = false;

        @ConfigEntry.Gui.Tooltip
        public String api_instance_name = "default";

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int max_concurrent_requests = 1;

        public double temperature = 0.7;
        @ConfigEntry.Gui.Tooltip
        public boolean enable_structured_output_if_available = false;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public String system_prompt = "You are a chat translation assistant, translating text into %target_language%. You will receive text with style tags, such as `s0>text</s0>`. Please keep these tags wrapping the translated text paragraphs. For example, `<s0>Hello</s0> world` translated into French is `<s0>Bonjour</s0> le monde`. Only output the translation result, keeping all formatting characters, and keeping all words that are uncertain to translate.";
    }

    public static class ChatInputTranslateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public String target_language = "English";

        @ConfigEntry.Gui.Tooltip
        public boolean streaming_response = false;

        @ConfigEntry.Gui.Tooltip
        public String api_instance_name = "default";

        public double temperature = 0.7;
        @ConfigEntry.Gui.Tooltip
        public boolean enable_structured_output_if_available = false;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public String system_prompt = "You are a chat translation assistant, translating user input text into %target_language%. Only output the translation result.";
    }
} 
