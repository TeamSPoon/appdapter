/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.matdat

import java.io.Reader;
import java.util.Iterator;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory}
import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
import com.hp.hpl.jena.shared.{PrefixMapping}

/**
 * @author Stu B. <www.texpedient.com>
 */

object SemSheet {
	
	val theDbg = new BasicDebugger();
	
	val	MT_Individual = "Individual";
	val MT_TypeProperty = "TypeProperty";
	val MT_ObjectProperty = "ObjectProperty";
	val MT_DatatypeProperty = "DatatypeProperty";

	class ModelInsertSheetProc (val myModel : Model) extends SheetProc(3) {
		var	myIndivColIdx = -1;
		var myIndivResResolver : ResourceResolver = null;
		
		var myOptLastIndiv : Option[Resource] = None;

		
		var myPropColBindings : List[PropertyValueColumnBinding] = List();
		
		override def absorbHeaderRows(headRows : Array[MatrixRow]) {
			val headRowLengths = headRows.map(_.getPossibleColumnCount());
			val headRowLengthMax = headRowLengths.max;
			val	propNameCells = headRows(0);
			val	metaKindCells = headRows(1);
			// Contains *either* RDF-Datatype  *or* default URI/QName prefix (applied when no ":" in data).
			val	subKindCells = headRows(2);
			
			val propResolver = new ResourceResolver(myModel, None);
			
			for (colIdx <- 0 until metaKindCells.getPossibleColumnCount()) {
				val metaKindCell : Option[String] = metaKindCells.getPossibleColumnValueString(colIdx);
				val propNameCell : Option[String] = propNameCells.getPossibleColumnValueString(colIdx);
				val subKindCell : Option[String] = subKindCells.getPossibleColumnValueString(colIdx);
				
				val optProp : Option[Property] =  propNameCell.map  { propResolver.findOrMakeProperty(myModel, _) }

				val optColBind : Option[PropertyValueColumnBinding]	= if (metaKindCell.isDefined) {
					
					val metaKind = metaKindCell.get
					metaKind match {
						case MT_Individual => {
							if (myIndivColIdx != -1) {
								throw new Exception("Got second column with MetaType='Individual' at col# " + colIdx)
							} else if (optProp.isDefined) {
								throw new Exception("Got illegal column with defined property " + optProp.get + " and MetaType='Individual' at col# " + colIdx)
							} else {
								myIndivResResolver = new ResourceResolver(myModel, subKindCell);
								myIndivColIdx = colIdx;
							}
							None;
						}
						case mt @ (MT_TypeProperty | MT_ObjectProperty) => {
							if (optProp.isEmpty) {
								throw new Exception("Got MetaType='" + mt + "' but no property name is specified at col# " + colIdx)
							} 
							val tgtResolver = new ResourceResolver(myModel, subKindCell);
							val binding = new ResLinkColumnBinding(optProp.get, colIdx, tgtResolver);
							Some(binding);
						}
						case mt @ MT_DatatypeProperty => {
							if (optProp.isEmpty) {
								throw new Exception("Got MetaType='" + mt + "' but no property name is specified at col# " + colIdx)
							}
							if (subKindCell.isEmpty) {
								throw new Exception("Got MetaType='" + mt + "' but no RDF-datatype is specified at col# " + colIdx)
							}
							val tm = TypeMapper.getInstance();
							// alternative:  getSafeTypeByName is more lenient
							val dt = tm.getTypeByName(subKindCell.get);
							val binding = new ResDataColumnBinding(optProp.get, colIdx, dt);
							Some(binding);
						}				
						case mt @ _ => {
							if (mt.startsWith("#")) {
								None;
							} else {
								throw new Exception("Unknown MetaType in column " + colIdx + " : " + mt);
							}
						}
					}
				} else None
				println("Got Col Binding: " + optColBind);				
				if (optColBind.isDefined) {
					myPropColBindings = optColBind.get :: myPropColBindings;
				}
			}
			myPropColBindings = myPropColBindings.reverse
		}
		
		override def absorbDataRow(cellRow : MatrixRow) {
			println("Processing SEMANTIC data(!) = " + cellRow.dump());
			if (myIndivColIdx >= 0) {
				val optIndivCell : Option[String] = cellRow.getPossibleColumnValueString(myIndivColIdx);
				val optIndiv : Option[Resource] = if (optIndivCell.isDefined) {
					val indivQNameOrURI = optIndivCell.get
					var indivRes = myIndivResResolver.findOrMakeResource(myModel, indivQNameOrURI)
					Some(indivRes)
				} else myOptLastIndiv;
				if (optIndiv.isDefined) {
					val indiv : Resource = optIndiv.get
					for (pcb <- myPropColBindings) {
						pcb.matrixCellToPossibleModelStmt(cellRow, indiv);
					}
				}
				myOptLastIndiv = optIndiv;
			}
		}		
	}
			
	abstract class PropertyValueColumnBinding(val myProp : Property, val myColIdx : Int) {
		def makeValueNode(cellString : String, model:Model) : RDFNode;
		def matrixCellToPossibleModelStmt(mtxRow: MatrixRow, modelParentRes:Resource) : Option[Statement] = {
			val optCellString : Option[String] = mtxRow.getPossibleColumnValueString(myColIdx);
			optCellString match {
				case None => None;
				case Some(cellString:String) => {
						val model : Model = modelParentRes.getModel();
						val rdfNode : RDFNode = makeValueNode(cellString, model);
						val stmt : Statement = model.createStatement(modelParentRes, myProp, rdfNode);
						model.add(stmt);
						Some(stmt);
					};
			}
		}
	}

	class ResLinkColumnBinding(linkProp : Property, colIdx: Int, val myResolver : ResourceResolver) 
			extends PropertyValueColumnBinding (linkProp, colIdx) {
				
		override def makeValueNode(cellString : String, model:Model) : RDFNode = {
			myResolver.findOrMakeResource(model, cellString);
		}
	}
	class ResDataColumnBinding(datProp : Property, colIdx: Int, val myDatatype : RDFDatatype) 
			extends PropertyValueColumnBinding (datProp, colIdx) {
				
		override def makeValueNode(cellString : String, model:Model) : RDFNode = {
			model.createTypedLiteral(cellString, myDatatype);
		}
	}
	
	def main(args: Array[String]) : Unit = {
	  	println("SemSheet test ");
		val keyForBootSheet22 = "0ArBjkBoH40tndDdsVEVHZXhVRHFETTB5MGhGcWFmeGc";
		
		val namespaceSheetNum = 9;
		val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(keyForBootSheet22, namespaceSheetNum, None);
		println("Made Namespace Sheet URL: " + namespaceSheetURL);
		val namespaceMapProc = new MapSheetProc(1);
		MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
		
		println("Got NS map: " + namespaceMapProc.myResultMap)
		
		
		
		val reposSheetNum = 8;
		val repoSheetURL = WebSheet.makeGdocSheetQueryURL(keyForBootSheet22, reposSheetNum, None);
		println("Made Repos Sheet URL: " + repoSheetURL);
		val sp = new SheetProc(3);
		MatrixData.processSheet (repoSheetURL, sp.processRow);
		val tgtModel : Model = ModelFactory.createDefaultModel();
		
		
		tgtModel.setNsPrefixes (namespaceMapProc.getJavaMap)
		
		val modelInsertProc = new ModelInsertSheetProc(tgtModel);
		MatrixData.processSheet (repoSheetURL, modelInsertProc.processRow);
		println ("tgtModel=" + tgtModel)
	}	
}