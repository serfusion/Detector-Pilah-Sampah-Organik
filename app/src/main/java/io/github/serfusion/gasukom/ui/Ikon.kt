package io.github.serfusion.gasukom.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Ikon senter (petir), digambar sendiri agar tidak perlu pustaka ikon lengkap yang besar. */
val IkonSenter: ImageVector by lazy {
    ImageVector.Builder(
        name = "Senter",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 2f)
            verticalLineTo(13f)
            horizontalLineTo(10f)
            verticalLineTo(22f)
            lineTo(17f, 10f)
            horizontalLineTo(13f)
            lineTo(17f, 2f)
            close()
        }
    }.build()
}
