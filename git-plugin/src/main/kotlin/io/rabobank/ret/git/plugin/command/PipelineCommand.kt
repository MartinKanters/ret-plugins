package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.Logged
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.*

@Command(
    name = "pipeline",
    description = ["Open a recent pipeline run"],
)
@Logged
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
    ) {
        val repositoryInContext = if (contextAwareness.ignoreContextAwareness) null else retContext.gitRepository

        // TODO if repo == null, check whether the GitProvider supports pipelines

        val url =
            if (pipelineId == null) {
                gitProvider.urlFactory.pipelineDashboard(repositoryInContext)
            } else if (pipelineRunId == null) {
                val resolvedPipelineId =
                    if (pipelineId.matches(DIGITS_PATTERN)) {
                        pipelineId
                    } else {
                        getPipelineByUniqueName(repositoryInContext, pipelineId).id.toString()
                    }
                gitProvider.urlFactory.pipeline(repositoryInContext, resolvedPipelineId)
            } else {
                gitProvider.urlFactory.pipelineRun(repositoryInContext, pipelineRunId)
            }

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
