package io.rabobank.ret.git.plugin.provider

data class GitProviderProperties(
    val providerName: String,
    val pipelinesTiedToRepository: Boolean
)
