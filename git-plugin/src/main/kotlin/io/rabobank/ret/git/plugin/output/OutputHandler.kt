package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.git.plugin.provider.*

interface OutputHandler {
    fun println(message: String) {
        // No-op
    }

    fun error(message: String) {
        // No-op
    }

    fun listPRs(data: Map<GitProviderProperties, List<PullRequest>>)

    fun listRepositories(data: Map<GitProviderProperties, List<Repository>>)

    fun listBranches(data: Map<GitProviderProperties, List<Branch>>)

    fun listPipelines(data: Map<GitProviderProperties, List<Pipeline>>)

    fun listPipelineRuns(data: Map<GitProviderProperties, List<PipelineRun>>)
}
