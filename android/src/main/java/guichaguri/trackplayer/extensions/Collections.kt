package guichaguri.trackplayer.extensions

/**
 * Returns index of the first element matching the given [predicate], or null if the list does not contain such element.
 */
inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    for (index in indices) {
        if (predicate(this[index])) {
            return index
        }
    }

    return null
}
