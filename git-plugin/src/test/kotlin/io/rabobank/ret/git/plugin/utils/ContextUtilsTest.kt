package io.rabobank.ret.git.plugin.utils

import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ContextUtilsTest {

    @Test
    fun `repositoryFlag is prioritized`() {
        val contextAwareness = ContextAwareness()

        val resolvedRepository = ContextUtils.resolveRepository(contextAwareness, RetContext(gitRepository = "repo-from-context"), "repo-from-flag")

        assertThat(resolvedRepository).isEqualTo("repo-from-flag")
    }

    @Test
    fun `when repositoryFlag is missing, context is prioritized next`() {
        val contextAwareness = ContextAwareness()

        val resolvedRepository = ContextUtils.resolveRepository(contextAwareness, RetContext(gitRepository = "repo-from-context"), null)

        assertThat(resolvedRepository).isEqualTo("repo-from-context")
    }

    @Test
    fun `when repositoryFlag is missing, context is ignored when context awareness is not activated`() {
        val contextAwareness = ContextAwareness().apply { ignoreContextAwareness = true }

        val resolvedRepository = ContextUtils.resolveRepository(contextAwareness, RetContext(gitRepository = "repo-from-context"), null)

        assertThat(resolvedRepository).isNull()
    }
}