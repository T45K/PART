package io.github.t45k.part.util

import io.reactivex.Observable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun listAsObservable(path: Path) = Observable.fromIterable(Files.list(path).toList())
fun walkAsObservable(path: Path) = Observable.fromIterable(Files.walk(path).toList())
