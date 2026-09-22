package org.schabi.newpipe.about

import androidx.fragment.app.Fragment

open class PvcAboutFragment : Fragment() {
    protected fun pvcAddSoftwareComponents(softwareComponents: ArrayList<SoftwareComponent>) {
        if (areSoftwareComponentsAlreadyAdded) {
            return // run only once
        }
        areSoftwareComponentsAlreadyAdded = true
        softwareComponents.add(
            SoftwareComponent(
                "EventBus",
                "2012 - 2016",
                "Markus Junginger",
                "https://greenrobot.org/eventbus",
                StandardLicenses.APACHE2
            )
        )
        softwareComponents.add(
            SoftwareComponent(
                "HlsDownloader",
                "2025",
                "evermind-zz",
                "https://github.com/evermind-zz/HlsDownloader",
                StandardLicenses.GPL3
            )
        )
        softwareComponents.add(
            SoftwareComponent(
                "slimhls-converter",
                "2025",
                "evermind-zz",
                "https://github.com/evermind-zz/slimhls-converter",
                StandardLicenses.GPL3
            )
        )
        softwareComponents.add(
            SoftwareComponent(
                "LogcatToolkit",
                "2017 - 2026",
                "evermind-zz: LogcatToolkit, (kyze8439690: logcatviewer)",
                "https://github.com/evermind-zz/logcat-toolkit",
                StandardLicenses.APACHE2
            )
        )
        softwareComponents.add(
            SoftwareComponent(
                "challengeFloatsAway",
                "2026",
                "evermind-zz",
                "https://github.com/evermind-zz/challengeFloatsAway",
                StandardLicenses.GPL3
            )
        )
    }

    companion object {
        private var areSoftwareComponentsAlreadyAdded: Boolean = false
    }
}
