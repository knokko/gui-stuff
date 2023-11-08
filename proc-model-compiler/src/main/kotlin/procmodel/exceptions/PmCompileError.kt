package procmodel.exceptions

class PmCompileError(message: String) : Exception(message) {
    constructor(description: String, lineNumber: Int, linePosition: Int) : this("$lineNumber:$linePosition $description")
}
