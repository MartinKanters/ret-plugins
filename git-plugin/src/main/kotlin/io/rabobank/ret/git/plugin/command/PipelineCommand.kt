package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.utils.ContextUtils
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.*

@Command(
    name = "pipeline",
    description = ["Open a recent pipeline run"],
)
class PipelineCommand(
    private val browserUtils: BrowserUtils,
    private val gitProvider: GitProvider,
    private val retContext: RetContext,
) {

    @Mixin
    lateinit var contextAwareness: ContextAwareness

    @Command(name = "open", description = ["Open the pipeline dashboard, or a specific pipeline or run"])
    fun openPipelineInBrowser(
        @Parameters(
            arity = "0..1",
            description = ["Pipeline id or <folder>\\<pipeline-name>"],
            paramLabel = "<pipeline_id>",
            completionCandidates = PipelineCompletionCandidates::class,
        ) pipelineId: String?,
        @Parameters(
            arity = "0..1",
            description = ["Pipeline run to open"],
            paramLabel = "<pipeline_run_id>",
            completionCandidates = PipelineRunCompletionCandidates::class,
        ) pipelineRunId: String?,
        @Option(
            names = ["--repository", "-r"],
            description = ["Filter on repository"],
            scope = ScopeType.INHERIT,
            completionCandidates = RepositoryFlagCompletionCandidates::class,
        ) repositoryFlag: String?
    ) {
        val repository = ContextUtils.resolveRepository(contextAwareness, retContext, repositoryFlag)
        require(repository != null || !gitProvider.properties.pipelinesTiedToRepository) {
            "A repository has to be provided to open a pipeline for it for Git provider '${gitProvider.properties.providerName}'"
        }

        val url = if (pipelineId == null) gitProvider.urlFactory.pipelineDashboard(repository)
        else if (pipelineRunId == null) {
            val resolvedPipelineId = if (pipelineId.matches(DIGITS_PATTERN)) {
                pipelineId
            } else {
                getPipelineByUniqueName(repository, pipelineId).id
            }
            gitProvider.urlFactory.pipeline(repository, resolvedPipelineId)
        } else gitProvider.urlFactory.pipelineRun(repository, pipelineRunId)

        browserUtils.openUrl(url)
    }

    private fun getPipelineByUniqueName(repositoryName: String?, pipelineId: String?) =
        requireNotNull(gitProvider.getAllPipelines(repositoryName).firstOrNull { it.uniqueName == pipelineId }) {
            "Could not find pipeline id by <folder>\\<pipeline-name> combination: '$pipelineId'"
        }
}

internal class PipelineCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_pipeline").iterator()
}

internal class PipelineRunCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_pipeline_run").iterator()
}
