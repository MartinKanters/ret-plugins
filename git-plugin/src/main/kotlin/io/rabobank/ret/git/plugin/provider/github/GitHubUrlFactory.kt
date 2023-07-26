package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder
import java.net.URL

private const val BASE_URI = "https://www.github.com"

@ApplicationScoped
class GitHubUrlFactory(pluginConfig: GitHubPluginConfig) : GitUrlFactory {

    override fun repository(repositoryName: String): URL = baseBuilder
        .path(repositoryName)
        .buildToURL()

    override fun pipelineRun(repositoryName: String?, pipelineRunId: String): URL {
        check(repositoryName != null) { "GitHub requires a repository for pipeline functionality" }
        return baseBuilder
            .path(repositoryName)
            .path("actions")
            .path("runs")
            .path(pipelineRunId)
            .buildToURL()
    }

    override fun pipeline(repositoryName: String?, pipelineId: String): URL {
        check(repositoryName != null) { "GitHub requires a repository for pipeline functionality" }
        return baseBuilder
            .path(repositoryName)
            .path("actions")
            .path("workflows")
            .path(pipelineId)
            .buildToURL()
    }

    override fun pipelineDashboard(repositoryName: String?): URL {
        check(repositoryName != null) { "GitHub requires a repository for pipeline functionality" }
        return baseBuilder
            .path(repositoryName)
            .path("actions")
            .buildToURL()
    }

    override fun pullRequest(repositoryName: String, pullRequestId: String): URL = baseBuilder
        .path(repositoryName)
        .path("pull")
        .path(pullRequestId)
        .buildToURL()

    override fun pullRequestCreate(repositoryName: String, sourceRef: String?): URL = baseBuilder
        .path(repositoryName)
        .path("compare")
        .also {
            if (sourceRef != null) {
                it.path("master...$sourceRef") // TODO - get master from default_branch
            }
        }
        .buildToURL()

    private val baseBuilder = UriBuilder.fromUri(BASE_URI)
        .path(pluginConfig.organization)
    
    private fun UriBuilder.buildToURL() = this.build().toURL()
}
