package com.hal1ucinogen.systembarsmodernizer.feature.applist.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.marginStart
import com.google.android.material.card.MaterialCardView
import com.hal1ucinogen.systembarsmodernizer.R
import com.hal1ucinogen.systembarsmodernizer.util.dp
import com.hal1ucinogen.systembarsmodernizer.util.getColor
import com.hal1ucinogen.systembarsmodernizer.util.getColorByAttr
import com.hal1ucinogen.systembarsmodernizer.util.getDimensionPixelSize
import com.hal1ucinogen.systembarsmodernizer.util.getDrawable
import com.hal1ucinogen.systembarsmodernizer.view.AViewGroup

class AppItemView(context: Context) : MaterialCardView(context) {

    val container = AppItemContainerView(context).apply {
        val padding = context.getDimensionPixelSize(R.dimen.main_card_padding)
        setPadding(padding, padding, padding, padding)
    }

    private val floatView by lazy {
        AppCompatTextView(
            ContextThemeWrapper(
                context,
                R.style.TextView_SansSerifCondensedMedium
            )
        ).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER
                it.topMargin = 24.dp
                it.bottomMargin = 24.dp
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    }

    init {
        radius = context.resources.getDimension(R.dimen.main_card_corner_radius)
        cardElevation = 0f
        strokeWidth = 0
        strokeColor = Color.TRANSPARENT
        val cardBgColor = runCatching {
            context.getColorByAttr(com.google.android.material.R.attr.colorSurfaceContainerLow)
        }.getOrElse {
            context.getColorByAttr(com.google.android.material.R.attr.colorSurfaceVariant)
        }
        setCardBackgroundColor(cardBgColor)
        isClickable = true
        isFocusable = true
        addView(container)
    }

    fun addFloatView(text: String) {
        if (container.parent != null) {
            removeView(container)
        }
        if (floatView.parent == null) {
            addView(floatView)
        }
        floatView.text = text
    }

    class AppItemContainerView(context: Context) : AViewGroup(context) {

        val icon = AppCompatImageView(context).apply {
            val iconSize = context.getDimensionPixelSize(R.dimen.app_icon_size)
            layoutParams = LayoutParams(iconSize, iconSize)
            addView(this)
        }

        val appName = AppCompatTextView(
            ContextThemeWrapper(
                context,
                R.style.TextView_SansSerifMedium
            )
        ).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.marginStart = 12.dp
            }
            setTextColor(context.getColorByAttr(com.google.android.material.R.attr.colorOnSurface))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            addView(this)
        }

        val packageName =
            AppCompatTextView(
                ContextThemeWrapper(
                    context,
                    R.style.TextView_SansSerif
                )
            ).apply {
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.getColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
                addView(this)
            }

        val versionInfo = AppCompatTextView(
            ContextThemeWrapper(
                context,
                R.style.TextView_SansSerifCondensed
            )
        ).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val outlineColor = runCatching {
                context.getColorByAttr(com.google.android.material.R.attr.colorOutline)
            }.getOrElse {
                android.R.color.darker_gray.getColor(context)
            }
            setTextColor(outlineColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            addView(this)
        }

        val statusBadge = AppCompatTextView(
            ContextThemeWrapper(
                context,
                R.style.TextView_SansSerifMedium
            )
        ).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(8.dp, 3.dp, 8.dp, 3.dp)
            maxLines = 1
            isGone = true
            addView(this)
        }

        val abiInfo = AppCompatTextView(
            ContextThemeWrapper(
                context,
                R.style.TextView_SansSerifCondensedMedium
            )
        ).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 2.dp)
            setTextColor(android.R.color.darker_gray.getColor(context))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            addView(this)
        }

        private var badge: AppCompatImageView? = null

        fun setAppName(text: String) {
            appName.text = text
            appName.setItemBackground()
        }

        fun setPackageName(text: String) {
            packageName.text = text
            packageName.setItemBackground()
        }

        fun setVersionInfo(text: String) {
            versionInfo.text = text
            versionInfo.setItemBackground()
        }

        fun setAbiInfo(text: String) {
            abiInfo.text = text
            abiInfo.setItemBackground()
        }

        fun setStatusBadge(text: String?, isPrimary: Boolean = true) {
            if (text.isNullOrBlank()) {
                statusBadge.isGone = true
            } else {
                statusBadge.isGone = false
                statusBadge.text = text
                if (isPrimary) {
                    statusBadge.setBackgroundResource(R.drawable.bg_badge_primary)
                    statusBadge.setTextColor(context.getColorByAttr(com.google.android.material.R.attr.colorOnPrimaryContainer))
                } else {
                    statusBadge.setBackgroundResource(R.drawable.bg_badge_secondary)
                    statusBadge.setTextColor(context.getColorByAttr(com.google.android.material.R.attr.colorOnSurfaceVariant))
                }
            }
        }

        fun setBadge(res: Int) {
            setBadge(res.getDrawable(context))
        }

        fun setBadge(drawable: Drawable?) {
            if (drawable != null) {
                if (badge == null) {
                    badge = AppCompatImageView(context).apply {
                        layoutParams = LayoutParams(24.dp, 24.dp)
                        addView(this)
                    }
                }
                badge!!.setImageDrawable(drawable)
            } else {
                if (badge != null) {
                    removeView(badge)
                    badge = null
                }
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            children.forEach {
                it.autoMeasure()
            }
            val badgeWidth = if (statusBadge.isGone) 0 else statusBadge.measuredWidth + 8.dp
            val textWidth =
                measuredWidth - paddingStart - paddingEnd - icon.measuredWidth - appName.marginStart - badgeWidth

            if (appName.measuredWidth > textWidth) {
                appName.measure(
                    textWidth.toExactlyMeasureSpec(),
                    appName.defaultHeightMeasureSpec(this)
                )
            }
            if (packageName.measuredWidth > textWidth) {
                packageName.measure(
                    textWidth.toExactlyMeasureSpec(),
                    packageName.defaultHeightMeasureSpec(this)
                )
            }
            if (versionInfo.measuredWidth > textWidth) {
                versionInfo.measure(
                    textWidth.toExactlyMeasureSpec(),
                    versionInfo.defaultHeightMeasureSpec(this)
                )
            }
            if (abiInfo.measuredWidth > textWidth) {
                abiInfo.measure(
                    textWidth.toExactlyMeasureSpec(),
                    abiInfo.defaultHeightMeasureSpec(this)
                )
            }
            val textHeight = appName.measuredHeightWithVisibility + packageName.measuredHeightWithVisibility + versionInfo.measuredHeightWithVisibility + abiInfo.measuredHeightWithVisibility + 4.dp
            val contentHeight = maxOf(icon.measuredHeightWithVisibility, textHeight)
            setMeasuredDimension(
                measuredWidth,
                paddingTop + contentHeight + paddingBottom
            )
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            icon.layout(paddingStart, icon.toVerticalCenter(this))
            val offsetStart = paddingStart + icon.measuredWidth + appName.marginStart
            appName.layout(offsetStart, paddingTop)
            packageName.layout(offsetStart, appName.bottom + 2.dp)
            versionInfo.layout(offsetStart, packageName.bottom + 2.dp)
            abiInfo.layout(offsetStart, versionInfo.bottom + 2.dp)
            if (!statusBadge.isGone) {
                statusBadge.layout(paddingEnd, statusBadge.toVerticalCenter(this), fromRight = true)
            }
            badge?.layout(paddingTop, paddingEnd, fromRight = true)
        }
    }
}

private fun TextView.setItemBackground() {
    if (text.trim().isEmpty()) {
        setBackgroundResource(R.drawable.bg_app_item_text_inset)
        alpha = 0.65f
    } else {
        background = null
        alpha = 1f
    }
}