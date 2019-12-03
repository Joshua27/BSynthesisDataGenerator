package datagenerator

import com.google.inject.Guice
import com.google.inject.Stage
import de.prob.MainModule
import de.prob.scripting.Api
import org.slf4j.LoggerFactory

class OperationDataGenerator {

    private val logger = LoggerFactory.getLogger(javaClass)
    //private val injector = Guice.createInjector(Stage.PRODUCTION, injector.DataGeneratorModule, MainModule())

    fun generateData() {
        //val api = injector.getInstance(Api::class.java)

    }
}