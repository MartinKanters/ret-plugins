package io.rabobank.ret.git.plugin.utils

import io.rabobank.ret.RetContext
import io.rabobank.ret.picocli.mixin.ContextAwareness

object ContextUtils {
    /**
     * Resolve the repository in the right order:
     *  First try the flag, otherwise take it from the context, if activated.
     * Returns null when the flag is not provided, not present in the context or when context-awareness is disabled.
     */
    fun resolveRepository(
        contextAwareness: ContextAwareness,
        retContext: RetContext,
        repositoryFlag: String?,
    ): String? {
        val repositoryInContext = if (contextAwareness.ignoreContextAwareness) null else retContext.gitRepository
        return if (repositoryFlag.isNullOrBlank()) repositoryInContext else repositoryFlag
    }
}
