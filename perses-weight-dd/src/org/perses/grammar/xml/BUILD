package(default_visibility = [
    "//visibility:public",
])

load("//antlropt/src/org/perses/antlr/pnf:pnf.bzl", "pnf_convert")
load(
    "//src/org/perses/grammar:antlr_util.bzl",
    "antlr_codegen_lexer",
    "antlr_codegen_parser",
)
load(
    "//src/org/perses/grammar/adhoc:adhoc_grammar.bzl",
    "pnf_parser_facade_library",
)

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

antlr_codegen_lexer(
    name = "xml_lexer",
    java_pkg_name = "org.perses.grammar.xml",
    lexer_grammar_file = "XMLLexer.g4",
    lexer_java_file_name = "XMLLexer.java",
)

pnf_convert(
    name = "normalize_xml_to_pnf",
    src = "XMLParser.g4",
    out = "PnfXMLParser.kt",
    lexer_grammar = "XMLLexer.g4",
    start_rule_name = "document",
)

antlr_codegen_parser(
    name = "xml_parser",
    java_pkg_name = "org.perses.grammar.xml",
    lexer_grammar_file = "XMLLexer.g4",
    parser_grammar_file = "XMLParser.g4",
    parser_java_file_name = "PnfXMLParser.java",
    deps = [
        ":xml_lexer",
    ],
)

pnf_parser_facade_library(
    name = "parser_facade",
    java_package_name = "org.perses.grammar.xml",
    language_kind_full_name = "org.perses.grammar.xml.LanguageXML",
    lexer_file_name = "XMLLexer.g4",
    output_jar_file_name = "parser_facade.jar",
    parser_facade_class_simple_name = "PnfXMLParserFacade",
    parser_file_name = "XMLParser.g4",
    start_rule_name = "document",
    token_names_of_identifiers = ["Name"],
    deps = [
        ":language_kind",
    ],
)

kt_jvm_library(
    name = "language_kind",
    srcs = ["LanguageXML.kt"],
    deps = [
        "//src/org/perses/program:language_kind",
    ],
)