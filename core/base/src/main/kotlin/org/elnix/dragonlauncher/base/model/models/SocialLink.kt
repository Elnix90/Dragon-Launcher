package org.elnix.dragonlauncher.base.model.models

import org.elnix.dragonlauncher.i18n.R

/**
 * Data class used to link an URL to an icon in the res folder
 */
data class SocialLink(
	val url: String,
	val icon: Int
)

fun buyMeACoffee(url: String): SocialLink =
	SocialLink(
		url = url,
		icon = R.drawable.buy_me_a_coffee
	)

fun github(url: String): SocialLink =
	SocialLink(
		url = url,
		icon = R.drawable.github_invertocat_white
	)

fun gitlab(url: String): SocialLink =
	SocialLink(
		url = url,
		icon = R.drawable.gitlab_logo_500_rgb
	)

fun codeberg(url: String): SocialLink =
	SocialLink(
		url = url,
		icon = R.drawable.codeberg_logo_icon_blue
	)

fun openInNew(url: String): SocialLink =
	SocialLink(
		url = url,
		icon = R.drawable.open_in_new
	)
