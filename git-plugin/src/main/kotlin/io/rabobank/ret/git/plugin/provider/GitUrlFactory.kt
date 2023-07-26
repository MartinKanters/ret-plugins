package io.rabobank.ret.git.plugin.provider

import java.net.URL

interface GitUrlFactory {
    fun repository(repositoryName: String): URL

    fun pipelineRun(repositoryName: String?, pipelineRunId: String): URL

    fun pipeline(repository: String?, pipelineId: String): URL

    fun pipelineDashboard(repository: String?): URL

    fun pullRequest(
        repositoryName: String,
        pullRequestId: String,
    ): URL

    fun pullRequestCreate(
        repositoryName: String,
        sourceRef: String?,
    ): URL
}
