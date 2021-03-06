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

package org.appdapter.fancy.rspec

import org.osgi.framework.BundleContext
import org.appdapter.core.boot.ClassLoaderUtils


object RepoSpecDefaultNames {

  // These 2 string constants establish repo-client wrapper defaults, giving a default
  // query context to easily fetch from.
  // Either value may be bypassed/overidden using either
  //      A) Overrides of the RepoSpec methods below
  //   or B) The more general forms of queryIndirect_.
  // 1) Default query *source* graph QName used in directory model (Sheet or RDF).
  // We read SPARQL text from this graph, which we use to query *other* graphs.
  // This graph is typically not used as a regular data graph by  other low-order
  // query operations, although there is no prohibition or protection from doing so
  // at this time.   This query source graph may be overridden using the more general
  // forms of queryIndirect_.

  val DFLT_QRY_SRC_GRAPH_TYPE = "ccrt:qry_sheet_22"

  // 2) default variable name for a single target graph in a SPARQL query.
  // This is used in the convenience forms of queryIndirect that handle many common
  // use cases, wherein the query needs a single graph to operate on that is switched
  // by application logic or user selection.

  val DFLT_TGT_GRAPH_SPARQL_VAR = "qGraph"

  //===============================================================================================
  //  Constants below are for test cases outside of appdapter (so we can test inside appdater)
  // they will be moved!
  //===============================================================================================

  // Formal prefix for Robokind 2012 runtime
  val RKRT_NS_PREFIX = "urn:ftd:robokind.org:2012:runtime#"
  // Formal prefix for Cogchar 2012 runtime
  val NS_CCRT_RT = "urn:ftd:cogchar.org:2012:runtime#"
  // Formal prefix for Cogchar 2012 goody
  val GOODY_NS = "urn:ftd:cogchar.org:2012:goody#"
  // Formal prefix for Cogchar Web
  //public static String WEB_NS = "http://www.cogchar.org/lift/config#";
  // Formal prefix for Cogchar 2012 runtime
  val NS_CC = "http://www.cogchar.org/schema/crazyRandom#"

  // Less formal web prefix still widely used:
  val NSP_Root = "http://www.cogchar.org/"

  val PIPELINE_GRAPH_TYPE = NS_CC + "PipelineModel"; /// "csi:pipeline_sheet_77";
  //val PIPE_QUERY_QN = "ccrt:find_pipes_77";
  //val PIPE_SOURCE_QUERY_QN = "ccrt:find_pipe_sources_78";
  //val PIPE_ATTR_QQN = PIPE_QUERY_QN;
  //val PIPE_SOURCE_QQN = PIPE_SOURCE_QUERY_QN;

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  final val BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c"
  val BMC_NAMESPACE_SHEET_NUM = 4
  val BMC_DIRECTORY_SHEET_NUM = 3

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  // When exported to Disk
  final val BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx"
  val DFLT_NAMESPACE_SHEET_NAME = "Nspc"
  val DFLT_DIRECTORY_SHEET_NAME = "Dir"

  val TGT_GRAPH_SPARQL_VAR = DFLT_TGT_GRAPH_SPARQL_VAR; // "qGraph"

	/*
  def main(args: Array[String]) = {
    val fileResModelCLs: java.util.List[ClassLoader] =\\
      ClassLoaderUtils.getFileResourceClassLoaders(null, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    val repoSpec = new OnlineSheetRepoSpec(RepoSpecDefaultNames.BMC_SHEET_KEY, RepoSpecDefaultNames.BMC_NAMESPACE_SHEET_NUM, RepoSpecDefaultNames.BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
    val repo = repoSpec.getOrMakeRepo //.asInstanceOf[GoogSheetRepo];

    print("Starting Whackamole");
    import org.appdapter.demo.DemoBrowserUI
    val repoNav = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    repoNav.addObject(null, repo, true, false);
    java.lang.Thread.sleep(60000000);
  }
*/


}