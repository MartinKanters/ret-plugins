package io.rabobank.ret.git.plugin.provider.github

import io.quarkus.rest.client.reactive.ClientQueryParam
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.utilities.LoggingFilter
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient


@RegisterRestClient
@RegisterClientHeaders(AuthorizationHeaderInjector::class)
@RegisterProvider(LoggingFilter::class)
interface GitHubClient {

    @GET
    @Path("orgs/{org}/repos")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    @ClientQueryParam(name = "per_page", value = ["100"]) // TODO 100 is the max, replace this later with pagination
    fun getRepositories(
        @PathParam("org") organization: String
    ): List<Repository>

    @GET
    @Path("orgs/{org}/repos/{repositoryName}")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    fun getRepository(
        @PathParam("org") organization: String,
        @PathParam("repositoryName") repositoryName: String): Repository

    @GET
    @Path("orgs/{org}/repos/{repositoryName}/branches")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    @ClientQueryParam(name = "per_page", value = ["100"]) // TODO 100 is the max, replace this later with pagination
    fun getBranches(
        @PathParam("org") organization: String,
        @PathParam("repositoryName") repositoryName: String
    ): List<Branch>

    @GET
    @Path("search/issues")
    @ClientQueryParam(name = "per_page", value = ["100"]) // TODO 100 is the max, replace this later with pagination
    fun searchIssuesForPullRequests(@QueryParam("q") query: String): PullRequestReferences

    @GET
    @Path("repos/{org}/{repositoryName}/pulls/{number}")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    fun getPullRequestByNumber(
        @PathParam("org") organization: String,
        @PathParam("repositoryName") repositoryName: String,
        @PathParam("number") number: String
    ): PullRequest

    @GET
    @Path("repos/{org}/{repositoryName}/actions/workflows")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    fun getWorkflows(
        @PathParam("org") organization: String,
        @PathParam("repositoryName") repositoryName: String,
    ): Workflows

    @GET
    @Path("repos/{org}/{repositoryName}/actions/workflows/{pipelineId}")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    fun getWorkflowRuns(
        @PathParam("org") organization: String,
        @PathParam("repositoryName") repositoryName: String,
        @PathParam("pipelineId") pipelineId: String,
    ): WorkflowRuns

    @POST
    @Path("repos/{org}/{repositoryName}/pulls")
    @ClientHeaderParam(name = "X-GitHub-Api-Version", value = ["2022-11-28"])
    fun createPullRequest(
        @PathParam("repositoryName") repositoryName: String,
        createPullRequest: CreatePullRequest
    ): PullRequestCreated
}