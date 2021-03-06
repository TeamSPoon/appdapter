/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.appdapter.xload.rspec
import org.appdapter.xload.sheet.{XLSXSheetRepoLoader}

class OfflineXlsSheetRepoSpec(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {
  override protected def makeDirectoryModel = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheet, dirSheet, fileModelCLs: java.util.List[ClassLoader])
  //override def makeRepo(): GoogSheetRepo = XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}
