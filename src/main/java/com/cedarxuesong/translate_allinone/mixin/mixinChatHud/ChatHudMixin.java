package com.cedarxuesong.translate_allinone.mixin.mixinChatHud;

import com.cedarxuesong.translate_allinone.registration.LifecycleEventManager;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.translate.ChatOutputTranslateManager;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow
    private List<ChatHudLine> messages;

    @Unique
    private static final ThreadLocal<Boolean> isProcessing = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<UUID> pendingMessageId = new ThreadLocal<>();

    @Unique
    private Text processMessage(Text message) {
        if (isProcessing.get() || !LifecycleEventManager.isReadyForTranslation) {
            return message;
        }

        isProcessing.set(true);
        pendingMessageId.remove();

        try {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (config.chatTranslate.output.enabled) {
                String plainText = AnimationManager.stripFormatting(message.getString()).trim();
                if (plainText.isEmpty()) {
                    return message;
                }

            UUID messageId = UUID.randomUUID();
            MessageUtils.putTrackedMessage(messageId, message);

                if (config.chatTranslate.output.auto_translate) {
                    pendingMessageId.set(messageId);
                    return message;
                } else {
            MutableText translateButton = Text.literal(" [T]");
            Style style = Style.EMPTY
                    .withClickEvent(new ClickEvent.RunCommand("/translate_allinone translatechatline " + messageId))
                    .withHoverEvent(new HoverEvent.ShowText(Text.translatable("text.translate_allinone.translate_button_hover")));
            translateButton.setStyle(style);

            return Text.empty().append(message).append(translateButton);
                }
        }
        return message;
        } catch (Exception e) {
            isProcessing.set(false);
            return message;
        }
    }

    @Unique
    private void processMessageTail() {
        if (!isProcessing.get()) {
            return;
        }
        isProcessing.set(false);

        UUID messageId = pendingMessageId.get();
        if (messageId == null) {
            return;
        }
        pendingMessageId.remove();

        if (!this.messages.isEmpty()) {
            ChatHudLine addedLine = this.messages.get(0);
            Text originalMessage = MessageUtils.getTrackedMessage(messageId);
            if (originalMessage != null) {
                ChatOutputTranslateManager.translate(messageId, originalMessage, addedLine);
            }
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text onAddMessage(Text message) {
        return processMessage(message);
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("TAIL"))
    private void onAddMessageTail(CallbackInfo ci) {
        processMessageTail();
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text onAddMessageSimple(Text message) {
        return processMessage(message);
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    private void onAddMessageSimpleTail(CallbackInfo ci) {
        processMessageTail();
    }
} 
