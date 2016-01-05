parser grammar SchemaParser;

options { tokenVocab=SchemaLexer; }

// Parser components

document
 : documentationBlock? directiveList
 ;

directiveList
 : directive*
 ;

directive
 : include
 | group
 | use
 | definition
 ;

definition
 : documentationBlock? definitionName parameterList? flagList? ( LBRACE directiveList RBRACE | SEMI )
 ;

documentationBlock
 : documentationLine+
 ;

documentationLine
 : DOC_START documentationContent DOC_END
 ;

documentationContent
 : DOC_CONTENT?
 ;

include
 : INCLUDE_LITERAL stringArgument SEMI
 ;

group
 : GROUP_LITERAL stringArgument LBRACE directiveList RBRACE
 ;

use
 : USE_LITERAL stringArgument SEMI
 ;

stringArgument
 : quotedStringValue
 | unquotedStringValue
 ;

definitionName
 : UNQUOTED_STRING_LITERAL
 ;

flagList
 : LBRACKET flag ( COMMA flag )* RBRACKET
 ;

flag
 : flagName ( EQ flagValue )?
 ;

flagName
 : UNQUOTED_STRING_LITERAL
 ;

flagValue
 : STRING_LITERAL
 | UNQUOTED_STRING_LITERAL
 ;

parameterList
 : parameter+
 ;

parameter
 : parameterName ( COLON parameterType )?
 ;

parameterName
 : UNQUOTED_STRING_LITERAL
 ;

parameterType
 : STRING_TYPE_LITERAL
 | BOOLEAN_TYPE_LITERAL
 | INT_TYPE_LITERAL
 | DECIMAL_TYPE_LITERAL
 | PERCENTAGE_TYPE_LITERAL
 | DURATION_TYPE_LITERAL
 ;

quotedStringValue
 : STRING_LITERAL
 ;

unquotedStringValue
: UNQUOTED_STRING_LITERAL
;
