package procmodel.editor2d

import procmodel.editor.PmColorTheme
import procmodel.editor.PmSyntaxHighlighter
import procmodel2.Pm2dBuiltinFunctions
import procmodel2.Pm2dTypes

class Pm2dSyntaxHighlighter(sourceCode: List<String>, colorTheme: PmColorTheme): PmSyntaxHighlighter(sourceCode, colorTheme) {

    override fun isBuiltinFunctionName(candidate: String) = super.isBuiltinFunctionName(candidate) ||
            Pm2dBuiltinFunctions.all.containsKey(candidate)

    override fun isBuiltinTypeName(candidate: String) = super.isBuiltinTypeName(candidate) ||
            Pm2dTypes.all.find { it.name == candidate } != null
}
