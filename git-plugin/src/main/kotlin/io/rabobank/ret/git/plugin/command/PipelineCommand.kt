package io.rabobank.ret.git.plugin.command

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.util.BrowserUtils
import io.rabobank.ret.util.RegexUtils.DIGITS_PATTERN
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Command(
    name = "pipeline",
    description = ["Open a recent pipeline run"],
)
class PipelineCommand(
    private val urlFactory: GitUrlFactory,
    private val browserUtils: BrowserUtils,
    private val gitProvider: GitProvider,
) {

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
        val url = if (pipelineId == null) urlFactory.createPipelineDashboardUrl()
        else if (pipelineRunId == null) {
            val resolvedPipelineId = if (pipelineId.matches(DIGITS_PATTERN)) {
                pipelineId
            } else {
                getPipelineByUniqueName(pipelineId).id.toString()
            }
            urlFactory.createPipelineUrl(resolvedPipelineId)
        } else urlFactory.createPipelineRunUrl(pipelineRunId)

        browserUtils.openUrl(url)
    }

    private fun getPipelineByUniqueName(pipelineId: String?) =
        requireNotNull(gitProvider.getAllPipelines().firstOrNull { it.uniqueName == pipelineId }) {
            "Could not find pipeline id by <folder>\\<pipeline-name> combination: '$pipelineId'"
        }
}

internal class PipelineCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_pipeline").iterator()
}

internal class PipelineRunCompletionCandidates : Iterable<String> {
    override fun iterator(): Iterator<String> = listOf("function:_autocomplete_pipeline_run").iterator()
}
