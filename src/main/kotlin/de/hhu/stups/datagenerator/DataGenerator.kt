package de.hhu.stups.datagenerator

import de.prob.scripting.Api
import de.prob.scripting.ModelTranslationError
import de.prob.statespace.StateSpace
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

open class DataGenerator {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(DataGeneratorException::class)
    fun loadStateSpace(api: Api, file: Path): StateSpace {
        logger.info("\tLoading state space for {}", file)
        try {
            val fileName = file.toString()
            return when {
                fileName.endsWith(".bcm") -> api.eventb_load(file.toString())
                fileName.endsWith(".mch") -> api.b_load(file.toString())
                else -> throw DataGeneratorException("Unknown machine type.")
            }
        } catch (e: ModelTranslationError) {
            throw DataGeneratorException("Unable to load state space due to model translation error.")
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    fun getSHA(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}