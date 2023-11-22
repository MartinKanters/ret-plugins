package io.rabobank.ret.git.plugin.utilities

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.ws.rs.core.UriBuilder
import java.net.URL

class TestUrlFactory(private val domain: String) : GitUrlFactory {
    override fun repository(repositoryName: String) = "$domain/repository/$repositoryName".toURL()

    override fun pipelineRun(
        repositoryName: String?,
        pipelineRunId: String,
    ): URL = "$domain/pipeline/run/$pipelineRunId".toURL()

    override fun pipeline(
        repositoryName: String?,
        pipelineId: String,
    ): URL = "$domain/pipeline/$pipelineId".toURL()

    override fun pipelineDashboard(repositoryName: String?): URL = "$domain/pipeline".toURL()

    override fun pullRequest(
        repositoryName: String,
        pullRequestId: String,
    ) = "$domain/pullrequest/$repositoryName/$pullRequestId".toURL()

    override fun pullRequestCreate(
        repositoryName: String,
        targetRef: String,
        sourceRef: String?,
    ): URL = ("$domain/pullrequest/create/$repositoryName/$targetRef/" + (sourceRef ?: "")).toURL()

    private fun String.toURL(): URL = UriBuilder.fromUri(this).build().toURL()
}
