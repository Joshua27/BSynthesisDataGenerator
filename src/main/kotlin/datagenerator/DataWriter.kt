package datagenerator

import prob.PredicateData
import java.nio.file.Path

fun writePredicateDataSetToFile(predicateDataSet: Set<PredicateData>, target: Path) {
    // TODO: XML
    // TODO: escape special chars for XML
    predicateDataSet.forEach { writePredicateDataToFile(it) }
}

private fun writePredicateDataToFile(predicateData: PredicateData) {
    // TODO
}
