package io.theidkteam.verita.utils

import org.matrix.android.sdk.api.session.content.ContentUrlResolver

fun String?.resolveMatrixUrl(
    resolver: ContentUrlResolver?,
    width: Int = 256,
    height: Int = 256,
    method: ContentUrlResolver.ThumbnailMethod = ContentUrlResolver.ThumbnailMethod.SCALE
): String? {
    if (this == null || !this.startsWith("mxc://")) return this
    return resolver?.resolveThumbnail(this, width, height, method) ?: this
}

fun String?.resolveFullMatrixUrl(resolver: ContentUrlResolver?): String? {
    if (this == null || !this.startsWith("mxc://")) return this
    return resolver?.resolveFullSize(this) ?: this
}
