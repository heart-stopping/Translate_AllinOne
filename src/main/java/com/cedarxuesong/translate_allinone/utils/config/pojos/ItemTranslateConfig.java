package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ItemTranslateConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled_translate_item_custom_name = false;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled_translate_item_lore = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int max_concurrent_requests = 2;

    @ConfigEntry.Gui.Tooltip
    public int requests_per_minute = 60;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int max_batch_size = 10;

    @ConfigEntry.Gui.Tooltip
    public String target_language = "Chinese";

    @ConfigEntry.Gui.Tooltip
    public String api_instance_name = "default";


    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public KeybindingConfig keybinding = new KeybindingConfig();

    public double temperature = 0.7;
    @ConfigEntry.Gui.Tooltip
    public boolean enable_structured_output_if_available = false;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public String system_prompt = "Translate each JSON value to %target_language%. Return JSON only. Keep all keys unchanged. Preserve formatting and tokens exactly (e.g. §a §l §r <...> {...} %s %d %f \\n \\t URLs numbers). If unsure, keep the original value.";


    public enum KeybindingMode {
        HOLD_TO_TRANSLATE,
        HOLD_TO_SEE_ORIGINAL,
        DISABLED
    }

    public static class KeybindingConfig {
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip(count = 4)
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public KeybindingMode mode = KeybindingMode.DISABLED;
    }
}
