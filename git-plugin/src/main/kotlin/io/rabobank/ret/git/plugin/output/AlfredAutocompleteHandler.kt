package io.rabobank.ret.git.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.provider.*

class AlfredAutocompleteHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) :
    OutputHandler {
    override fun println(message: String) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        throw UnsupportedOperationException()
    }

    override fun listPRs(data: Map<GitProviderProperties, List<PullRequest>>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (data.all { it.value.isEmpty() }) {
                    Wrapper(listOf(Item("No pull requests found", valid = false)))
                } else {
                    Wrapper(data.flatMap { entry -> entry.value.map { Item(entry.key, it) } })
                },
            ),
        )
    }

    override fun listRepositories(data: Map<GitProviderProperties, List<Repository>>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (data.all { it.value.isEmpty() }) {
                    Wrapper(listOf(Item("No repositories found", valid = false)))
                } else {
                    Wrapper(data.flatMap { entry -> entry.value.map { Item(entry.key, it) } })
                },
            ),
        )
    }

    override fun listBranches(data: Map<GitProviderProperties, List<Branch>>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (data.all { it.value.isEmpty() }) {
                    Wrapper(listOf(Item("No branches found", valid = false)))
                } else {
                    Wrapper(data.flatMap { entry -> entry.value.map { Item(entry.key, it) } })
                },
            ),
        )
    }

    override fun listPipelines(data: Map<GitProviderProperties, List<Pipeline>>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (data.all { it.value.isEmpty() }) {
                    Wrapper(listOf(Item("No pipelines found", valid = false)))
                } else {Wrapper(
                    listOf(Item(title = "Pipeline dashboard", arg = "open-dashboard")) +
                            data.flatMap { entry -> entry.value.map {
                                Item(title = "${entry.key}:${it.name}", subtitle = "Folder: ${it.container}", arg = "${entry.key}:${it.id}")
                            }
                    },
                )},
            ),
        )
    }

    override fun listPipelineRuns(data: Map<GitProviderProperties, List<PipelineRun>>) {
        retConsole.out(
            objectMapper.writeValueAsString(
                if (data.all { it.value.isEmpty() }) {
                    Wrapper(listOf(Item("No pipeline runs found", valid = false)))
                } else {Wrapper(
                    listOf(Item(title = "Pipeline run overview", arg = "open-dashboard")) +
                            data.flatMap { entry ->
                                entry.value.map {
                                    Item(
                                        title = it.name,
                                        subtitle = "State: ${it.state}, result: ${it.result}",
                                        icon = ItemIcon("icons/${it.icon()}"),
                                        arg = "${entry.key}:${it.id}",
                                    )
                                }
                            },
                )},
            ),
        )
    }

    private fun PipelineRun.icon() =
        if (state == PipelineRunState.COMPLETED) {
            if (result == PipelineRunResult.SUCCEEDED) "succeeded.png" else "failed.png"
        } else {
            "in_progress.png"
        }
}
