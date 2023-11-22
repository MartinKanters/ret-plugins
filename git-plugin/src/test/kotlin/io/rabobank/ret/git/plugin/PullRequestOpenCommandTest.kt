package io.rabobank.ret.git.plugin

import io.quarkus.test.junit.QuarkusTest
import io.rabobank.ret.RetContext
import io.rabobank.ret.git.plugin.command.PullRequestOpenCommand
import io.rabobank.ret.git.plugin.output.OutputHandler
import io.rabobank.ret.git.plugin.provider.GitProvider
import io.rabobank.ret.git.plugin.provider.GitProviderSelector
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository
import io.rabobank.ret.git.plugin.provider.Reviewer
import io.rabobank.ret.git.plugin.utilities.TestUrlFactory
import io.rabobank.ret.picocli.mixin.ContextAwareness
import io.rabobank.ret.util.BrowserUtils
import org.assertj.core.api.Assertions.assertThat
import org.jboss.resteasy.reactive.ClientWebApplicationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import picocli.CommandLine
import java.net.URI
import java.net.URL

private const val BASE_URL = "https://test.git"

@QuarkusTest
internal class PullRequestOpenCommandTest {
    private val mockedGitProvider = mock<GitProvider>()
    private val mockedBrowserUtils = mock<BrowserUtils>()
    private val outputHandler = mock<OutputHandler>()
    private val retContext = mock<RetContext>()
    private lateinit var commandLine: CommandLine

    @BeforeEach
    fun before() {
        val mockedGitProviderSelector = mock<GitProviderSelector>()

        val command =
            PullRequestOpenCommand(
                mockedGitProviderSelector,
                mockedBrowserUtils,
                outputHandler,
                retContext,
            )

        command.contextAwareness = ContextAwareness()

        commandLine = spy(CommandLine(command))

        whenever(mockedGitProvider.urlFactory).thenReturn(TestUrlFactory("https://test.git"))
        whenever(retContext.gitRepository).thenReturn("repo")
        whenever(mockedGitProviderSelector.byKey(any())).thenReturn(mockedGitProvider)
    }

    @Test
    fun `pull requests can be opened`() {
        whenever(mockedGitProvider.getPullRequestById("repo", "1234")).thenReturn(
            PullRequest(
                "1234",
                "PR Title",
                Repository("repo", "master"),
                listOf(Reviewer("manks@live.com")),
            ),
        )

        val exitCode = commandLine.execute("AZDO:1234")
        assertThat(exitCode).isEqualTo(0)
        val expectedURL = URI.create("$BASE_URL/pullrequest/repo/1234").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `pull requests can be opened and favors the -r flag over context`() {
        whenever(mockedGitProvider.getPullRequestById("actualRepo", "1234")).thenReturn(
            PullRequest(
                "1234",
                "PR Title",
                Repository("repo", "master"),
                listOf(Reviewer("manks@live.com")),
            ),
        )

        val exitCode = commandLine.execute("AZDO:1234", "-r", "actualRepo")
        assertThat(exitCode).isEqualTo(0)
        val expectedURL = URI.create("$BASE_URL/pullrequest/repo/1234").toURL()

        verify(mockedBrowserUtils).openUrl(expectedURL)
    }

    @Test
    fun `pull request cannot be opened when it does not exist`() {
        whenever(mockedGitProvider.getPullRequestById("repo", "12345")).thenThrow(
            ClientWebApplicationException(404),
        )

        val exitCode = commandLine.execute("AZDO:12345")
        assertThat(exitCode).isEqualTo(1)
        verify(outputHandler).error("Pull request with id 'AZDO:12345' could not be found")
    }

    @Test
    fun `pull requests cannot be opened when no repository is given and context is ignored`() {
        whenever(mockedGitProvider.getPullRequestById("repo", "1234")).thenReturn(
            PullRequest(
                "1234",
                "PR Title",
                Repository("repo", "master"),
                listOf(Reviewer("manks@live.com")),
            ),
        )

        val exitCode = commandLine.execute("1234", "-ica")
        assertThat(exitCode).isEqualTo(1)

        verify(mockedBrowserUtils, never()).openUrl(any<URL>())
    }
}
