package procmodel.exceptions

class PmRuntimeError(message: String) : RuntimeException(message) {
    constructor(description: String, lineNumber: Int) : this("line $lineNumber: $description")
}