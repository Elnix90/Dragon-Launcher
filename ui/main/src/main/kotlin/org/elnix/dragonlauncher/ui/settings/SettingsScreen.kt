package org.elnix.dragonlauncher.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import io.github.elnix90.annotations.SettingEntry
import io.github.elnix90.annotations.SettingScreen
import io.github.elnix90.core.util.clearAllData
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.search.SearchResult
import io.github.elnix90.runtime.search.SettingsSearchIndex
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.animation.bouncySpec
import io.github.elnix90.shared.ui.base.asState
import io.github.elnix90.shared.ui.base.components.BoxedIcon
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonButton
import io.github.elnix90.shared.ui.dragon.components.DragonGroupScope
import io.github.elnix90.shared.ui.dragon.components.DragonIconButton
import io.github.elnix90.shared.ui.dragon.components.DragonModalBottomSheet
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.text.TextWithDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.ScrollbarSettings
import org.elnix.dragonlauncher.base.Constants.URLs.CODEBERG_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.DISCORD_INVITE_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.DOCUMENTATION_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.DRAGON_WEBSITE
import org.elnix.dragonlauncher.base.Constants.URLs.ELNIX90_BUY_ME_A_COFFEE
import org.elnix.dragonlauncher.base.Constants.URLs.ELNIX90_GITHUB_PROFILE_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.EXTENSIONS_GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_ISSUES_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_RELEASES_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITLAB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.MAILTO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.REDDIT_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.TELEGRAM_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.WEBLATE_LINK
import org.elnix.dragonlauncher.base.model.models.SocialLink
import org.elnix.dragonlauncher.base.model.models.buyMeACoffee
import org.elnix.dragonlauncher.base.model.models.codeberg
import org.elnix.dragonlauncher.base.model.models.github
import org.elnix.dragonlauncher.base.model.models.gitlab
import org.elnix.dragonlauncher.base.model.models.openInNew
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.LifecycleUtils.closeApp
import org.elnix.dragonlauncher.base.utils.VersionsUtils
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getBuildType
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionNumber
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.components.BuildTypeChip
import org.elnix.dragonlauncher.ui.components.CodeNameChip
import org.elnix.dragonlauncher.ui.components.LocalePickerSheet
import org.elnix.dragonlauncher.ui.components.VersionCodeChip
import org.elnix.dragonlauncher.ui.components.VersionNumberChip
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.helpers.settings.ContributorItem
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.settings.SplitSettingsItem
import org.elnix.dragonlauncher.ui.settings.search.AllSettingsUsage
import org.elnix.dragonlauncher.ui.warning.GoogleWarningManager
import org.elnix.dragonlauncher.ui.warning.GoogleWarningReminder
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalGridApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("LocalContextGetResourceValueCall")
@SettingScreen(NavigationRoute.Settings::class)
@Composable
fun SettingsScreen(
	viewModel: SettingsScreenViewModel,
	securityViewModel: SecurityViewModel = activityViewModel()
) {
	val ctx = LocalContext.current
	val uriHandler = LocalUriHandler.current
	val navigator = LocalNavigator.current
	val windowInfo = LocalWindowInfo.current
	val focusManager = LocalFocusManager.current

	val scope = rememberCoroutineScope()
	val focusRequester = remember { FocusRequester() }

	val versionCode = ctx.getVersionCode()
	val versionName = ctx.getVersionNumber()

	var isDebugModeEnabled by DebugSettingsStore.debugEnabled.asMutableState()

	var showLanguageSheet by viewModel.showLanguageSheet
	var showBottomSearch by viewModel.showBottomSearch

	SettingsScaffold(
		title = stringResource(R.string.settings),
		helpText = stringResource(R.string.settings),
		resetTitle = stringResource(R.string.reset_all_settings),
		resetText = stringResource(R.string.every_setting_will_return_to_its_default_state_this_cannot_be_undone_the_app_will_kill_itself),
		onReset = {
			scope.launch {
				clearAllData(ctx)
				closeApp(ctx as ComponentActivity)
			}
		},
		imePadding = false,
		scrollableContent = false
	) {
		Box {
			Column(
				verticalArrangement = Arrangement.spacedBy(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				val showGoogleLockDownWarning by DebugSettingsStore.showGoogleLockDownWarning.asState()
				if (GoogleWarningManager.showWarning() && showGoogleLockDownWarning && ctx.getBuildType() != VersionsUtils.BuildType.playstore) {
					GoogleWarningReminder()
				}

				val showBetaVersionWarning by viewModel.showBetaVersionWarning.collectAsState()
				AnimatedVisibility(showBetaVersionWarning) {
					BetaVersionWarning(BetaVersionType.App)
				}

				val signatureMatched by securityViewModel.signatureMatched.asState()
				AnimatedVisibility(!signatureMatched) {
					BetaVersionWarning(BetaVersionType.Custom(R.string.signature_not_matched))
				}

				DragonSettingsGroup(R.string.common_settings) {
					RouteItem(NavigationRoute.Appearance)
					RouteItem(NavigationRoute.Wallpaper)
					RouteItem(NavigationRoute.Widgets)
					RouteItem(NavigationRoute.Behavior)
					RouteItem(NavigationRoute.Backup)
					RouteItem(NavigationRoute.DrawerSettings)
					RouteItem(NavigationRoute.Wellbeing)

					val forceAppLanguageSelector by DebugSettingsStore.forceAppLanguageSelector.asState()
					SettingsItem(
						title = R.string.language,
						icon = R.drawable.web,
						onClick = {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !forceAppLanguageSelector) {
								val intent =
									Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
										data = Uri.fromParts("package", ctx.packageName, null)
									}
								ctx.startActivity(intent)
							} else {
								showLanguageSheet = true
							}
						}
					)
				}

				DragonSettingsGroup(R.string.advanced) {
					RouteItem(
						route = NavigationRoute.Extensions,
						github(EXTENSIONS_GITHUB_REPO_LINK)
					)

					SettingsItem(
						title = R.string.android_settings,
						icon = R.drawable.settings_alert
					) {
						val packageName = ctx.packageName
						val intent =
							Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
								data = Uri.fromParts("package", packageName, null)
							}
						ctx.startActivity(intent)
					}

					RouteItem(NavigationRoute.Permissions)
					AnimatedVisibility(isDebugModeEnabled) {
						RouteItem(NavigationRoute.Debug)
					}
				}

				DragonSettingsGroup(R.string.about) {
					Row(
						modifier =
							Modifier
								.height(60.dp)
								.dragonSettingGroup(padding = false),
						horizontalArrangement = Arrangement.SpaceEvenly,
						verticalAlignment = Alignment.CenterVertically
					) {
						@SettingEntry
						fun ButtonGroupScope.ic(
							@DrawableRes icon: Int,
							@StringRes title: Int,
							link: String,
							interactionSource: MutableInteractionSource
						) {
							customItem(
								buttonGroupContent = {
									Box(
										modifier =
											Modifier
												.weight(1f)
												.animateWidth(interactionSource)
												.fillMaxHeight()
												.clip(MaterialTheme.shapes.extraLarge)
												.clickable(interactionSource = interactionSource) { uriHandler.openUri(link) }
												.padding(vertical = 8.dp),
										contentAlignment = Alignment.Center
									) {
										Icon(
											painter = painterResource(icon),
											contentDescription = stringResource(title),
											tint = Color.Unspecified,
											modifier = Modifier.size(32.dp)
										)
									}
								}
							) {}
						}

						val githubIcon =
							if (MaterialTheme.colorScheme.background.luminance() < 0.5) {
								R.drawable.github_invertocat_white
							} else {
								R.drawable.github_invertocat_black
							}

						val interactionSources = remember { List(7) { MutableInteractionSource() } }
						ButtonGroup(
							overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
							horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
						) {
							ic(githubIcon, R.string.github, GITHUB_REPO_LINK, interactionSources[0])
							ic(R.drawable.discord_symbol_blurple, R.string.discord, DISCORD_INVITE_LINK, interactionSources[1])
							ic(R.drawable.reddit_icon_fullcolor, R.string.reddit, REDDIT_LINK, interactionSources[2])
							ic(R.mipmap.dragon_launcher_foreground, R.string.website, DRAGON_WEBSITE, interactionSources[3])
							ic(R.drawable.weblate_icon, R.string.weblate, WEBLATE_LINK, interactionSources[4])
							ic(R.drawable.protonmail_icon, R.string.mail, MAILTO_LINK, interactionSources[5])
							ic(R.drawable.telegram, R.string.telegram, TELEGRAM_LINK, interactionSources[6])
						}
					}

					SettingsItem(
						title = R.string.source_code,
						description = null,
						icon = R.drawable.code,
						github(GITHUB_REPO_LINK),
						gitlab(GITLAB_REPO_LINK),
						codeberg(CODEBERG_REPO_LINK)
					) { uriHandler.openUri(GITHUB_REPO_LINK) }

					SettingsItem(
						title = R.string.documentation,
						description = R.string.documentation_help,
						icon = R.drawable.unknown_document,
						openInNew(DOCUMENTATION_LINK)
					) { uriHandler.openUri(DOCUMENTATION_LINK) }

					SplitSettingsItem(
						title = R.string.changelogs,
						description = R.string.see_changelogs,
						icon = R.drawable.source_notes,
						openInNew("$GITHUB_REPO_LINK/blob/main/fastlane/metadata/android/en-US/changelogs/$versionCode.txt"),
						firstHalfEnabled = true,
						firstHalfOnClick = { navigator.navigate(NavigationRoute.Changelogs) }
					)

					SettingsItem(
						title = R.string.check_for_update,
						description = R.string.check_for_updates_github,
						icon = R.drawable.reset,
						openInNew(GITHUB_REPO_RELEASES_LINK)
					) { uriHandler.openUri(GITHUB_REPO_RELEASES_LINK) }

					SettingsItem(
						title = R.string.report_a_bug,
						description = R.string.open_an_issue_on_github,
						icon = R.drawable.report,
						openInNew(GITHUB_REPO_ISSUES_LINK)
					) { uriHandler.openUri(GITHUB_REPO_ISSUES_LINK) }
				}

				DragonSettingsGroup(R.string.app_developer) {
					ContributorItem(
						name = "Elnix90",
						shape = MaterialShapes.Circle,
						imageRes = R.mipmap.elnix90,
						description = stringResource(R.string.app_developer),
						github(ELNIX90_GITHUB_PROFILE_LINK),
						buyMeACoffee(ELNIX90_BUY_ME_A_COFFEE)
					)
				}

				DragonSettingsGroup(R.string.contributors) {
					ContributorItem(
						name = "YoannDev90",
						shape = MaterialShapes.Gem,
						imageRes = R.mipmap.yoanndev90,
						description = stringResource(R.string.yoann_desc),
						github("https://github.com/YoannDev90"),
						buyMeACoffee("https://buymeacoffee.com/yoanndev90")
					)

					// TODO write script to fetch total lines added / removed and display them per user
					ContributorItem(
						name = "Lucky",
						shape = MaterialShapes.Cookie7Sided,
						imageRes = R.mipmap.lucky_the_cookie,
						description = stringResource(R.string.lucky_desc),
						github("https://lthb.fr")
					)

					ContributorItem(
						name = "Federico",
						shape = MaterialShapes.Pill,
						imageRes = R.mipmap.federico,
						description = stringResource(R.string.federico_desc),
						github("https://github.com/federicobuttafuori")
					)
				}

				DragonSettingsGroup(R.string.translators) {
					val translators =
						listOf(
							SocialLink("https://github.com/manmen2414", R.mipmap.mameeenn),
							SocialLink("https://github.com/acress1", R.mipmap.acress1),
							SocialLink("https://github.com/TamilNeram", R.mipmap.tamilneram),
							SocialLink("https://github.com/sudo-py-dev", R.mipmap.sudopydev),
							SocialLink("https://github.com/fatihinur", R.mipmap.fatihinur),
							SocialLink("https://github.com/pentakore", R.mipmap.pentakore),
							SocialLink("https://github.com/vasesm", R.mipmap.vasesm),
							SocialLink("https://github.com/ak1for2business-prog", R.mipmap.ak1for2businessprog)
						)

					Grid(
						config = {
							repeat(7) { column(1.fr) }
							gap(row = 15.dp, column = 0.dp)
						},
						modifier = Modifier.dragonSettingGroup()
					) {
						translators.forEach { translator ->
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.clip(MaterialTheme.shapes.medium)
									.clickable {
										uriHandler.openUri(translator.url)
									},
								contentAlignment = Alignment.Center
							) {
								Image(
									painter = painterResource(translator.icon),
									contentDescription = null,
									contentScale = ContentScale.Fit,
									modifier =
										Modifier
											.size(40.dp)
											.clip(CircleShape)
								)
							}
						}
					}

					DragonButton(
						onClick = {
							uriHandler.openUri(WEBLATE_LINK)
						}
					) {
						Image(
							painter = painterResource(R.drawable.weblate_icon),
							contentDescription = stringResource(R.string.contribute_in_translations),
							modifier = Modifier.size(32.dp)
						)
						Spacer(5.dp)
						Text(
							text = stringResource(R.string.contribute_in_translations),
							style = MaterialTheme.typography.labelMediumEmphasized
						)
					}
				}

				Card(shape = MaterialTheme.shapes.extraLarge) {
					Column(
						modifier =
							Modifier
								.padding(16.dp)
								.fillMaxWidth(),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							verticalAlignment = Alignment.CenterVertically
						) {
							Box(
								modifier =
									Modifier
										.weight(1f)
										.aspectRatio(1f)
										.size(140.dp)
										.background(MaterialTheme.colorScheme.background, MaterialShapes.Cookie9Sided.toShape())
										.padding(20.dp)
							) {
								Image(
									painter = painterResource(R.mipmap.dragon_launcher_foreground),
									contentDescription = stringResource(R.string.app_name),
									modifier = Modifier.fillMaxSize()
								)
							}
							Spacer(16.dp)

							Column(
								modifier =
									Modifier
										.weight(1f)
										.fillMaxWidth(),
								verticalArrangement = Arrangement.spacedBy(8.dp)
							) {
								var toast by remember { mutableStateOf<Toast?>(null) }
								var timesClickedOnVersion by remember { mutableIntStateOf(0) }

								TextRow(stringResource(R.string.version), {
									VersionNumberChip(
										modifier =
											Modifier
												.clickable(
													indication = null,
													interactionSource = remember { MutableInteractionSource() }
												) {
													toast?.cancel()

													when {
														timesClickedOnVersion == 0 -> {
															timesClickedOnVersion += 1

															ctx.copyToClipboard(versionName)
															toast =
																Toast.makeText(
																	ctx,
																	ctx.getString(R.string.copied_to_clipboard),
																	Toast.LENGTH_SHORT
																)
															toast?.show()
														}

														isDebugModeEnabled -> {
															toast =
																Toast.makeText(
																	ctx,
																	ctx.getString(R.string.debug_mode_already_enabled),
																	Toast.LENGTH_SHORT
																)
															toast?.show()
														}

														timesClickedOnVersion < 6 -> {
															timesClickedOnVersion++
															if (timesClickedOnVersion > 2) {
																toast =
																	Toast.makeText(
																		ctx,
																		"${7 - timesClickedOnVersion} more times to enable Debug Mode",
																		Toast.LENGTH_SHORT
																	)
															}
															toast?.show()
														}

														else -> {
															isDebugModeEnabled = true
														}
													}
												}
									)
								})

								TextRow(stringResource(R.string.code_name_string), { CodeNameChip() })
								TextRow(stringResource(R.string.version_code), { VersionCodeChip() })
								TextRow(stringResource(R.string.build_type), { BuildTypeChip() })
							}
						}

						Text(
							text = stringResource(R.string.app_name),
							style = MaterialTheme.typography.titleLargeEmphasized,
							textAlign = TextAlign.Center,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(
							text = stringResource(R.string.app_tagline),
							style = MaterialTheme.typography.bodyLargeEmphasized,
							textAlign = TextAlign.Center,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}

				Spacer(100.dp)
			}

			val oneFourthOfHeight = windowInfo.containerSize.height / 4f
			val yTranslationSearchbar = remember { Animatable(0f) }

			fun onOpen() {
				scope.launch {
					yTranslationSearchbar.snapTo(0f)
				}
				focusManager.clearFocus(true)
				showBottomSearch = true
			}

			val searchbarDragState = rememberDraggableState { dragAmount ->
				scope.launch {
					// reminder: dragging up gives a negative value
					val newValue = yTranslationSearchbar.value + dragAmount
					val upLimit = -oneFourthOfHeight

					val finalValue = if (newValue < 0) {
						newValue.coerceAtLeast(upLimit)
					} else {
						newValue
					}

					yTranslationSearchbar.snapTo(finalValue)
				}
			}

			val progress by remember {
				derivedStateOf {
					if (yTranslationSearchbar.value <= 0) {
						(yTranslationSearchbar.value.absoluteValue / oneFourthOfHeight).coerceIn(0f, 1f)
					} else {
						0f // means we're dragging down
					}
				}
			}

			val searchbarWidth by animateFloatAsState(
				targetValue = lerp(0.85f, 0.95f, progress),
				animationSpec = bouncySpec()
			)

			TextField(
				state = viewModel.textFieldState,
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth(searchbarWidth)
					.navigationBarsPadding()
					.padding(5.dp)
					.draggable(
						state = searchbarDragState,
						orientation = Orientation.Vertical,
						onDragStopped = {
							val targetValue = yTranslationSearchbar.value

							if (targetValue <= -oneFourthOfHeight / 2) {
								onOpen()
							}

							scope.launch {
								yTranslationSearchbar.animateTo(0f, bouncySpec())
							}
						}
					).onFocusChanged {
						if (it.isFocused) {
							onOpen()
						}
					}.graphicsLayer {
						translationY = yTranslationSearchbar.value
					},
				colors = AppObjectsColors.outlinedTextFieldColors(),
				placeholder = {
					Text(stringResource(R.string.search_settings))
				},
				leadingIcon = {
					Icon(
						painter = painterResource(R.drawable.search),
						contentDescription = null
					)
				},
				// Needed for the text field to be vertically scrollable, see its doc
				// I spend too much time on that shit :(
				lineLimits = TextFieldLineLimits.SingleLine,
				shape = FloatingToolbarDefaults.ContainerShape
			)
		}
	}

	if (showLanguageSheet) {
		LocalePickerSheet { showLanguageSheet = false }
	}

	if (showBottomSearch) {
		var isFirstGet by remember { mutableStateOf(true) }
		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}

		var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
		val index = SettingsSearchIndex(AllSettingsUsage)

		val searchQuery = viewModel.textFieldState.text.toString()
		LaunchedEffect(searchQuery) {
			if (!isFirstGet) {
				delay(500.milliseconds)
			} else {
				isFirstGet = false
			}
			results = withContext(Dispatchers.IO) { index.search(ctx, searchQuery) }
		}

		DragonModalBottomSheet(
			onDismissRequest = {
				showBottomSearch = false
				viewModel.textFieldState.clearText()
				focusManager.clearFocus(true)
			},
			skipPartiallyExpanded = true
		) {
			TextField(
				state = viewModel.textFieldState,
				placeholder = {
					Text(stringResource(R.string.search_settings))
				},
				leadingIcon = {
					Icon(
						painter = painterResource(R.drawable.search),
						contentDescription = stringResource(R.string.search_settings)
					)
				},
				trailingIcon = {
					Icon(
						painter = painterResource(R.drawable.close),
						tint = MaterialTheme.colorScheme.error,
						contentDescription = stringResource(R.string.clear),
						modifier = Modifier
							.clip(CircleShape)
							.clickable {
								if (viewModel.textFieldState.text.isNotEmpty()) {
									viewModel.textFieldState.clearText()
								} else {
									showBottomSearch = false
									viewModel.textFieldState.clearText()
									focusManager.clearFocus(true)
								}
							}.padding(5.dp)
					)
				},
				colors = AppObjectsColors.outlinedTextFieldColors(),
				shape = CircleShape,
				modifier = Modifier
					.fillMaxWidth()
					.focusRequester(focusRequester)
			)

			val thumbColor = MaterialTheme.colorScheme.primary

			val scrollBarSettings = remember {
				ScrollbarSettings(
					alwaysShowScrollbar = true,
					thumbThickness = 10.dp,
					thumbUnselectedColor = thumbColor.alphaMultiplier(0.5f),
					thumbSelectedColor = thumbColor,
					selectionMode = ScrollbarSelectionMode.Full
				)
			}
			val lazyListState = rememberLazyListState()

			Spacer(5.dp)
			LazyColumnScrollbar(lazyListState, settings = scrollBarSettings) {
				DragonSettingsGroup {
					var isGloballyExpanded by remember { mutableStateOf(false) }

					LazyColumn(
						state = lazyListState,
						verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
						modifier = Modifier.fillMaxSize()
					) {
						if (isDebugModeEnabled) {
							item {
								Row(
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween,
									modifier = Modifier
										.dragonSettingGroup()
								) {
									Text(
										text = stringResource(R.string.n_settings_found, results.size),
										style = MaterialTheme.typography.bodyLargeEmphasized
									)
									DragonIconButton(
										icon = R.drawable.arrow_drop_down,
										contentDescription = R.string.more
									) { isFirstGet = !isGloballyExpanded }
								}
							}
						}

						items(
							items = results,
							key = { it.key }
						) { result ->
							SearchResultRow(
								result = result,
								isDebugModeEnabled = isDebugModeEnabled,
								isGloballyExpanded = isGloballyExpanded,
								onClick = {
									val screen = result.screenRoute as? NavigationRoute ?: return@SearchResultRow

									if (screen == NavigationRoute.Settings) {
										// This screen, close the search
										showBottomSearch = false
										viewModel.textFieldState.clearText()
									}
									navigator.navigate(screen)
								}
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun TextRow(
	text: String,
	tag: @Composable RowScope.() -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(5.dp)
	) {
		Text(
			text = "$text:",
			style = MaterialTheme.typography.bodySmallEmphasized
		)
		tag()
	}
}

@Composable
private fun DragonGroupScope.SearchResultRow(
	result: SearchResult,
	isDebugModeEnabled: Boolean,
	isGloballyExpanded: Boolean,
	onClick: () -> Unit
) {
	var expanded by remember { mutableStateOf(false) }

	Column(
		Modifier
			.dragonSettingGroup {
				clickable(onClick = onClick)
			},
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			result.icon?.let { icon ->
				BoxedIcon(icon)
			}

			TextWithDescription(
				text = result.title,
				description = result.description,
				modifier = Modifier.weight(1f)
			)

			if (result.screenRoute is NavigationRoute) {
				Icon(
					painter = painterResource(R.drawable.open_in_new),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.secondary
				)
			}

			if (isDebugModeEnabled || isGloballyExpanded) {
				DragonIconButton(
					icon = R.drawable.arrow_drop_down,
					contentDescription = R.string.more
				) { expanded = !expanded }
			}
		}

		AnimatedVisibility(expanded) {
			Column(
				verticalArrangement = Arrangement.spacedBy(5.dp)
			) {
				CompositionLocalProvider(
					LocalTextStyle provides MaterialTheme.typography.labelSmallEmphasized.copy(fontFamily = FontFamily.Monospace)
				) {
					Text(result.screenRoute.toString())
					Text(result.screenFqName)
					Text(result.key)
					Text("Score: ${result.score}")
				}
			}
		}
	}
}
