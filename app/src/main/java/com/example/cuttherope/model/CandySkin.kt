package com.example.cuttherope.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R

enum class CandyType(
    val id: String,
    val displayName: String,
    val requiredStars: Int,
    @DrawableRes val drawableRes: Int?,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String
) {
    CLASSIC(
        id = "classic",
        displayName = "Classic Swirl",
        requiredStars = 0,
        drawableRes = R.drawable.candy_classic,
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFFFFD54F),
        description = "Om Nom's all-time favorite red-and-yellow swirl candy!"
    ),
    BERRY_ORB(
        id = "berry_orb",
        displayName = "Berry Galaxy",
        requiredStars = 5,
        drawableRes = null,
        primaryColor = Color(0xFF7E57C2),
        secondaryColor = Color(0xFFEC407A),
        description = "Sweet cosmic blackberry spiral with sparkling star dust."
    ),
    SUNNY_CITRUS(
        id = "sunny_citrus",
        displayName = "Sunny Citrus",
        requiredStars = 10,
        drawableRes = null,
        primaryColor = Color(0xFFFF9800),
        secondaryColor = Color(0xFFFFEB3B),
        description = "Tangy orange and lemon pinwheel full of vitamins!"
    ),
    MINT_FRESH(
        id = "mint_fresh",
        displayName = "Peppermint Drop",
        requiredStars = 15,
        drawableRes = null,
        primaryColor = Color(0xFF00BFA5),
        secondaryColor = Color(0xFFE0F2F1),
        description = "Cool refreshing icy peppermint with emerald stripes."
    ),
    GOLDEN_STAR(
        id = "golden_star",
        displayName = "Golden Royal Star",
        requiredStars = 20,
        drawableRes = null,
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFFF8F00),
        description = "Pure candied honey shaped into a glorious celestial star."
    ),
    RAINBOW_SURPRISE(
        id = "rainbow_surprise",
        displayName = "Rainbow Swirl",
        requiredStars = 25,
        drawableRes = null,
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFF00E676),
        description = "Six-flavored carnival jawbreaker bursting with joy!"
    ),
    COSMIC_DONUT(
        id = "cosmic_donut",
        displayName = "Choco Donut Candy",
        requiredStars = 30,
        drawableRes = null,
        primaryColor = Color(0xFF5D4037),
        secondaryColor = Color(0xFFFF4081),
        description = "Mini frosted chocolate glazed snack with rainbow sprinkles."
    )
}
