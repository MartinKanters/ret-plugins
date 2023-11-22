package io.rabobank.ret.git.plugin.output

import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.GitProviderProperties
import io.rabobank.ret.git.plugin.provider.Pipeline
import io.rabobank.ret.git.plugin.provider.PipelineRun
import io.rabobank.ret.git.plugin.provider.PipelineRunState
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository

class CliAutocompleteHandler(private val retConsole: RetConsole) : OutputHandler {
    override fun listPRs(data: Map<GitProviderProperties, List<PullRequest>>) {
        data.flatMap { entry ->
            entry.value.map { "${entry.key}:${it.id}:${it.repository.name}: ${it.title}" }
        }.forEach(retConsole::out)
    }

    override fun listRepositories(data: Map<GitProviderProperties, List<Repository>>) {
        data.flatMap { entry ->
            entry.value.map { "${entry.key}:${it.name}" }
        }.forEach(retConsole::out)
    }

    override fun listBranches(data: Map<GitProviderProperties, List<Branch>>) {
        data.flatMap { entry ->
            entry.value.map { "${entry.key}:${it.name}" }
        }.forEach(retConsole::out)
    }

    override fun listPipelines(data: Map<GitProviderProperties, List<Pipeline>>) {
        data.flatMap { entry ->
            entry.value.map { "${entry.key}:${it.uniqueName}" }
        }.forEach(retConsole::out)
    }

    override fun listPipelineRuns(data: Map<GitProviderProperties, List<PipelineRun>>) {
        data.flatMap { entry ->
            entry.value.map {
                val combinedState = if (it.state == PipelineRunState.COMPLETED) it.result else it.state
                "${entry.key}:${it.id}:${it.name} ($combinedState)"
            }
        }.forEach(retConsole::out)
    }
}
