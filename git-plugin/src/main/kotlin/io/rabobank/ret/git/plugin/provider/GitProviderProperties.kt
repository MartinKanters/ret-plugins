package io.rabobank.ret.git.plugin.provider

enum class GitProviderProperties(val fullName: String,
                                 val pipelinesTiedToRepository: Boolean) {
    AZDO("Azure Devops", false)
}

fun String.splitByProviderKeyAndValue(): Pair<GitProviderProperties, String> {
    require(this.contains(":"))
    val gitProviderKeyName = this.substringBefore(":")
    return GitProviderProperties.valueOf(gitProviderKeyName) to this.substringAfter(":")
}
