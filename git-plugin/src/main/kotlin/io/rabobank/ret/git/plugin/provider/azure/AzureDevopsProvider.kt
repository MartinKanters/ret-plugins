package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.*
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository

class AzureDevopsProvider(
    private val azureDevopsClient: AzureDevopsClient,
    private val pluginConfig: AzureDevopsPluginConfig,
    override val urlFactory: AzureDevopsUrlFactory
) : GitProvider {

    override val properties = GitProviderProperties(
        providerName = "Azure Devops",
        pipelinesTiedToRepository = false
    )

    override fun getAllPullRequests(): List<PullRequest> {
        return azureDevopsClient.getAllPullRequests().value.toGenericDomain()
    }

    override fun getPullRequestsNotReviewedByUser(): List<PullRequest> {
        return getAllPullRequests().filterNot {
            it.reviewers.any { reviewer -> reviewer.uniqueName.equals(pluginConfig.email, true) }
        }
    }

    override fun getPullRequestById(repositoryName: String, id: String): PullRequest {
        return azureDevopsClient.getPullRequestById(id).toGenericDomain()
    }

    override fun createPullRequest(
        repositoryName: String,
        sourceRefName: String,
        targetRefName: String,
        title: String,
        description: String,
    ): PullRequestCreated {
        val creationDTO = CreatePullRequest("refs/heads/$sourceRefName", "refs/heads/$targetRefName", title, description)
        return azureDevopsClient.createPullRequest(repositoryName, "6.0", creationDTO).toGenericDomain()
    }

    override fun getAllRepositories(): List<Repository> {
        return azureDevopsClient.getAllRepositories().value.toGenericDomain()
    }

    override fun getRepositoryById(repositoryName: String): Repository {
        return azureDevopsClient.getRepositoryById(repositoryName).toGenericDomain()
    }

    override fun getAllBranches(repositoryName: String): List<Branch> {
        return azureDevopsClient.getAllRefs(repositoryName, "heads/").value.toGenericDomain()
    }

    override fun getAllPipelines(repositoryName: String?): List<Pipeline> {
        return azureDevopsClient.getAllPipelines().value.toGenericDomain()
    }

    override fun getPipelineRuns(pipelineId: String, repositoryName: String?): List<PipelineRun> {
        return azureDevopsClient.getPipelineRuns(pipelineId).value.toGenericDomain()
    }

}