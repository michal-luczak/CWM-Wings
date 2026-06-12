package dev.codewithmike.wings.wing

import dev.codewithmike.wings.data.definition.WingsDefinitionDto
import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

private val VALID_WINGS_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$")

class WingsDefinitionArgumentResolver(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope
) : ArgumentResolver<CommandSender, WingsDefinitionDto>() {


    override fun suggest(
        invocation: Invocation<CommandSender>,
        argument: Argument<WingsDefinitionDto>,
        context: SuggestionContext
    ): SuggestionResult {
        return SuggestionResult.of(runBlocking { wingsManager.getWingsDefinitions() })
    }

    override fun parse(
        invocation: Invocation<CommandSender>,
        context: Argument<WingsDefinitionDto>,
        argument: String
    ): ParseResult<WingsDefinitionDto> {
        return ParseResult.completableFuture(scope.future { wingsManager.getWingsDefinition(argument) }) { wings ->
            if (wings == null) {
                return@completableFuture ParseResult.failure("Wings definition not found.")
            }

            return@completableFuture ParseResult.success(wings)
        }
    }

    override fun match(invocation: Invocation<CommandSender>, context: Argument<WingsDefinitionDto>, argument: String): Boolean {
        return VALID_WINGS_PATTERN.matcher(argument).matches()
    }
}
