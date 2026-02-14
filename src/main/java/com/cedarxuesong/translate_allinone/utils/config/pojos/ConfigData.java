package com.cedarxuesong.translate_allinone.utils.config.pojos;

import com.google.gson.annotations.SerializedName;

public class ConfigData {
    @SerializedName("LLMApiConfig")
    public LLMApiConfig llmApiConfig = new LLMApiConfig();

    @SerializedName("ChatTranslateConfig")
    public ChatTranslateConfig chatTranslateConfig = new ChatTranslateConfig();

    @SerializedName("ItemTranslateConfig")
    public ItemTranslateConfig itemTranslateConfig = new ItemTranslateConfig();

    @SerializedName("ScoreboardConfig")
    public ScoreboardConfig scoreboardConfig = new ScoreboardConfig();
}
