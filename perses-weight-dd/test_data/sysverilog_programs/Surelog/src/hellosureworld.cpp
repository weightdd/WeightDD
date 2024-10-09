/*
 Copyright 2019 Alain Dargelas

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 * File:   hellosureworld.cpp
 * Author: alain
 *
 * Created on April 13, 2020, 08:07 PM
 */

// Example of usage:
// cd tests/UnitElabBlock
// hellouhdm top.v -parse -mutestdout
#include <iostream>
#include <functional>
#include "uhdm.h"
#include "surelog.h"

int main(int argc, const char** argv) {
  // Read command line, compile a design, use -parse argument
  unsigned int code = 0;
  SURELOG::SymbolTable* symbolTable = new SURELOG::SymbolTable();
  SURELOG::ErrorContainer* errors = new SURELOG::ErrorContainer(symbolTable);
  SURELOG::CommandLineParser* clp =
      new SURELOG::CommandLineParser(errors, symbolTable, false, false);
  clp->noPython();
  bool success = clp->parseCommandLine(argc, argv);
  errors->printMessages(clp->muteStdout());
  SURELOG::Design* the_design = NULL;
  if (success && (!clp->help())) {
    SURELOG::scompiler* compiler = SURELOG::start_compiler(clp);
    the_design = SURELOG::get_design(compiler);
    SURELOG::shutdown_compiler(compiler);
    auto stats = errors->getErrorStats();
    code = (!success) | stats.nbFatal | stats.nbSyntax | stats.nbError;
  }
  delete clp;
  delete symbolTable;
  delete errors;

  // Browse the Surelog Data Model
  if (the_design) {
    for (auto& top : the_design->getTopLevelModuleInstances()) {
      std::function<void(SURELOG::ModuleInstance*)> inst_visit =
          [&inst_visit](SURELOG::ModuleInstance* inst) {
            std::cout << "Inst: " << inst->getFullPathName() << std::endl;
            for (unsigned int i = 0; i < inst->getNbChildren(); i++) {
              inst_visit(inst->getChildren(i));
            }
          };
      inst_visit(top);
    }
  }
  delete the_design;
  return code;
}
