package io.rabobank.ret.git.plugin.output

import com.fasterxml.jackson.databind.ObjectMapper
import io.rabobank.ret.RetConsole
import io.rabobank.ret.git.plugin.provider.*

class AlfredOutputHandler(private val retConsole: RetConsole, private val objectMapper: ObjectMapper) : OutputHandler {
    override fun listPRs(data: Map<GitProviderProperties, List<PullRequest>>) {
        throw UnsupportedOperationException()
    }

    override fun error(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item("Error: $message", false)))))
    }

    override fun println(message: String) {
        retConsole.out(objectMapper.writeValueAsString(Wrapper(listOf(Item(message)))))
    }

    override fun listRepositories(data: Map<GitProviderProperties, List<Repository>>) {
        throw UnsupportedOperationException()
    }

    override fun listBranches(data: Map<GitProviderProperties, List<Branch>>) {
        throw UnsupportedOperationException()
    }

    override fun listPipelines(data: Map<GitProviderProperties, List<Pipeline>>) {
        throw UnsupportedOperationException()
    }

    override fun listPipelineRuns(data: Map<GitProviderProperties, List<PipelineRun>>) {
        throw UnsupportedOperationException()
    }
}
