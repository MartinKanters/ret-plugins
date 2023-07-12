package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.GitUrlFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.UriBuilder
import java.net.URL

private const val BASE_URI = "https://www.github.com"

@ApplicationScoped
class GitHubUrlFactory(private val pluginConfig: GitHubPluginConfig) : GitUrlFactory {

    override fun repository(repositoryName: String): URL = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .buildToURL()

    override fun pipelineRun(pipelineRunId: String): URL {
        TODO("Not yet implemented")
    }

    override fun pipeline(pipelineId: String): URL {
        TODO("Not yet implemented")
    }

    override fun pipelineDashboard(): URL {
        TODO("Not yet implemented")
    }

    override fun pullRequest(repositoryName: String, pullRequestId: String): URL = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .path("pull")
        .path(pullRequestId)
        .buildToURL()

    override fun pullRequestCreate(repositoryName: String, sourceRef: String?): URL = baseBuilder
        .path(pluginConfig.organization)
        .path(repositoryName)
        .path("compare")
        .also {
            if (sourceRef != null) {
                it.path("master...$sourceRef") // TODO - get master from default_branch
            }
        }
        .buildToURL()

    private val baseBuilder = UriBuilder.fromUri(BASE_URI)
    
    private fun UriBuilder.buildToURL() = this.build().toURL()
}
