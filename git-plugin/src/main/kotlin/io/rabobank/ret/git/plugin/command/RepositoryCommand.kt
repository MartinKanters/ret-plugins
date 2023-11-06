package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.provider.GitProviderSelector
import io.rabobank.ret.git.plugin.provider.splitByProviderKeyAndValue
import io.rabobank.ret.git.plugin.utils.ContextUtils
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.Logged
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters
import java.lang.IllegalArgumentException

@Command(
    name = "repository",
    description = ["List all repositories"],
)
@Logged
class RepositoryCommand(
    private val gitProviderSelector: GitProviderSelector,
    private val browserUtils: BrowserUtils,
    private val retContext: RetContext,
) {
    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "open")
    fun openRepositoryInBrowser(
        @Parameters(
            arity = "0..1",
            description = ["Repository name to open in the browser"],
            paramLabel = "<repository>",
            completionCandidates = RepositoryCompletionCandidates::class,
        ) repositoryFlag: String?,
    ) {
        val repositoryByProvider = ContextUtils.resolveRepository(contextAwareness, retContext, repositoryFlag)
        val (gitProviderKey, repository) = repositoryByProvider?.splitByProviderKeyAndValue() ?: throw IllegalArgumentException("No repository provided and ret cannot get repository from context.")
            val gitProvider = gitProviderSelector.byKey(gitProviderKey)

        val repositoriesByProvider = gitProvider.getAllRepositories()

        require(repositoriesByProvider.any { it.name == repository }) { "No repository found with name $repository for provider $gitProviderKey." }

        val url = gitProvider.urlFactory.repository(repository)
        browserUtils.openUrl(url)
    }
}

internal class RepositoryCompletionCandidates : Iterable<String> {
    override fun iterator() = listOf("function:_autocomplete_repository").iterator()
}
