package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.IntelliSearch
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.utils.ContextUtils
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.Logged
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Option
import picocli.CommandLine.ScopeType

@Command(name = "autocomplete", hidden = true)
@Logged
class AutoCompleteCommand(
    private val gitProviderSelector: GitProviderSelector,
    private val intelliSearch: IntelliSearch,
    private val outputHandler: OutputHandler,
    private val retContext: RetContext,
) {
    companion object {
        private const val TOP_20_PIPELINES = 20
    }

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "git-pipeline")
    fun printPipelines(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--repository", "-r"]) repositoryFlag: String?,
    ) {
        val repository = ContextUtils.resolveRepository(contextAwareness, retContext, repositoryFlag)
        val pipelinesByProvider = gitProviderSelector.all()
            .associateBy( { it.providerProperties }, { provider -> provider.getAllPipelines(repository)
                .filter { it.matches(word) }
                .sortedWith(compareBy( { it.container }, { it.name } )) })

        outputHandler.listPipelines(pipelinesByProvider)
    }

    @Command(name = "pipeline-run")
    fun printPipelineRuns(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(
            required = true,
            names = ["--pipeline-id"],
            description = ["Filter on pipeline"],
        ) pipelineIdFlag: String,
        @Option(names = ["--repository", "-r"]) repositoryFlag: String?,
    ) {
        val repository = ContextUtils.resolveRepository(contextAwareness, retContext, repositoryFlag)
        val pipelineId =
            if (pipelineIdFlag.matches(DIGITS_PATTERN)) {
                pipelineIdFlag
            } else {
                val (gitProviderKey, id) = pipelineIdFlag.splitByProviderKeyAndValue()
            getPipelineByUniqueName(gitProviderKey, repository, id).id
            }

        val pipelineRunsByProvider = gitProviderSelector.all().associateBy ( { it.providerProperties }, { provider -> provider.getPipelineRuns(pipelineId, repository)
            .filter { it.matches(word) }
            .sortedByDescending { it.createdDate }
            .take(TOP_20_PIPELINES) } )

        outputHandler.listPipelineRuns(pipelineRunsByProvider)
    }

    @Command(name = "repository")
    fun printRepositories(
        @Option(names = ["--word", "-w"]) word: String?,
    ) {
        val repositoriesByProvider = gitProviderSelector.all().associateBy( { it.providerProperties }, { provider -> provider.getAllRepositories()
            .filter { word == null || intelliSearch.matches(word, it.name) } } )

        outputHandler.listRepositories(repositoriesByProvider)
    }

    @Command(name = "branch")
    fun printBranches(
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(names = ["--repository", "-r"]) repositoryFlag: String?,
    ) {
        val repository = ContextUtils.resolveRepository(contextAwareness, retContext, repositoryFlag)

        repository?.let { repo ->
            val branchesByProvider = gitProviderSelector.all().associateBy({ it.providerProperties }, { provider -> provider.getAllBranches(repo)
                .filter { word == null || intelliSearch.matches(word, it.name) } } )

            outputHandler.listBranches(branchesByProvider)
        } ?: outputHandler.error("No repository could be determined")
    }

    @Command(name = "pullrequest")
    fun printPullRequests(
        @Option(
            names = ["-n", "--not-reviewed"],
            description = ["Only show Pull Requests that you have not been reviewed"],
        )
        notReviewed: Boolean = false,
        @Option(names = ["--word", "-w"]) word: String?,
        @Option(
            names = ["--repository", "-r"],
            description = ["Filter on repository"],
            scope = ScopeType.INHERIT,
        )
        filterRepository: String? = null,
    ) {
        val getPRsFunction = if (!notReviewed) GitProvider::getAllPullRequests else GitProvider::getPullRequestsNotReviewedByUser
        val filteredPRsByProvider = gitProviderSelector.all().associateBy( { it.providerProperties }, { provider -> getPRsFunction(provider)
                .filter { it.isFromRepository(filterRepository) }
                .filter {
                    word == null ||
                        intelliSearch.matches(word, it.title) ||
                        intelliSearch.matches(word, it.repository.name)
                } } )

        outputHandler.listPRs(filteredPRsByProvider)
    }

    private fun PullRequest.isFromRepository(filterRepository: String?): Boolean {
        val filterWord = ContextUtils.resolveRepository(contextAwareness, retContext, filterRepository)

        return filterWord.isNullOrBlank() || this.repository.name.equals(filterWord, true)
    }

    private fun Pipeline.matches(value: String?) =
        value == null ||
            intelliSearch.matches(value, name) ||
            intelliSearch.matches(value, container) ||
            intelliSearch.matches(value, uniqueName)

    private fun PipelineRun.matches(word: String?) =
        word == null || intelliSearch.matches(word, id) || intelliSearch.matches(word, name) ||
            intelliSearch.matches(word, state.toString()) || intelliSearch.matches(word, result.toString())

    private fun getPipelineByUniqueName(gitProviderProperties: GitProviderProperties, repository: String?, pipelineIdFlag: String): Pipeline {
        val gitProvider = gitProviderSelector.byKey(gitProviderProperties)
        val pipelineByRepositoryAndId = gitProvider.getAllPipelines(repository).firstOrNull { it.uniqueName == pipelineIdFlag }
        return requireNotNull(pipelineByRepositoryAndId) {
            "Could not find pipeline id by <folder>\\<pipeline-name> combination: '$pipelineIdFlag' for repository: $repository"
        }
    }
}
