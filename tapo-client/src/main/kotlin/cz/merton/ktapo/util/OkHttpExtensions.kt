/*
 * Copyright (c) 2021 Merton Labs s.r.o
 * Author: rvbiljouw
 * License: MIT
 */
package cz.merton.webflow.util

import okhttp3.*
import java.util.logging.Logger

fun String.toJSONPayload(): RequestBody {
    return RequestBody.create(MediaType.get("application/json"), this)
}

fun OkHttpClient.get(url: String): Request.Builder {
    return Request.Builder()
        .url(url)
        .get()
}

fun OkHttpClient.delete(url: String): Request.Builder {
    return Request.Builder()
        .url(url)
        .delete()
}


fun OkHttpClient.post(url: String, body: RequestBody): Request.Builder {
    return Request.Builder()
        .url(url)
        .post(body)
}

fun OkHttpClient.patch(url: String, body: RequestBody): Request.Builder {
    return Request.Builder()
        .url(url)
        .patch(body)
}


fun OkHttpClient.execute(request: Request): Response {
    return try {
        newCall(request).execute()
    } catch (t: Throwable) {
        Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .message("Request failed. ${t.message}")
            .code(500)
            .build()
    }
}
