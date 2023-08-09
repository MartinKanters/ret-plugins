package io.rabobank.ret.git.plugin.provider.github

import com.fasterxml.jackson.annotation.JsonProperty
import io.rabobank.ret.git.plugin.provider.*
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository
import java.time.ZonedDateTime
import io.rabobank.ret.git.plugin.provider.Branch as GenericBranch
import io.rabobank.ret.git.plugin.provider.Pipeline as GenericPipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun as GenericPipelineRun
import io.rabobank.ret.git.plugin.provider.PullRequest as GenericPullRequest
import io.rabobank.ret.git.plugin.provider.Repository as GenericRepository

data class Repository(
    @JsonProperty("url") val url: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("default_branch") val defaultBranch: String
) : GitDomainConvertible<GenericRepository> {
    override fun toGenericDomain() = GenericRepository(name, defaultBranch)
}

data class Branch(
    @JsonProperty("name") val name: String
) : GitDomainConvertible<GenericBranch> {
    override fun toGenericDomain(): Branch = GenericBranch(name)
}

data class PullRequest(
    @JsonProperty("number") val number: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("repository") val repository: Repository,
) : GitDomainConvertible<GenericPullRequest> {
    override fun toGenericDomain(): PullRequest = GenericPullRequest(
        number,
        title,
        GenericRepository(repository.name, repository.defaultBranch),
        listOf()
    )
}

data class PullRequestReference(
    @JsonProperty("repository_url") val repositoryUrl: String,
    @JsonProperty("number") val number: String,
    @JsonProperty("title") val title: String
) : GitDomainConvertible<GenericPullRequest> {
    override fun toGenericDomain(): PullRequest = GenericPullRequest(
        number,
        title,
        Repository(repositoryUrl.substringAfterLast("/"), null), // The default branch is not available here,
                                                                 //   if we want it we should do another call to get the repository in the provider code
        listOf()
    )
}

data class PullRequestReferences(
    @JsonProperty("items") val items: List<PullRequestReference>
)

data class Workflow(
    @JsonProperty("path") val path: String,
    @JsonProperty("name") val name: String,
) : GitDomainConvertible<GenericPipeline> {
    override fun toGenericDomain() =
        GenericPipeline(
            id = path,
            name = name,
            container = "",
            uniqueName = ""
        )

}

data class Workflows(
    @JsonProperty("workflows") val workflows: List<Workflow>
)

data class WorkflowRun(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("created_at") val createdAt: ZonedDateTime,
    @JsonProperty("status") val status: String,
    @JsonProperty("conclusion") val conclusion: String?,
) : GitDomainConvertible<GenericPipelineRun> {
    override fun toGenericDomain(): PipelineRun {
        return PipelineRun(
            id = id,
            name = name,
            createdDate = createdAt,
            state = when(status) {
                "completed",
                "success",
                "cancelled",
                "failure",
                "skipped",
                "stale",
                "timed_out" -> PipelineRunState.COMPLETED

                "action_required",
                "in_progress",
                "queued",
                "requested",
                "waiting",
                "pending",
                "neutral" -> PipelineRunState.IN_PROGRESS

                else -> PipelineRunState.UNKNOWN
            },
            result = when(conclusion) {
                "completed",
                "success" -> PipelineRunResult.SUCCEEDED

                "stale",
                "failure" -> PipelineRunResult.FAILED

                "timed_out",
                "cancelled",
                "skipped" -> PipelineRunResult.CANCELED

                null -> null

                else -> PipelineRunResult.UNKNOWN
            }
        )
    }
}

data class WorkflowRuns(
    @JsonProperty("workflow_runs") val workflowRuns: List<WorkflowRun>
)
