package io.rabobank.ret.git.plugin.provider

interface GitProvider {
    fun getAllPullRequests(): List<PullRequest>

    fun getPullRequestsNotReviewedByUser(): List<PullRequest>

    fun getPullRequestById(repositoryName: String, id: String): PullRequest

    fun createPullRequest(
        repositoryName: String,
        sourceRefName: String,
        targetRefName: String,
        title: String,
        description: String,
    ): PullRequestCreated // TODO: Verify whether this is implementable in GitHub

    fun getAllRepositories(): List<Repository>

    fun getRepositoryById(repositoryName: String): Repository

    fun getAllBranches(
        repositoryName: String,
    ): List<Branch>

    fun getAllPipelines(repositoryName: String?): List<Pipeline>

    fun getPipelineRuns(pipelineId: String, repositoryName: String?): List<PipelineRun>

    val urlFactory: GitUrlFactory

    val properties: GitProviderProperties
}
