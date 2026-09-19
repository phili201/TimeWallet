
val repo = (application as TimeWalletApp).repository

lifecycleScope.launch {
    repo.socialMinutesFlow.collect { m ->
        binding.blockCountdown.text = "Noch $m Minuten"
    }
}

val repo = (application as TimeWalletApp).repository

lifecycleScope.launch {
    repo.socialMinutesFlow.collect { m ->
        binding.blockCountdown.text = "Noch $m Minuten"
    }
}

val repo = (application as TimeWalletApp).repository

lifecycleScope.launch {
    repo.socialMinutesFlow.collect { m ->
        binding.blockCountdown.text = "Noch $m Minuten"
    }
}
