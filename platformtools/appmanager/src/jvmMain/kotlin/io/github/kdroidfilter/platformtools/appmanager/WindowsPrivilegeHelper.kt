package io.github.kdroidfilter.platformtools.appmanager

import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import java.io.File
import java.io.InputStreamReader

object WindowsPrivilegeHelper {

    /**
     * Vérifie si le processus est déjà exécuté avec des privilèges élevés (admin).
     */
    fun isProcessElevated(): Boolean {
        val hToken = WinNT.HANDLEByReference()
        try {
            // Ouvrir le token du processus courant
            val success = Advapi32.INSTANCE.OpenProcessToken(
                Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_QUERY,
                hToken
            )
            if (!success) return false

            val elevation = WinNT.TOKEN_ELEVATION()
            val size = IntByReference()
            val result = Advapi32.INSTANCE.GetTokenInformation(
                hToken.value,
                WinNT.TOKEN_INFORMATION_CLASS.TokenElevation,
                elevation,
                elevation.size(),
                size
            )
            if (!result) return false

            // Si TokenIsElevated != 0, alors le processus est en mode admin.
            return elevation.TokenIsElevated != 0
        } finally {
            Kernel32.INSTANCE.CloseHandle(hToken.value)
        }
    }

    /**
     * Demande l'élévation (UAC) pour installer un MSI via msiexec.
     */
    fun requestAdminPrivilegesForMsi(msiPath: String) {
        val shellExecuteInfo = ShellAPI.SHELLEXECUTEINFO().apply {
            cbSize = size()
            lpVerb = "runas"                         // Demande l'élévation (UAC)
            lpFile = "msiexec"                       // Chemin de l’exécutable (dans le PATH)
            lpParameters = "/i \"$msiPath\" /quiet"   // Paramètres pour installer le MSI silencieusement
            nShow = WinUser.SW_SHOWNORMAL
            fMask = Shell32.SEE_MASK_NOCLOSEPROCESS
        }

        val success = Shell32.INSTANCE.ShellExecuteEx(shellExecuteInfo)
        if (!success) {
            val errorCode = Kernel32.INSTANCE.GetLastError()
            println("Échec de ShellExecuteEx pour l’MSI. Code erreur : $errorCode")
        } else {
            println("Demande d'élévation UAC pour l’installation du MSI réussie.")
        }
    }

    /**
     * Installe un fichier MSI sous Windows.
     * @param installerFile Le fichier MSI à installer.
     * @param onResult Callback (succès: Boolean, messageErreur: String?).
     * @param requireAdmin Indique si l'installation doit absolument se faire en mode admin.
     */
    fun installOnWindows(
        installerFile: File,
        onResult: (Boolean, String?) -> Unit,
        requireAdmin: Boolean = false
    ) {
        // 1. Vérifier si on demande explicitement des privilèges admin
        if (requireAdmin && !isProcessElevated()) {
            // Si on a besoin des droits admin et qu'on n'est pas en mode admin,
            // on lance la demande d'élévation et on arrête ici.
            requestAdminPrivilegesForMsi(installerFile.absolutePath)
            onResult(
                false,
                "Installation interrompue pour demander l’élévation des privilèges. " +
                        "Veuillez accepter la boîte de dialogue UAC."
            )
            return
        }

        // 2. Vérifier la validité du fichier
        if (!installerFile.exists() || !installerFile.extension.equals("msi", ignoreCase = true)) {
            onResult(false, "Fichier introuvable ou extension incorrecte (MSI attendu).")
            return
        }

        // 3. Préparer la commande msiexec
        val command = listOf(
            "msiexec",
            "/i", installerFile.absolutePath,
            "/quiet",
            "/l*v", "${installerFile.parentFile?.absolutePath}\\installation_log.txt"
        )

        try {
            // 4. Construire le ProcessBuilder
            val processBuilder = ProcessBuilder(command).apply {
                redirectErrorStream(true)
            }

            // 5. Lancer le processus msiexec
            val process = processBuilder.start()
            val exitCode = process.waitFor()

            // 6. Lire le flux de sortie
            val output = InputStreamReader(process.inputStream).readText().trim()

            // 7. Vérifier le code de sortie
            if (exitCode == 0) {
                onResult(true, null)
            } else {
                val messageErreur = """
                    Échec de l'installation (code de retour : $exitCode).
                    Sortie :
                    $output
                """.trimIndent()
                onResult(false, messageErreur)
            }
        } catch (e: Exception) {
            onResult(false, "Exception lors de l'installation : ${e.message}")
        }
    }
}
