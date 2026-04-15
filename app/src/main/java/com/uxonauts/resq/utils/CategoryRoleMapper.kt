package com.uxonauts.resq.utils

object CategoryRoleMapper {
    // Role: 1=Admin, 2=Polisi, 3=Medis, 4=Damkar

    fun getTargetRoles(category: String): List<Int> {
        return when (category.lowercase()) {
            "kecelakaan" -> listOf(2, 3)
            "kebakaran" -> listOf(2, 3, 4)
            "darurat medis" -> listOf(3)
            "tindak kriminal" -> listOf(2)
            "bencana alam" -> listOf(2, 3, 4)
            "orang hilang" -> listOf(2)
            else -> listOf(2)
        }
    }

    // Mapping untuk laporan non-darurat
    fun getReportTargetRoles(subJenis: String): List<Int> {
        return when (subJenis.lowercase()) {
            // Kejahatan & Keamanan → Polisi
            "pencurian kendaraan", "pencurian rumah", "gang",
            "perusakan barang", "gangguan keamanan", "kdrt" -> listOf(2)

            // Lalu Lintas → Polisi
            "kecelakaan ringan", "pelanggaran lalu lintas" -> listOf(2)
            // Infrastruktur jalan → Admin + Polisi
            "jalan rusak", "lampu lalu lintas mati" -> listOf(1, 2)

            // Publik & Lingkungan → Admin
            "sampah menumpuk", "pohon tumbang", "saluran tersumbat" -> listOf(1, 4)
            "banjir" -> listOf(1, 2, 3, 4)

            // Siber → Polisi
            "penipuan online", "pencurian data", "cyberbullying", "konten negatif" -> listOf(2)

            // Bencana → semua
            "kerusakan rumah", "kebutuhan logistik", "lokasi terisolasi" -> listOf(1, 2, 3, 4)

            // Keluhan layanan → Admin
            "layanan kesehatan" -> listOf(1, 3)
            "layanan administrasi", "fasilitas umum rusak" -> listOf(1)

            else -> listOf(1) // default ke admin
        }
    }

    fun categoryDescription(category: String): String {
        val roles = getTargetRoles(category)
        val names = roles.map { roleId ->
            when (roleId) {
                2 -> "Polisi"
                3 -> "Medis/Ambulans"
                4 -> "Damkar"
                else -> ""
            }
        }.filter { it.isNotEmpty() }
        return "Alert dikirim ke: ${names.joinToString(", ")}"
    }
}