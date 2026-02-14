package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiInstance;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ChatInputTranslateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatInputTranslateManager.class);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Chat-Input-Translate-Thread");
        t.setDaemon(true);
        return t;
    });

    private static final AtomicBoolean isTranslating = new AtomicBoolean(false);
    private static final AtomicReference<String> originalTextRef = new AtomicReference<>("");

    /**
     * Intercepts an outgoing chat message for automatic translation before sending.
     * Called from the ChatScreenMixin when the user presses Enter to send a message.
     *
     * @param chatText the message text the user is sending
     * @param sendCallback callback to invoke on the main thread with the translated text
     * @return true if the message was intercepted for translation (caller should cancel the original send)
     */
    public static boolean interceptAndTranslate(String chatText, Consumer<String> sendCallback) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.chatTranslate.input.enabled) {
            return false;
        }

        if (chatText.trim().isEmpty() || chatText.startsWith("/")) {
            return false;
        }

        if (!isTranslating.compareAndSet(false, true)) {
            return false;
        }

        executor.submit(() -> {
            try {
                ChatTranslateConfig.ChatInputTranslateConfig inputConfig = config.chatTranslate.input;
                ApiInstance apiInstance = config.llmApi.findByName(inputConfig.api_instance_name);
                ProviderSettings settings = ProviderSettings.fromApiInstance(apiInstance, inputConfig.temperature, inputConfig.enable_structured_output_if_available);
                LLM llm = new LLM(settings);
                List<OpenAIRequest.Message> apiMessages = getMessages(inputConfig, chatText);

                String result;
                if (inputConfig.streaming_response) {
                    final StringBuilder visibleContentBuffer = new StringBuilder();
                    final StringBuilder rawResponseBuffer = new StringBuilder();
                    final AtomicBoolean inThinkTag = new AtomicBoolean(false);

                    llm.getStreamingCompletion(apiMessages).forEach(chunk -> {
                        rawResponseBuffer.append(chunk);
                        while (true) {
                            if (inThinkTag.get()) {
                                int endTagIndex = rawResponseBuffer.indexOf("</think>");
                                if (endTagIndex != -1) {
                                    inThinkTag.set(false);
                                    rawResponseBuffer.delete(0, endTagIndex + "</think>".length());
                                    continue;
                                }
                                break;
                            } else {
                                int startTagIndex = rawResponseBuffer.indexOf("<think>");
                                if (startTagIndex != -1) {
                                    visibleContentBuffer.append(rawResponseBuffer.substring(0, startTagIndex));
                                    rawResponseBuffer.delete(0, startTagIndex + "<think>".length());
                                    inThinkTag.set(true);
                                    continue;
                                } else {
                                    visibleContentBuffer.append(rawResponseBuffer.toString());
                                    rawResponseBuffer.setLength(0);
                                    break;
                                }
                            }
                        }
                    });
                    result = visibleContentBuffer.toString().stripLeading();
                } else {
                    result = llm.getCompletion(apiMessages).join().stripLeading();
                }

                final String finalTranslation = result;
                LOGGER.info("[Chat-Input-Translate] Translated '{}' -> '{}'", chatText, finalTranslation);

                MinecraftClient.getInstance().execute(() -> sendCallback.accept(finalTranslation));
            } catch (Exception e) {
                LOGGER.error("[Chat-Input-Translate] Exception during send-time translation", e);
                MinecraftClient.getInstance().execute(() -> {
                    Text errorMessage = Text.literal("Chat Input Translation Error: " + e.getMessage()).formatted(Formatting.RED);
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(errorMessage);
                    // Send the original untranslated message so the user's message isn't lost
                    sendCallback.accept(chatText);
                });
            } finally {
                isTranslating.set(false);
            }
        });

        return true;
    }

    public static void translate(TextFieldWidget chatField) {
        if (!isTranslating.compareAndSet(false, true)) {
            return; // Already translating
        }

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.chatTranslate.input.enabled) {
            isTranslating.set(false);
            return;
        }

        String currentText = chatField.getText();
        if (currentText.trim().isEmpty()) {
            isTranslating.set(false);
            return;
        }
        originalTextRef.set(currentText);


        executor.submit(() -> {
            try {
                ChatTranslateConfig.ChatInputTranslateConfig inputConfig = config.chatTranslate.input;
                ApiInstance apiInstance = config.llmApi.findByName(inputConfig.api_instance_name);
                ProviderSettings settings = ProviderSettings.fromApiInstance(apiInstance, inputConfig.temperature, inputConfig.enable_structured_output_if_available);
                LLM llm = new LLM(settings);
                List<OpenAIRequest.Message> apiMessages = getMessages(inputConfig, originalTextRef.get());

                if (inputConfig.streaming_response) {
                    final StringBuilder rawResponseBuffer = new StringBuilder();
                    final StringBuilder visibleContentBuffer = new StringBuilder();
                    final AtomicBoolean inThinkTag = new AtomicBoolean(false);

                    MinecraftClient.getInstance().execute(() -> chatField.setText("Connecting...")); // Clear field for streaming

                    llm.getStreamingCompletion(apiMessages).forEach(chunk -> {
                        rawResponseBuffer.append(chunk);

                        while (true) {
                            if (inThinkTag.get()) {
                                int endTagIndex = rawResponseBuffer.indexOf("</think>");
                                if (endTagIndex != -1) {
                                    inThinkTag.set(false);
                                    rawResponseBuffer.delete(0, endTagIndex + "</think>".length());

                                    // Restore the visible content so far
                                    String currentTranslation = visibleContentBuffer.toString().stripLeading();
                                    MinecraftClient.getInstance().execute(() -> {
                                        chatField.setText(currentTranslation);
                                        chatField.setCursor(currentTranslation.length(), false);
                                    });
                                    continue; // Check for more tags in the same chunk
                                }
                                break; // Incomplete tag, wait for more chunks
                            } else {
                                int startTagIndex = rawResponseBuffer.indexOf("<think>");
                                if (startTagIndex != -1) {
                                    // Found a think tag. Append content before it to visible buffer.
                                    String translationPart = rawResponseBuffer.substring(0, startTagIndex);
                                    visibleContentBuffer.append(translationPart);
                                    rawResponseBuffer.delete(0, startTagIndex + "<think>".length());
                                    inThinkTag.set(true);

                                    // Now display "Thinking..."
                                    MinecraftClient.getInstance().execute(() -> chatField.setText("Thinking..."));

                                    continue; // Check for more tags
                                } else {
                                    // No think tag, just regular content
                                    visibleContentBuffer.append(rawResponseBuffer.toString());
                                    rawResponseBuffer.setLength(0);
                                    String currentTranslation = visibleContentBuffer.toString().stripLeading();
                                    MinecraftClient.getInstance().execute(() -> {
                                        chatField.setText(currentTranslation);
                                        chatField.setCursor(currentTranslation.length(), false);
                                    });
                                    break; // Wait for more chunks
                                }
                            }
                        }
                    });

                    // Final update after stream is complete, using the accumulated visible content
                    MinecraftClient.getInstance().execute(() -> {
                        String finalTranslation = visibleContentBuffer.toString().stripLeading();
                        chatField.setText(finalTranslation);
                        chatField.setCursor(finalTranslation.length(), false);
                    });
                } else {
                    MinecraftClient.getInstance().execute(() -> chatField.setText("Translating..."));
                    String result = llm.getCompletion(apiMessages).join();
                    final String finalTranslation = result.stripLeading();
                    MinecraftClient.getInstance().execute(() -> {
                        chatField.setText(finalTranslation);
                        chatField.setCursor(finalTranslation.length(), false);
                    });
                }
            } catch (Exception e) {
                LOGGER.error("[Chat-Input-Translate] Exception during translation", e);
                MinecraftClient.getInstance().execute(() -> {
                    Text errorMessage = Text.literal("Chat Input Translation Error: " + e.getMessage()).formatted(Formatting.RED);
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(errorMessage);
                    chatField.setText(originalTextRef.get()); // Restore original on error
                    chatField.setCursor(originalTextRef.get().length(), false);
                });
            } finally {
                isTranslating.set(false);
                originalTextRef.set("");
            }
        });
    }

    @NotNull
    private static List<OpenAIRequest.Message> getMessages(ChatTranslateConfig.ChatInputTranslateConfig inputConfig, String textToTranslate) {
        String systemPrompt = inputConfig.system_prompt
                .replace("%target_language%", inputConfig.target_language);
        return List.of(
                new OpenAIRequest.Message("system", systemPrompt),
                new OpenAIRequest.Message("user", textToTranslate)
        );
    }
} 