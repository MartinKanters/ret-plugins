package io.rabobank.ret.git.plugin.provider.github

import io.rabobank.ret.git.plugin.provider.*
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.provider.azure.toGenericDomain

class GitHubProvider(
    private val gitHubClient: GitHubClient,
    private val pluginConfig: GitHubPluginConfig,
    override val urlFactory: GitUrlFactory
) : GitProvider {

    override val properties = GitProviderProperties(providerName = "GitHub", pipelinesTiedToRepository = true)

    override fun getAllPullRequests(): List<PullRequest> {
        return gitHubClient.searchIssuesForPullRequests("org:${pluginConfig.organization} is:pull-request state:open").items.toGenericDomain()
    }

    override fun getPullRequestsNotReviewedByUser(): List<PullRequest> {
        // GitHub does not offer a solution for finding this without doing N+1 searches (first finding all PRs, then per PR finding the reviewers)
        throw IllegalStateException("Not implemented for the GitHub Git provider")
    }

    override fun getPullRequestById(repositoryName: String, id: String) =
        gitHubClient.getPullRequestByNumber(pluginConfig.organization, repositoryName, id).toGenericDomain()


    override fun createPullRequest(repositoryName: String, sourceRefName: String, targetRefName: String, title: String, description: String) =
        gitHubClient.createPullRequest(repositoryName, CreatePullRequest(sourceRefName, targetRefName, title, description))

    override fun getAllRepositories(): List<Repository> {
        return gitHubClient.getRepositories(pluginConfig.organization).toGenericDomain()
    }

    override fun getRepositoryById(repositoryName: String): Repository {
        return gitHubClient.getRepository(pluginConfig.organization, repositoryName).toGenericDomain()
    }

    override fun getAllBranches(repositoryName: String): List<Branch> {
        return gitHubClient.getBranches(pluginConfig.organization, repositoryName).toGenericDomain()
    }

    override fun getAllPipelines(repositoryName: String?): List<Pipeline> {
        check(repositoryName != null) { "GitHub requires a repository for pipeline functionality" }
        return gitHubClient.getWorkflows(pluginConfig.organization, repositoryName).workflows.toGenericDomain()
            .map { it.copy(container = repositoryName, uniqueName = "$repositoryName:${it.id}") }
    }

    override fun getPipelineRuns(pipelineId: String, repositoryName: String?): List<PipelineRun> {
        check(repositoryName != null) { "GitHub requires a repository for pipeline functionality" }
        return gitHubClient.getWorkflowRuns(pluginConfig.organization, repositoryName, pipelineId).workflowRuns.toGenericDomain()
    }
}