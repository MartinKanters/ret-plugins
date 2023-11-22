package io.rabobank.ret.git.plugin.provider

import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsClient
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsPluginConfig
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsProvider
import io.rabobank.ret.git.plugin.provider.azure.AzureDevopsUrlFactory
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class GitProviderSelector(
    azureDevopsClient: AzureDevopsClient,
    pluginConfig: AzureDevopsPluginConfig,
    azureDevopsUrlFactory: AzureDevopsUrlFactory,
) {
    private val azureDevOpsGitProvider = AzureDevopsProvider(azureDevopsClient, pluginConfig, azureDevopsUrlFactory)
    private val allProviders: List<GitProvider> = listOfNotNull(azureDevOpsGitProvider)

    fun all() = allProviders

    fun byKey(key: GitProviderProperties): GitProvider = allProviders.first { key == it.providerProperties }
}
