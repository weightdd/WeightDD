/*
 * Copyright (C) 2018-2024 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.pluverse.jvm.fuzz.basic

import org.pluverse.jvm.fuzz.util.Configuration
import org.pluverse.jvm.fuzz.util.JavaType
import org.pluverse.jvm.fuzz.util.VariableType

class Context(
  val parent: Context?,
  val kind: ContextAttribute,
  val globalContext: GlobalContext,
  var contextMethod: JavaMethod? = null,
  var contextClass: JavaClass,
) {
  val contextID = globalContext.contextTracker++
  val variableList: MutableList<Variable> = ArrayList<Variable>()
  val arrayList: MutableList<Array> = ArrayList<Array>()
  val objectList: MutableList<JavaObject> = ArrayList<JavaObject>()
  var withinConstructorCall = false

  fun fullVariableList(): List<Variable> {
    if (parent != null) {
      return variableList + parent.fullVariableList()
    }
    return variableList
  }

  fun fullObjectList(): List<JavaObject> {
    if (parent != null) {
      return objectList + parent.fullObjectList()
    }
    return objectList
  }

  fun fullArrayList(): List<Array> {
    if (parent != null) {
      return arrayList + parent.fullArrayList()
    }
    return arrayList
  }

  fun genUniqueName(sort: String, type: JavaType? = null, isArray: Boolean = false): String {
    if (kind.value < ContextAttribute.METHOD.value) {
      return parent!!.genUniqueName(sort, type, isArray)
    }

    var prefix = ""
    if (type == JavaType.OBJECT) {
      prefix = "obj"
    } else if (type == JavaType.STRING) {
      prefix = "str"
    } else if (type == JavaType.BYTE) {
      prefix = "by"
    } else if (
      (
        type == JavaType.VOID
        ) or (
        type == JavaType.EXCEPTION
        ) or Configuration.TYPES.getKeys().contains(
        type,
      )
    ) {
      prefix = type!!.stringValue.get(0).toString()
    }

    if (isArray) {
      prefix += "Arr"
    }

    if (!sort.equals("var")) {
      prefix += sort
    } else if (kind.value == ContextAttribute.CLASS.value) {
      prefix += "Fld"
    }

    var name = prefix
    if (globalContext.variableMapping.containsKey(prefix)) {
      name += globalContext.variableMapping[prefix].toString()
      globalContext.variableMapping[prefix] = globalContext.variableMapping[prefix]!! + 1
      return name
    }
    globalContext.variableMapping[prefix] = 0
    return name
  }

  fun registerVariable(
    variable: Variable,
    localFlag:
    Boolean = false,
    name: String?,
    isArray: Boolean,
  ): String {
    if ((kind.value == ContextAttribute.CLASS.value) ||
      (kind.value == ContextAttribute.METHOD.value) ||
      localFlag || variable.flagCheck(NameAttribute.BLOCK)
    ) {
      if (variable is Array) {
        arrayList.add(variable)
      } else if (!(variable is JavaObject)) {
        if (!variable.flagCheck(NameAttribute.AUXILIARY)) {
          variableList.add(variable)
        }
      }

      if (name == null) {
        return genUniqueName("var", variable.type, isArray)
      }
      return name
    }
    return parent!!.registerVariable(variable, false, name, isArray)
  }

  /** Recursively find the context method of this context, if it exists */
  fun recurseContextMethod(): JavaMethod {
    var currentContext = this
    while (currentContext.contextMethod == null) {
      if (currentContext.parent == null) {
        return contextClass.constructorMethod
      }
      currentContext = currentContext.parent!!
    }
    return currentContext.contextMethod as JavaMethod
  }

  // Generate global check sum
  // Used by context of mainTest and mainClass
  fun generateResultPrint(): String {
    var returnResult = "\n"
    (arrayList + variableList + objectList).forEach {
        variable ->
      if (!variable.flagCheck(NameAttribute.CHECKSUM) &&
        (!(variable is JavaObject) || variable.flagCheck(NameAttribute.NOTNULL))
      ) {
        returnResult += globalContext.printLine(
          "returnResult += \"" +
            variable.name + " = \" + " + variable.generateCheckSum() + "+ \"\\n\";",
        )
      }
    }

    returnResult += "\n"

    (arrayList + variableList).forEach {
        variable ->
      if (variable.flagCheck(NameAttribute.CHECKSUM)) {
        returnResult += globalContext.printLine(
          "returnResult += \"" +
            variable.name + " = \" + " + variable.generateCheckSum() + " + \"\\n\";",
        )
      }
    }
    return returnResult
  }

  // Need to add object declaration here after class implementations
  fun generateDeclaration(): String {
    var returnResult = ""
    val declaration = HashMap<JavaType, String>()
    val classDeclaration = ArrayList<String>()
    variableList.map({ variable ->
      if (!variable.flagCheck(NameAttribute.ARGUMENT) &&
        !variable.flagCheck(NameAttribute.CLASS) &&
        !variable.flagCheck(NameAttribute.SUB) &&
        !variable.flagCheck(NameAttribute.LOCAL)
      ) {
        if (declaration.containsKey(variable.type)) {
          declaration[variable.type] += ", " + variable.generateDeclaration()
        } else {
          declaration[variable.type] = ", " + variable.generateDeclaration()
        }
      }

      if (variable.flagCheck(NameAttribute.CLASS) &&
        !variable.flagCheck(NameAttribute.SUB)
      ) {
        classDeclaration.add(
          variable.generateModifier() + variable.type.stringValue +
            " " + variable.generateDeclaration(),
        )
      }
    })

    arrayList.map({ array ->
      if (!array.flagCheck(NameAttribute.ARGUMENT) &&
        !array.flagCheck(NameAttribute.CLASS) &&
        !array.flagCheck(NameAttribute.SUB) &&
        !array.flagCheck(NameAttribute.LOCAL)
      ) {
        if (declaration.containsKey(array.type)) {
          declaration[array.type] += ", " + array.generateDeclaration()
        } else {
          declaration[array.type] = ", " + array.generateDeclaration()
        }
      }

      if (array.flagCheck(NameAttribute.CLASS) &&
        !array.flagCheck(NameAttribute.SUB)
      ) {
        classDeclaration.add(
          array.generateModifier() + array.type.stringValue +
            " " + array.generateDeclaration(),
        )
      }
    })

    declaration.forEach { (key, value) ->
      returnResult += globalContext.printLine(key.stringValue + value.substring(1) + ";")
    }

    classDeclaration.map { value ->
      returnResult += globalContext.printLine(value + ";")
    }

    if (kind == ContextAttribute.CLASS) {
      returnResult += globalContext.printLine("static {")
      globalContext.updateIndentation(1)
    }

    arrayList.forEach({ array ->
      if (!array.flagCheck(NameAttribute.ARGUMENT) &&
        (
          array.flagCheck(NameAttribute.STATIC) ||
            kind != ContextAttribute.CLASS
          )
      ) {
        returnResult += globalContext.printLine(array.initialValue + ";")
      }
    })

    if (kind == ContextAttribute.CLASS) {
      globalContext.updateIndentation(-1)
      returnResult += globalContext.printLine("}\n")
    }

    objectList.forEach { objVal ->
      if (objVal.flagCheck(NameAttribute.CLASS) &&
        !objVal.flagCheck(NameAttribute.SUB)
      ) {
        returnResult += globalContext.printLine(
          objVal.generateModifier() + " " + objVal.generateDeclaration(),
        )
      } else {
        returnResult += globalContext.printLine(objVal.generateDeclaration())
      }
    }

    if (kind.value > ContextAttribute.METHOD.value) {
      returnResult += "\n"
    }

    return returnResult
  }

  fun getAllClassVariable(): ArrayList<Variable> {
    val currentMethod = recurseContextMethod()
    val returnArray = arrayListOf<Variable>()
    globalContext.classList.forEach { currentClass ->
      currentClass.classContext.variableList.forEach {
        if (currentClass == contextClass || it.flagCheck(NameAttribute.STATIC) ||
          currentClass.constructorMethod == currentMethod ||
          !globalContext.isCaller(currentClass.constructorMethod, currentMethod)
        ) {
          returnArray.add(it)
        }
      }
    }
    return returnArray
  }

  fun getAllClassArray(): ArrayList<Array> {
    val currentMethod = recurseContextMethod()
    val returnArray = arrayListOf<Array>()
    globalContext.classList.forEach { currentClass ->
      currentClass.classContext.arrayList.forEach {
        if (currentClass == contextClass || it.flagCheck(NameAttribute.STATIC) ||
          currentClass.constructorMethod == currentMethod ||
          !globalContext.isCaller(currentClass.constructorMethod, currentMethod)
        ) {
          returnArray.add(it)
        }
      }
    }
    return returnArray
  }

  /** get static object list from current context and parents */
  fun getStaticObject(): ArrayList<JavaObject> {
    val returnArray = arrayListOf<JavaObject>()

    globalContext.classList.forEach { currentClass ->
      currentClass.classContext.objectList.forEach {
        if (it.flagCheck(NameAttribute.STATIC) && !globalContext.isCaller(
            currentClass.constructorMethod,
            recurseContextMethod(),
          )
        ) {
          returnArray.add(it)
        }
      }
    }
    return returnArray
  }

  fun getValidVariable(
    type: JavaType,
    notNull: Boolean,
    destination: Boolean,
  ): ArrayList<Variable> {
    val currentMethod = recurseContextMethod()
    val validVariable = arrayListOf<Variable>()

    (fullVariableList() + getAllClassVariable()).toSet().forEach {
      if (it.type == type &&
        (it.flagCheck(NameAttribute.NOTNULL) || !notNull) &&
        (kind != ContextAttribute.CLASS || it.flagCheck(NameAttribute.STATIC)) &&
        (
          !currentMethod.flagCheck(MethodAttribute.STATIC) ||
            !it.flagCheck(NameAttribute.CLASS) ||
            it.flagCheck(NameAttribute.STATIC)
          ) &&
        (
          !destination || (
            !it.flagCheck(NameAttribute.INDUCTION) &&
              !it.flagCheck(NameAttribute.NULL) &&
              !it.flagCheck(NameAttribute.CHECKSUM)
            )
          ) &&
        (
          !withinConstructorCall ||
            it.context.contextClass == contextClass
          )
      ) {
        validVariable.add(it)
      }
    }

    return validVariable
  }

  fun getValidObject(
    notNull: Boolean,
    destination: Boolean,
    classAttribute: JavaClass?,
  ): ArrayList<JavaObject> {
    val currentMethod = recurseContextMethod()
    val validVariable = arrayListOf<JavaObject>()
    val possibleVariable = (getStaticObject() + fullObjectList()).toSet()
    possibleVariable.forEach {
      if (it.classAttribute == classAttribute &&
        (it.flagCheck(NameAttribute.NOTNULL) || !notNull) &&
        (
          !currentMethod.flagCheck(MethodAttribute.STATIC) ||
            !it.flagCheck(NameAttribute.CLASS) ||
            it.flagCheck(NameAttribute.STATIC)
          ) &&
        (
          currentMethod is Constructor &&
            it.classAttribute != contextClass
          ) &&
        (
          !currentMethod.flagCheck(MethodAttribute.OVERRIDE) ||
            !globalContext.isCaller(it.classAttribute.constructorMethod, currentMethod)
          ) &&
        (
          !destination || (
            !it.flagCheck(NameAttribute.INDUCTION) &&
              !it.flagCheck(NameAttribute.NULL) &&
              !it.flagCheck(NameAttribute.CHECKSUM)
            )
          )
      ) {
        validVariable.add(it)
      }
    }
    return validVariable
  }

  fun checkValidClass(currentMethod: JavaMethod): JavaClass? {
    val validClassList = arrayListOf<JavaClass>()
    globalContext.classList.forEach {
      if ((
          !globalContext.isCaller(it.constructorMethod, currentMethod) ||
            !globalContext.isCaller(
              it.constructorMethod,
              contextClass.constructorMethod,
            )
          ) &&
        it != contextClass
      ) {
        validClassList.add(it)
      }
    }
    if (validClassList.size == 0) {
      return null
    }
    return validClassList[
      globalContext.randomGenerator.nextInt(
        validClassList.size,
      ),
    ]
  }

  fun getInheritVariable(
    type: JavaType,
    notNull: Boolean,
  ): Variable? {
    require(contextClass.extendClass != null)
    val currentMethod = recurseContextMethod()
    val validVariable = arrayListOf<Variable>()
    contextClass.extendClass!!.classContext.variableList.forEach {
      if (it.type == type &&
        (it.flagCheck(NameAttribute.NOTNULL) || !notNull) &&
        (kind != ContextAttribute.CLASS || it.flagCheck(NameAttribute.STATIC)) &&
        (
          !currentMethod.flagCheck(MethodAttribute.STATIC) ||
            !it.flagCheck(NameAttribute.CLASS) ||
            it.flagCheck(NameAttribute.STATIC)
          ) &&
        (
          !it.flagCheck(NameAttribute.INDUCTION) &&
            !it.flagCheck(NameAttribute.NULL) &&
            !it.flagCheck(NameAttribute.CHECKSUM)
          ) &&
        (
          !withinConstructorCall ||
            it.context.contextClass == contextClass
          )
      ) {
        validVariable.add(it)
      }
    }
    if (validVariable.size == 0) {
      return null
    }
    val chosenVariable = validVariable[globalContext.randomGenerator.nextInt(validVariable.size)]

    // Now see if this chosenVariable is already created in current class context
    // If it is, automatically use again instead of readd
    contextClass.classContext.variableList.forEach {
      if (it.name == chosenVariable.name) {
        return it
      }
    }

    var currentFlag = if (notNull) NameAttribute.NOTNULL.value else 0
    // Else we either directly inherit the attribute or hide the parent attribute
    if (chosenVariable.flagCheck(NameAttribute.STATIC)) {
      currentFlag = (
        currentFlag or NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
          or NameAttribute.CLASS.value or NameAttribute.HIDDEN.value
        )
      return Variable(
        context = currentMethod.methodClass.classContext,
        type = type,
        flagAttribute = currentFlag,
        globalContext = globalContext,
        name = chosenVariable.name,
      )
    } else {
      currentFlag = currentFlag or NameAttribute.CLASS.value or
        NameAttribute.PUBLIC.value or NameAttribute.HIDDEN.value
      return Variable(
        context = currentMethod.methodClass.classContext,
        type = type,
        flagAttribute = currentFlag,
        globalContext = globalContext,
        name = chosenVariable.name,
      )
    }
  }

  /** Get variable in current context, generate new variable based on
   probability or if no valid variable is found.
   Notes: For static methods, we cannot access instance variables
   If we are generating a destination (assigment) variable
   We need to make sure we are not using an induction variable or variable
   that would be used to store NULL values since they would be overwritten
   */
  fun getVariable(
    reuseProb: Int,
    type: JavaType,
    destination: Boolean,
    notNull: Boolean,
  ): Variable {
    val currentMethod = recurseContextMethod()
    val validClass = checkValidClass(currentMethod)
    if (globalContext.randomGenerator.nextInt(100) < reuseProb) {
      if (contextClass.extendClass != null && globalContext.randomGenerator.nextInt(
          101,
        ) < Configuration.P_HIDE_PARENT
      ) {
        val inheritVariable = getInheritVariable(type, notNull)
        if (inheritVariable != null) {
          return inheritVariable
        }
      }
      val validVariable = getValidVariable(type, notNull, destination)
      if (validVariable.size > 0) {
        val returnVar = validVariable[globalContext.randomGenerator.nextInt(validVariable.size)]
        return returnVar
      }
    }
    val excludeTypes = mutableSetOf<VariableType>()
    if (kind.value >= ContextAttribute.METHOD.value) {
      excludeTypes.add(VariableType.BLOCK)
    }
    if (kind == ContextAttribute.CLASS || currentMethod.flagCheck(MethodAttribute.STATIC)) {
      excludeTypes.add(VariableType.NON_STATIC)
      excludeTypes.add(VariableType.NON_STATIC_OTHER)
    }
    if (kind.value == ContextAttribute.CLASS.value) {
      excludeTypes.add(VariableType.BLOCK)
      excludeTypes.add(VariableType.LOCAL)
      excludeTypes.add(VariableType.NON_STATIC_OTHER)
    }
    if (validClass == null || withinConstructorCall) {
      excludeTypes.add(VariableType.NON_STATIC_OTHER)
      excludeTypes.add(VariableType.STATIC_OTHER)
    }
    val variableType = Configuration.VAR_TYPES.getRandom(
      null,
      excludeTypes,
      globalContext.randomGenerator,
    )
    var currentFlag = if (notNull) NameAttribute.NOTNULL.value else 0
    when (variableType) {
      VariableType.NON_STATIC -> {
        currentFlag = currentFlag or NameAttribute.CLASS.value or NameAttribute.PUBLIC.value
        return Variable(
          context = currentMethod.methodClass.classContext,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      VariableType.STATIC -> {
        currentFlag = (
          currentFlag or NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
            or NameAttribute.CLASS.value
          )
        return Variable(
          context = currentMethod.methodClass.classContext,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      VariableType.LOCAL -> {
        return Variable(
          context = this,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      VariableType.BLOCK -> {
        currentFlag = currentFlag or NameAttribute.BLOCK.value
        return Variable(
          context = this,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      VariableType.NON_STATIC_OTHER -> {
        currentFlag = (
          currentFlag or NameAttribute.PUBLIC.value
            or NameAttribute.CLASS.value
          )
        return Variable(
          context = validClass!!.classContext,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      VariableType.STATIC_OTHER -> {
        currentFlag = (
          currentFlag or NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
            or NameAttribute.CLASS.value
          )
        return Variable(
          context = validClass!!.classContext,
          type = type,
          flagAttribute = currentFlag,
          globalContext = globalContext,
        )
      }
      else -> throw RuntimeException("Unhandled Scope Type")
    }
  }

  fun getObject(
    reuseProb: Int,
    destination: Boolean,
    notNull: Boolean,
    classAttribute: JavaClass,
    withinMethod: Boolean = false,
  ): JavaObject {
    val currentMethod = recurseContextMethod()
    if (globalContext.randomGenerator.nextInt(100) < reuseProb) {
      val validVariable = getValidObject(notNull, destination, classAttribute)
      if (validVariable.size > 0) {
        return validVariable[
          globalContext.randomGenerator.nextInt(validVariable.size),
        ]
      }
    }
    val excludeTypes = mutableSetOf<VariableType>()
    if (kind.value >= ContextAttribute.METHOD.value) {
      excludeTypes.add(VariableType.BLOCK)
    }
    if (currentMethod.flagCheck(MethodAttribute.STATIC)) {
      excludeTypes.add(VariableType.NON_STATIC)
    }
    if (kind.value == ContextAttribute.CLASS.value) {
      excludeTypes.add(VariableType.BLOCK)
      excludeTypes.add(VariableType.LOCAL)
    }

    if (globalContext.isCaller(
        classAttribute.constructorMethod,
        contextClass.constructorMethod,
      ) || withinMethod || currentMethod.flagCheck(MethodAttribute.OVERRIDE)
    ) {
      excludeTypes.add(VariableType.NON_STATIC)
      excludeTypes.add(VariableType.STATIC)
    }

    if ((
      globalContext.isCaller(
        classAttribute.constructorMethod,
        currentMethod,
      ) && currentMethod != contextClass.constructorMethod
      )
    ) {
      excludeTypes.add(VariableType.BLOCK)
      excludeTypes.add(VariableType.LOCAL)
    }

    excludeTypes.add(VariableType.NON_STATIC_OTHER)
    excludeTypes.add(VariableType.STATIC_OTHER)
    val variableType = Configuration.VAR_TYPES.getRandom(
      null,
      excludeTypes,
      globalContext.randomGenerator,
    )
    var currentFlag = if (notNull) NameAttribute.NOTNULL.value else 0
    when (variableType) {
      VariableType.NON_STATIC -> {
        currentFlag = currentFlag or NameAttribute.CLASS.value or NameAttribute.PUBLIC.value
        globalContext.addMethodCaller(
          this.contextClass.classContext.contextMethod!!,
          classAttribute.constructorMethod,
        )
        return JavaObject(
          context = currentMethod.methodClass.classContext,
          flagAttribute = currentFlag,
          globalContext = globalContext,
          classAttribute = classAttribute,
        )
      }
      VariableType.STATIC -> {
        currentFlag = (
          currentFlag or NameAttribute.PUBLIC.value or NameAttribute.STATIC.value
            or NameAttribute.CLASS.value
          )
        globalContext.addMethodCaller(
          this.contextClass.classContext.contextMethod!!,
          classAttribute.constructorMethod,
        )
        return JavaObject(
          context = currentMethod.methodClass.classContext,
          flagAttribute = currentFlag,
          globalContext = globalContext,
          classAttribute = classAttribute,
        )
      }
      VariableType.LOCAL -> {
        globalContext.addMethodCaller(currentMethod, classAttribute.constructorMethod)
        return JavaObject(
          context = this,
          flagAttribute = currentFlag,
          globalContext = globalContext,
          classAttribute = classAttribute,
        )
      }
      VariableType.BLOCK -> {
        globalContext.addMethodCaller(currentMethod, classAttribute.constructorMethod)
        currentFlag = currentFlag or NameAttribute.BLOCK.value
        return JavaObject(
          context = this,
          flagAttribute = currentFlag,
          globalContext = globalContext,
          classAttribute = classAttribute,
        )
      }
      else -> throw RuntimeException("Unhandled Scope Type")
    }
  }

  fun getValidArray(
    type: JavaType,
    notNull: Boolean,
    dimension: Int,
    size: Int,
  ): ArrayList<Array> {
    val currentMethod = recurseContextMethod()
    val validArray = arrayListOf<Array>()

    (fullArrayList() + getAllClassArray()).toSet().forEach {
      if (it.type == type &&
        (it.flagCheck(NameAttribute.NOTNULL) || !notNull) &&
        (it.dimension == dimension) &&
        (it.size >= size) &&
        (
          !currentMethod.flagCheck(MethodAttribute.STATIC) ||
            !it.flagCheck(NameAttribute.CLASS) ||
            it.flagCheck(NameAttribute.STATIC)
          ) && (
          !it.flagCheck(NameAttribute.NULL)
          ) &&
        (
          !withinConstructorCall ||
            it.context.contextClass == contextClass
          )
      ) {
        validArray.add(it)
      }
    }
    return validArray
  }

  fun getArray(
    reuseProb: Int,
    type: JavaType,
    dimension: Int,
    size: Int,
    notNull: Boolean = false,
  ): Array {
    val currentMethod = recurseContextMethod()
    val validClass = checkValidClass(currentMethod)
    if (globalContext.randomGenerator.nextInt(100) < reuseProb) {
      val validArray = getValidArray(type, notNull, dimension, size)
      if (validArray.size > 0) {
        return validArray[globalContext.randomGenerator.nextInt(validArray.size)]
      }
    }
    val excludeTypes = mutableSetOf<VariableType>()
    if (kind.value >= ContextAttribute.METHOD.value) {
      excludeTypes.add(VariableType.BLOCK)
    }
    if (currentMethod.flagCheck(MethodAttribute.STATIC)) {
      excludeTypes.add(VariableType.NON_STATIC)
      excludeTypes.add(VariableType.NON_STATIC_OTHER)
    }
    if (kind.value == ContextAttribute.CLASS.value) {
      excludeTypes.add(VariableType.BLOCK)
      excludeTypes.add(VariableType.LOCAL)
    }
    if (currentMethod is MainMethod) {
      excludeTypes.add(VariableType.STATIC)
    }
    if (validClass == null || withinConstructorCall) {
      excludeTypes.add(VariableType.NON_STATIC_OTHER)
      excludeTypes.add(VariableType.STATIC_OTHER)
    }
    val variableType = Configuration.VAR_TYPES.getRandom(
      null,
      excludeTypes,
      globalContext.randomGenerator,
    )
    val currentFlag = if (notNull) NameAttribute.NOTNULL.value else 0
    when (variableType) {
      VariableType.LOCAL -> {
        return Array(
          context = this,
          type = type,
          flagAttribute = currentFlag,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      VariableType.BLOCK -> {
        return Array(
          context = this,
          type = type,
          flagAttribute = currentFlag or NameAttribute.BLOCK.value,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      VariableType.NON_STATIC -> {
        return Array(
          context = currentMethod.methodClass.classContext,
          type = type,
          flagAttribute = currentFlag or NameAttribute.CLASS.value or NameAttribute.PUBLIC.value,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      VariableType.STATIC -> {
        return Array(
          context = currentMethod.methodClass.classContext,
          type = type,
          flagAttribute = currentFlag or NameAttribute.CLASS.value
            or NameAttribute.PUBLIC.value or NameAttribute.STATIC.value,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      VariableType.NON_STATIC_OTHER -> {
        return Array(
          context = validClass!!.classContext,
          type = type,
          flagAttribute = currentFlag or NameAttribute.PUBLIC.value
            or NameAttribute.CLASS.value,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      VariableType.STATIC_OTHER -> {
        return Array(
          context = validClass!!.classContext,
          type = type,
          flagAttribute = currentFlag or NameAttribute.PUBLIC.value
            or NameAttribute.CLASS.value or NameAttribute.STATIC.value,
          dimension = dimension,
          globalContext = globalContext,
          size = size,
        )
      }
      else -> throw RuntimeException("Unhandled Scope Type")
    }
  }
}
