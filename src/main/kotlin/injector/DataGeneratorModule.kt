package injector

import com.google.inject.AbstractModule
import de.prob.MainModule

class DataGeneratorModule : AbstractModule() {
    override fun configure() {
        install(MainModule())
    }
}