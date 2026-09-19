
binding.btnLegal.setOnClickListener {
    startActivity(Intent(this, com.example.timewallet.ui.legal.LegalActivity::class.java))
}

private fun setupNavigation() {
    binding.navHome.setOnClickListener {
        highlight(binding.navHome)
    }

    binding.navStats.setOnClickListener {
        startActivity(Intent(this, com.example.timewallet.ui.history.SessionHistoryActivity::class.java))
        highlight(binding.navStats)
    }

    binding.navSettings.setOnClickListener {
        startActivity(Intent(this, com.example.timewallet.ui.settings.SettingsActivity::class.java))
        highlight(binding.navSettings)
    }

    binding.navLegal.setOnClickListener {
        startActivity(Intent(this, com.example.timewallet.ui.legal.LegalActivity::class.java))
        highlight(binding.navLegal)
    }
}

private fun highlight(active: ImageView) {
    val cyan = Color.parseColor("#00E5FF")
    val gray = Color.parseColor("#B3B3B3")

    binding.navHome.setColorFilter(gray)
    binding.navStats.setColorFilter(gray)
    binding.navSettings.setColorFilter(gray)
    binding.navLegal.setColorFilter(gray)

    active.setColorFilter(cyan)
}
