package io.rabobank.ret.git.plugin.provider

import java.net.URL

interface GitUrlFactory {
    fun repository(repositoryName: String): URL

    fun pipelineRun(
        repositoryName: String?,
        pipelineRunId: String,
    ): URL

    fun pipeline(
        repositoryName: String?,
        pipelineId: String,
    ): URL

    fun pipelineDashboard(repositoryName: String?): URL

    fun pullRequest(
        repositoryName: String,
        pullRequestId: String,
    ): URL

    fun pullRequestCreate(
        repositoryName: String,
        targetRef: String,
        sourceRef: String?,
    ): URL
}
