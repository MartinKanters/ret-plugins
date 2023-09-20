package io.rabobank.ret.git.plugin.provider.azure

import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.PullRequestCreated

const val API_VERSION = "6.0"

class AzureDevopsProvider(
    private val azureDevopsClient: AzureDevopsClient,
    private val pluginConfig: AzureDevopsPluginConfig,
    override val urlFactory: AzureDevopsUrlFactory,
) : GitProvider {

    override val providerProperties = GitProviderProperties.AZDO

    override fun getAllPullRequests()= azureDevopsClient.getAllPullRequests().value.toGenericDomain()

    override fun getPullRequestsNotReviewedByUser() =
        getAllPullRequests().filterNot {
            it.reviewers.any { reviewer -> reviewer.uniqueName.equals(pluginConfig.config.email, true) }
        }

    override fun getPullRequestById(repositoryName: String, id: String) = azureDevopsClient.getPullRequestById(id).toGenericDomain()

    override fun createPullRequest(
        repositoryName: String,
        sourceRefName: String,
        targetRefName: String,
        title: String,
        description: String,
    ): PullRequestCreated {
        val creationDTO = CreatePullRequest("refs/heads/$sourceRefName", "refs/heads/$targetRefName", title, description)
        return azureDevopsClient.createPullRequest(repositoryName, API_VERSION, creationDTO).toGenericDomain()
    }

    override fun getAllRepositories() = azureDevopsClient.getAllRepositories().value.toGenericDomain()

    override fun getRepositoryById(repositoryName: String) =
        azureDevopsClient.getRepositoryById(repositoryName).toGenericDomain()

    override fun getAllBranches(repositoryName: String): List<Branch> {
        return azureDevopsClient.getAllRefs(repositoryName, "heads/").value.toGenericDomain()
    }

    override fun getAllPipelines(repositoryName: String?) = azureDevopsClient.getAllPipelines().value.toGenericDomain()

    override fun getPipelineRuns(pipelineId: String, repositoryName: String?) =
        azureDevopsClient.getPipelineRuns(pipelineId).value.toGenericDomain()
}
