package de.hhu.stups.datagenerator

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

fun getStateForVarFromExample(varName: String, state: HashSet<VariableState>): String {
    state.forEach {
        if (it.variable.name == varName) {
            return it.value
        }
    }
    throw Exception("Variable name not found for operation data.")
}

fun writeOperationDataSetToFile(operationDataSet: Set<OperationData>, target: Path) {
    val xmlWriter = IndentingXMLStreamWriter(
        XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(File(target.toUri())), "UTF-8")
    )
    // xmlWriter.document {
    //element("synthesis-data") {
    //    attribute("type", "predicates")
    //    attribute("created", LocalDateTime.now().toString())
    operationDataSet.forEach { operationData ->
        xmlWriter.element("record") {
            attribute("path", operationData.metaData.machinePath)
            attribute("machine", operationData.metaData.machineName)
            attribute("operation", operationData.metaData.operationName)
            //attribute("hash", operationData.metaData.astHash)
            attribute("examples", operationData.amountOfExamples().toString())
            attribute("vars", operationData.amountOfVariables().toString())
            element("vars") {
                operationData.getVariables().forEach { variable ->
                    val varName = variable.name
                    element("var") {
                        element("name", varName)
                        element("type", variable.type)
                        element("examples") {
                            operationData.examples.forEach { example ->
                                element("example") {
                                    element(
                                        "input",
                                        getStateForVarFromExample(varName, example.input)
                                    )
                                    element(
                                        "output",
                                        getStateForVarFromExample(varName, example.output)
                                    )
                                }
                            }
                        }
                        element("ground-truth") {
                            operationData.varGroundTruths[varName]?.forEach {
                                element("component") {
                                    element("name", it.componentName)
                                    element("amount", it.componentAmount.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
        //  }
    }
    println()
    println("Output written to: $target")
    xmlWriter.flush()
    xmlWriter.close()
}

fun writePredicateDataSetToFile(predicateDataSet: Set<PredicateData>, target: Path) {
    val xmlWriter = IndentingXMLStreamWriter(
        XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(File(target.toUri())), "UTF-8")
    )
    // xmlWriter.document {
    //element("synthesis-data") {
    //    attribute("type", "predicates")
    //    attribute("created", LocalDateTime.now().toString())
    predicateDataSet.forEach { predicateData ->
        xmlWriter.element("record") {
            attribute("path", predicateData.metaData.machinePath)
            attribute("machine", predicateData.metaData.machineName)
            attribute("hash", predicateData.metaData.astHash)
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
    println()
    println("Output written to: $target")
    xmlWriter.flush()
    xmlWriter.close()
}
/*
fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.init()
    this.writeEndDocument()
    return this
}*/

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
