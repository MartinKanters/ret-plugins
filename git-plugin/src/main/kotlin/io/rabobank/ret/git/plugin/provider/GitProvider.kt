package io.rabobank.ret.git.plugin.provider

interface GitProvider {
    fun getAllPullRequests(): List<PullRequest>

    fun getPullRequestsNotReviewedByUser(): List<PullRequest>

    fun getPullRequestById(repository: String, id: String): PullRequest

    fun createPullRequest(
        repository: String,
        sourceRefName: String,
        targetRefName: String,
        title: String,
        description: String,
    ): PullRequestCreated // TODO: Verify whether this is implementable in GitHub

    fun getAllRepositories(): List<Repository>

    fun getRepositoryById(repository: String): Repository

    fun getAllRefs(
        repository: String,
        filter: String,
    ): List<Branch>

    fun getAllPipelines(repository: String?): List<Pipeline>

    fun getPipelineRuns(pipelineId: String, repository: String?): List<PipelineRun>

    val urlFactory: GitUrlFactory
}
