package com.doublesymmetry.kotlinaudio.models

import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import androidx.media3.session.R

enum class CustomCommandButton(
    val customAction: String,
    val capability: Capability,
    val commandButton: CommandButton,
) {
    JUMP_BACKWARD(
        customAction = "JUMP_BACKWARD",
        capability = Capability.JUMP_BACKWARD,
        commandButton = CommandButton.Builder()
            .setDisplayName("Jump Backward")
            .setSessionCommand(SessionCommand("JUMP_BACKWARD", Bundle()))
            .setIconResId(R.drawable.media3_icon_skip_back)
            .build(),
    ),
    JUMP_FORWARD(
        customAction = "JUMP_FORWARD",
        capability = Capability.JUMP_FORWARD,
        commandButton = CommandButton.Builder()
            .setDisplayName("Jump Forward")
            .setSessionCommand(SessionCommand("JUMP_FORWARD", Bundle()))
            .setIconResId(R.drawable.media3_icon_skip_forward)
            .build(),
    );
}