package datagenerator

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.LocalDateTime
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

fun writePredicateDataSetToFile(predicateDataSet: Set<PredicateData>, target: Path) {
    val xmlWriter = IndentingXMLStreamWriter(
        XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(File(target.toUri())), "UTF-8")
    )
    xmlWriter.document {
        //element("synthesis-data") {
        //    attribute("type", "predicates")
        //    attribute("created", LocalDateTime.now().toString())
        predicateDataSet.forEach { predicateData ->
            element("record") {
                attribute("path", predicateData.metaData.machinePath)
                attribute("machine", predicateData.metaData.machineName)
                attribute("examples", predicateData.amountOfExamples().toString())
                attribute("vars", predicateData.amountOfVariables().toString())
                element("vars") {
                    predicateData.getVariables().forEach { variable ->
                        element("var") {
                            element("name", variable.name)
                            element("type", variable.type)
                            element("positive") {
                                predicateData.positiveInputs.forEachIndexed { i, positiveInput ->
                                    element(
                                        "input",
                                        positiveInput.first { it.variable.name == variable.name }.value
                                    ) {
                                        attribute("index", i.toString())
                                    }
                                }
                            }
                            element("negative") {
                                predicateData.negativeInputs.forEachIndexed { i, positiveInput ->
                                    element(
                                        "input",
                                        positiveInput.first { it.variable.name == variable.name }.value
                                    ) {
                                        attribute("index", i.toString())
                                    }
                                }
                            }
                        }
                    }
                }
                element("ground-truth") {
                    predicateData.groundTruth.forEach {
                        element("component") {
                            element("name", it.componentName)
                            element("amount", it.componentAmount.toString())
                        }
                    }
                }
            }
            //  }
        }
    }
    println("Data written to $target.")
    xmlWriter.flush()
    xmlWriter.close()
}

fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.init()
    this.writeEndDocument()
    return this
}

fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    writeCharacters(content)
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String) {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)
