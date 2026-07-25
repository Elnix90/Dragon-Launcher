package org.elnix.dragonlauncher.ui.dragon.internals

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults.connectedButtonCheckedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

/** Default shape for the leading button in a connected button group */
val connectedTopButtonShape: Shape
    @Composable
    get() =
        RoundedCornerShape(
            topStart = ShapeDefaultsClone.CornerFull,
            bottomStart = ConnectedButtonGroupSmallTokens.InnerCornerCornerSize,
            topEnd = ShapeDefaultsClone.CornerFull,
            bottomEnd = ConnectedButtonGroupSmallTokens.InnerCornerCornerSize,
        )

/** Default shape for the pressed state for the leading button in a connected button group. */
val connectedTopButtonPressShape: Shape
    @Composable
    get() =
        RoundedCornerShape(
            topStart = ShapeDefaultsClone.CornerFull,
            bottomStart = ConnectedButtonGroupSmallTokens.PressedInnerCornerCornerSize,
            topEnd = ShapeDefaultsClone.CornerFull,
            bottomEnd = ConnectedButtonGroupSmallTokens.PressedInnerCornerCornerSize,
        )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun connectedTopButtonShapes(
    shape: Shape = connectedTopButtonShape,
    pressedShape: Shape = connectedTopButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
): ToggleButtonShapes =
    ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)





/** Default shape for the leading button in a connected button group */
val connectedBottomButtonShape: Shape
    @Composable
    get() =
        RoundedCornerShape(
            topStart = ConnectedButtonGroupSmallTokens.InnerCornerCornerSize,
            bottomStart = ShapeDefaultsClone.CornerFull,
            topEnd = ConnectedButtonGroupSmallTokens.InnerCornerCornerSize,
            bottomEnd =  ShapeDefaultsClone.CornerFull,
        )

/** Default shape for the pressed state for the leading button in a connected button group. */
val connectedBottomButtonPressShape: Shape
    @Composable
    get() =
        RoundedCornerShape(
            topStart = ConnectedButtonGroupSmallTokens.PressedInnerCornerCornerSize,
            bottomStart = ShapeDefaultsClone.CornerFull,
            topEnd = ConnectedButtonGroupSmallTokens.PressedInnerCornerCornerSize,
            bottomEnd =  ShapeDefaultsClone.CornerFull,
        )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun connectedBottomButtonShapes(
    shape: Shape = connectedBottomButtonShape,
    pressedShape: Shape = connectedBottomButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
): ToggleButtonShapes =
    ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)
