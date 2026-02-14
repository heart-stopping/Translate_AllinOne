package com.cedarxuesong.translate_allinone.mixin.mixinChatScreen;

import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import com.cedarxuesong.translate_allinone.utils.translate.ChatInputTranslateManager;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Unique
    private boolean isSendingTranslated = false;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (KeybindingManager.chatInputTranslateKey.matchesKey(keyInput)) {
            ChatInputTranslateManager.translate(this.chatField);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (isSendingTranslated) {
            return;
        }

        if (ChatInputTranslateManager.interceptAndTranslate(chatText, addToHistory, (ChatScreen)(Object) this, translated -> {
            isSendingTranslated = true;
            try {
                ((ChatScreen)(Object) this).sendMessage(translated, addToHistory);
            } finally {
                isSendingTranslated = false;
            }
        })) {
            ci.cancel();
        }
    }
}
