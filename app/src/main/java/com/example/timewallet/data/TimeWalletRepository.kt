
// --- Heartbeat-Override (Notfallmodus) ---
@Volatile
var lastHeartbeat: Long = 0L

fun updateHeartbeat() {
    lastHeartbeat = System.currentTimeMillis()
}

fun isAppAlive(timeoutMs: Long = 5000L): Boolean {
    val now = System.currentTimeMillis()
    return now - lastHeartbeat <= timeoutMs
}

suspend fun addCoins(amount: Int, reason: String) {
    val entry = com.example.timewallet.data.coins.CoinEntry(
        amount = amount,
        reason = reason,
        timestamp = System.currentTimeMillis()
    )
    database.coinDao().insert(entry)
}

fun getCoinHistory() = database.coinDao().getHistory()

@Volatile var socialMediaMinutes: Int = 0
@Volatile var sessionRunning: Boolean = false
@Volatile var sessionValid: Boolean = false

fun allowSocialMedia(): Boolean {
    return socialMediaMinutes > 0 && sessionValid && !sessionRunning
}

fun consumeMinute() {
    if (socialMediaMinutes > 0) {
        socialMediaMinutes--
    }
}

fun isSocialAllowed(): Boolean {
    // Social Media nur erlaubt, wenn:
    // - Minuten > 0
    // - Session gültig
    // - keine Session läuft
    return socialMediaMinutes > 0 && sessionValid && !sessionRunning
}

private val _socialMinutesFlow = MutableStateFlow(socialMediaMinutes)
val socialMinutesFlow = _socialMinutesFlow

fun consumeMinute() {
    if (socialMediaMinutes > 0) {
        socialMediaMinutes--
        _socialMinutesFlow.value = socialMediaMinutes
    }
}

suspend fun addSession(minutes: Int, score: Int, valid: Boolean) {
    database.sessionDao().insert(
        SessionEntry(
            minutes = minutes,
            score = score,
            valid = valid,
            timestamp = System.currentTimeMillis()
        )
    )
}

fun getSessions() = database.sessionDao().getSessions()

suspend fun addSession(minutes: Int, score: Int, valid: Boolean) {
    database.sessionDao().insert(
        SessionEntry(
            minutes = minutes,
            score = score,
            valid = valid,
            timestamp = System.currentTimeMillis()
        )
    )
}

fun getSessions() = database.sessionDao().getSessions()
