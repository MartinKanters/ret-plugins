package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.command.PullRequestCreateCommand
import io.rabobank.ret.git.plugin.config.ExceptionMessageHandler
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.GitProviderSelector
import io.rabobank.ret.git.plugin.provider.PullRequestCreated
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.utilities.TestUrlFactory
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.anyString
import org.mockito.Mockito.contains
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.net.URI

private const val BASE_URL = "https://test.git"

private const val REPO = "generic-project"
private const val DEFAULT_BRANCH = "defaultBranch"

@QuarkusTest
internal class PullRequestCreateCommandTest {
    private val gitProvider = mock<GitProvider>()
    private val mockedBrowserUtils = mock<BrowserUtils>()
    private val mockedRetContext = mock<RetContext>()
    private val outputHandler = mock<OutputHandler>()
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        val mockedGitProviderSelector = mock<GitProviderSelector>()
        val command =
            PullRequestCreateCommand(
                mockedGitProviderSelector,
                mockedBrowserUtils,
                outputHandler,
                mockedRetContext,
            )

        command.contextAwareness = ContextAwareness()

        commandLine = spy(CommandLine(command))
        commandLine.executionExceptionHandler = ExceptionMessageHandler(outputHandler)
        whenever(gitProvider.getRepositoryById(REPO)).thenReturn(Repository(REPO, DEFAULT_BRANCH))
        whenever(gitProvider.urlFactory).thenReturn(TestUrlFactory("https://test.git"))
        whenever(mockedGitProviderSelector.byKey(any())).thenReturn(gitProvider)
    }

    @Test
    fun `create should open web page to create pr in context aware case`() {
        whenever(mockedRetContext.gitRepository).thenReturn("AZDO:$REPO")
        whenever(mockedRetContext.gitBranch).thenReturn("feature/my-branch")

        val exitCode = commandLine.execute()
        assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            URI.create("$BASE_URL/pullrequest/create/$REPO/$DEFAULT_BRANCH/feature/my-branch").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr when repo and branch are provided`(flag: String) {
        val exitCode = commandLine.execute(flag, "AZDO:$REPO", "feature/my-branch")
        assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            URI.create("$BASE_URL/pullrequest/create/$REPO/$DEFAULT_BRANCH/feature/my-branch").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr without selected branch, when repo provided and branch not`(
        flag: String,
    ) {
        val exitCode = commandLine.execute(flag, "AZDO:$REPO")
        assertThat(exitCode).isEqualTo(0)

        val expectedURL = URI.create("$BASE_URL/pullrequest/create/$REPO/$DEFAULT_BRANCH/").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--repository", "-r"])
    fun `create should open web page to create pr without selected branch, when repo provided and branch from context`(
        flag: String,
    ) {
        whenever(mockedRetContext.gitBranch).thenReturn("AZDO:feature/my-branch")

        val exitCode = commandLine.execute(flag, "AZDO:$REPO")
        assertThat(exitCode).isEqualTo(0)

        val expectedURL = URI.create("$BASE_URL/pullrequest/create/$REPO/$DEFAULT_BRANCH/").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `create should open web page to create pr when branch provided, repo from context`() {
        whenever(mockedRetContext.gitRepository).thenReturn("AZDO:$REPO")

        val exitCode = commandLine.execute("feature/my-branch")
        assertThat(exitCode).isEqualTo(0)

        val expectedURL =
            URI.create("$BASE_URL/pullrequest/create/$REPO/$DEFAULT_BRANCH/feature/my-branch").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `create should return error when branch provided, repo not from execution context`() {
        val branch = "feature/my-branch"

        val exitCode = commandLine.execute(branch)
        assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not determine repository from context. Please provide the repository.")
        verify(outputHandler).error(contains("Usage:"))
    }

    @Test
    fun `create should return a URL to the created PR with --no-prompt`() {
        val branch = "feature/my-branch"
        val createdPullRequestId = "123456"

        whenever(gitProvider.getRepositoryById(REPO)).thenReturn(Repository(REPO, DEFAULT_BRANCH))
        whenever(
            gitProvider.createPullRequest(
                REPO,
                branch,
                DEFAULT_BRANCH,
                "Merge $branch into $DEFAULT_BRANCH",
                "PR created by RET using `ret pr create --no-prompt`.",
            ),
        ).thenReturn(PullRequestCreated(createdPullRequestId))

        val exitCode = commandLine.execute("-r", "AZDO:$REPO", "--no-prompt", branch)
        assertThat(exitCode).isEqualTo(0)
        verify(outputHandler)
            .println("$BASE_URL/pullrequest/$REPO/$createdPullRequestId")
    }

    @Test
    fun `create should handle if PR is already created with --no-prompt`() {
        val branch = "feature/my-branch"

        whenever(gitProvider.getRepositoryById(REPO)).thenReturn(Repository(REPO, DEFAULT_BRANCH))
        whenever(gitProvider.createPullRequest(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(ClientWebApplicationException(Response.Status.CONFLICT))

        val exitCode = commandLine.execute("-r", "AZDO:$REPO", "--no-prompt", branch)
        assertThat(exitCode).isEqualTo(1)
        verify(outputHandler).error("A pull request for this branch already exists!")
    }

    @Test
    fun `create should handle if current branch is the same as the default branch with --no-prompt`() {
        val exitCode = commandLine.execute("-r", "AZDO:$REPO", "--no-prompt", DEFAULT_BRANCH)
        assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not create PR. Source branch is the same as the default branch.")
    }

    @Test
    fun `create should handle if no git context available with --no-prompt`() {
        val exitCode = commandLine.execute("-r", "AZDO:$REPO", "--no-prompt")
        assertThat(exitCode).isEqualTo(2)
        verify(outputHandler).error("Could not determine branch from context. Please provide the branch.")
    }
}
