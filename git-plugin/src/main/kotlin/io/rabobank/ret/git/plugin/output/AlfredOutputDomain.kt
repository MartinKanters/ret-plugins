package io.rabobank.ret.git.plugin.output

import io.quarkus.runtime.annotations.RegisterForReflection
import io.rabobank.ret.git.plugin.provider.Branch
import io.rabobank.ret.git.plugin.provider.GitProviderProperties
import io.rabobank.ret.git.plugin.provider.PullRequest
import io.rabobank.ret.git.plugin.provider.Repository

@RegisterForReflection
data class Wrapper(val items: List<Item>)

@RegisterForReflection
data class ItemIcon(val path: String)

@RegisterForReflection
data class Item(
    val title: String,
    val arg: String? = null,
    val subtitle: String = "",
    val icon: ItemIcon? = null,
    val valid: Boolean = true,
) {
    constructor(message: String) :
        this(
            title = message,
        )

    constructor(message: String, valid: Boolean) :
        this(
            title = message,
            valid = valid,
        )

    constructor(gitProviderProperties: GitProviderProperties, pr: PullRequest) :
        this(
            title = pr.title,
            subtitle = "${pr.repository.name} - ${gitProviderProperties.fullName}",
            arg = "${gitProviderProperties.name}:${pr.id}",
            icon = ItemIcon("icons/pull_request.png"),
        )

    constructor(gitProviderProperties: GitProviderProperties, repo: Repository) :
        this(
            title = repo.name,
            subtitle = gitProviderProperties.fullName,
            arg = "${gitProviderProperties.name}:${repo.name}",
            icon = ItemIcon("icons/icon_repo.png"),
        )

    constructor(gitProviderProperties: GitProviderProperties, branch: Branch) :
        this(
            title = branch.name,
            arg = "${gitProviderProperties.name}:${branch.name}",
        )
}
