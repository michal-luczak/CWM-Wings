package dev.codewithmike.wings.utils.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

fun CommandSender.sendSuccessMessage(text: String) {
    val component = successComponent(text)
    sendMessage(component)
}

fun CommandSender.sendFailureMessage(text: String) {
    val component = failureComponent(text)
    sendMessage(component)
}

fun CommandSender.sendLoadingMessage(text: String) {
    val component = loadingComponent(text)
    sendMessage(component)
}

fun CommandSender.sendInfoMessage(text: String) {
    val component = infoComponent(text)
    sendMessage(component)
}

fun failureComponent(text: String): Component =
    Component
        .text("[", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
        .append(Component.text("❌", NamedTextColor.DARK_RED, TextDecoration.BOLD))
        .append(Component.text("] ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
        .append(Component.text(text, NamedTextColor.RED).decoration(TextDecoration.BOLD, false))

fun successComponent(text: String): Component =
    Component
        .text("[", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
        .append(Component.text("✔", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
        .append(Component.text("] ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
        .append(Component.text(text, NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false))

fun loadingComponent(text: String): Component =
    Component
        .text("[", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
        .append(Component.text("⌛", NamedTextColor.GOLD, TextDecoration.BOLD))
        .append(Component.text("] ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
        .append(Component.text(text, NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false))

fun infoComponent(text: String): Component =
    Component
        .text("[", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
        .append(Component.text("ℹ", NamedTextColor.AQUA, TextDecoration.BOLD))
        .append(Component.text("] ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
        .append(Component.text(text, NamedTextColor.AQUA).decoration(TextDecoration.BOLD, false))
