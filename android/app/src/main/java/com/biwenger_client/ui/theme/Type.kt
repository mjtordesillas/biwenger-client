package com.biwenger_client.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.biwenger_client.R

// Inter, bundled at the weights Nocturne actually uses (400/500/600/700) —
// see readme.md's "--font-heading" / "--font-body" (both Inter).
val Inter = FontFamily(
    Font(R.font.inter_400, FontWeight.Normal),
    Font(R.font.inter_500, FontWeight.Medium),
    Font(R.font.inter_600, FontWeight.SemiBold),
    Font(R.font.inter_700, FontWeight.Bold),
)

// --font-heading-weight is 500 system-wide — headings never bolden past
// it (readme: "hierarchy here is size and space").
val HeadingWeight = FontWeight.Medium

val Typography = Typography(
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = HeadingWeight, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = HeadingWeight, fontSize = 20.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.5.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 11.5.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 10.5.sp),
)
